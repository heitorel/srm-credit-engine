import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  computed,
  inject,
  signal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatNativeDateModule } from '@angular/material/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { finalize, forkJoin } from 'rxjs';

import { ReferenceDataApiService } from '../../../core/api/reference-data-api.service';
import { ApiErrorResponse } from '../../../models/api-error.model';
import {
  CurrencyReference,
  ReceivableTypeReference,
} from '../../../models/reference-data.model';
import { PageShellComponent } from '../../../shared/components/page-shell/page-shell.component';
import { CurrencyAmountPipe } from '../../../shared/pipes/currency-amount.pipe';
import { normalizeApiError } from '../../../shared/utils/api-error.util';
import { PricingSimulationResponse } from '../models/pricing-simulation.models';
import { PricingSimulationApiService } from '../services/pricing-simulation-api.service';

interface SimulationFormValue {
  faceValue: number | null;
  sourceCurrency: string | null;
  paymentCurrency: string | null;
  baseRate: number | null;
  receivableType: string | null;
  dueDate: Date | null;
}

function futureDateValidator(value: Date | null): { futureDate: true } | null {
  if (!value) {
    return null;
  }

  const candidate = new Date(value);
  candidate.setHours(0, 0, 0, 0);

  const today = new Date();
  today.setHours(0, 0, 0, 0);

  return candidate > today ? null : { futureDate: true };
}

@Component({
  selector: 'app-pricing-simulation-page',
  imports: [
    CommonModule,
    CurrencyAmountPipe,
    MatButtonModule,
    MatCardModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatInputModule,
    MatListModule,
    MatNativeDateModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    PageShellComponent,
    ReactiveFormsModule,
  ],
  templateUrl: './pricing-simulation-page.component.html',
  styleUrl: './pricing-simulation-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PricingSimulationPageComponent implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly referenceDataApi = inject(ReferenceDataApiService);
  private readonly pricingSimulationApi = inject(PricingSimulationApiService);

  protected readonly form = this.formBuilder.group({
    faceValue: this.formBuilder.control<number | null>(null, [
      Validators.required,
      Validators.min(0.01),
    ]),
    sourceCurrency: this.formBuilder.control<string | null>(null, [
      Validators.required,
    ]),
    paymentCurrency: this.formBuilder.control<string | null>(null, [
      Validators.required,
    ]),
    baseRate: this.formBuilder.control<number | null>(null, [Validators.min(0)]),
    receivableType: this.formBuilder.control<string | null>(null, [
      Validators.required,
    ]),
    dueDate: this.formBuilder.control<Date | null>(null, [
      Validators.required,
      ({ value }) => futureDateValidator(value),
    ]),
  });

  protected readonly currencies = signal<CurrencyReference[]>([]);
  protected readonly receivableTypes = signal<ReceivableTypeReference[]>([]);
  protected readonly referenceDataLoading = signal(true);
  protected readonly submitting = signal(false);
  protected readonly result = signal<PricingSimulationResponse | null>(null);
  protected readonly apiError = signal<ApiErrorResponse | null>(null);
  protected readonly fieldErrors = signal<Record<string, string>>({});

  protected readonly hasReferenceData = computed(
    () =>
      this.currencies().length > 0 &&
      this.receivableTypes().length > 0 &&
      !this.referenceDataLoading(),
  );

  ngOnInit(): void {
    this.loadReferenceData();
  }

  protected submit(): void {
    this.form.markAllAsTouched();
    this.clearApiErrors();

    if (this.form.invalid || this.referenceDataLoading() || this.submitting()) {
      return;
    }

    const formValue = this.form.getRawValue() as SimulationFormValue;

    if (!formValue.dueDate) {
      return;
    }

    this.submitting.set(true);
    this.result.set(null);

    this.pricingSimulationApi
      .simulate({
        faceValue: Number(formValue.faceValue),
        sourceCurrency: formValue.sourceCurrency ?? '',
        paymentCurrency: formValue.paymentCurrency ?? '',
        ...(formValue.baseRate === null ? {} : { baseRate: Number(formValue.baseRate) }),
        receivableType: formValue.receivableType ?? '',
        dueDate: this.toIsoDate(formValue.dueDate),
      })
      .pipe(finalize(() => this.submitting.set(false)))
      .subscribe({
        next: (response) => {
          this.result.set(response);
        },
        error: (error: unknown) => {
          const apiError = normalizeApiError(error);
          this.apiError.set(apiError);
          this.fieldErrors.set(
            Object.fromEntries(
              (apiError.details ?? [])
                .filter(
                  (detail): detail is { field: string; message: string } =>
                    typeof detail.field === 'string' && detail.field.length > 0,
                )
                .map((detail) => [detail.field, detail.message]),
            ),
          );
        },
      });
  }

  protected trackByCurrencyCode(_: number, currency: CurrencyReference): string {
    return currency.code;
  }

  protected trackByReceivableType(
    _: number,
    receivableType: ReceivableTypeReference,
  ): string {
    return receivableType.code;
  }

  protected resolveControlError(controlName: keyof SimulationFormValue): string | null {
    const backendError = this.fieldErrors()[controlName];

    if (backendError) {
      return backendError;
    }

    const control = this.form.controls[controlName];

    if (!control || !(control.touched || control.dirty)) {
      return null;
    }

    if (control.hasError('required')) {
      return 'Campo obrigatório.';
    }

    if (control.hasError('min')) {
      if (controlName === 'baseRate') {
        return 'A taxa base deve ser maior ou igual a zero.';
      }

      return 'O valor de face deve ser maior que zero.';
    }

    if (control.hasError('futureDate')) {
      return 'A data de vencimento deve ser futura.';
    }

    return null;
  }

  private loadReferenceData(): void {
    this.referenceDataLoading.set(true);
    this.clearApiErrors();

    forkJoin({
      currencies: this.referenceDataApi.listCurrencies(),
      receivableTypes: this.referenceDataApi.listReceivableTypes(),
    })
      .pipe(finalize(() => this.referenceDataLoading.set(false)))
      .subscribe({
        next: ({ currencies, receivableTypes }) => {
          this.currencies.set(currencies.currencies);
          this.receivableTypes.set(receivableTypes.receivableTypes);
          this.applyDefaultSelections();
        },
        error: (error: unknown) => {
          this.apiError.set(normalizeApiError(error));
        },
      });
  }

  private applyDefaultSelections(): void {
    const firstCurrencyCode = this.currencies()[0]?.code ?? null;
    const firstReceivableType = this.receivableTypes()[0]?.code ?? null;

    this.form.patchValue({
      sourceCurrency: this.form.controls.sourceCurrency.value ?? firstCurrencyCode,
      paymentCurrency: this.form.controls.paymentCurrency.value ?? firstCurrencyCode,
      receivableType: this.form.controls.receivableType.value ?? firstReceivableType,
    });
  }

  private clearApiErrors(): void {
    this.apiError.set(null);
    this.fieldErrors.set({});
  }

  private toIsoDate(value: Date): string {
    const year = value.getFullYear();
    const month = `${value.getMonth() + 1}`.padStart(2, '0');
    const day = `${value.getDate()}`.padStart(2, '0');

    return `${year}-${month}-${day}`;
  }
}
