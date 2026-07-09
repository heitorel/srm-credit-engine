import { CommonModule, DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  OnInit,
  computed,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { finalize, forkJoin } from 'rxjs';

import { ReferenceDataApiService } from '../../../core/api/reference-data-api.service';
import { ApiErrorResponse } from '../../../models/api-error.model';
import { CurrencyReference } from '../../../models/reference-data.model';
import { PageShellComponent } from '../../../shared/components/page-shell/page-shell.component';
import { normalizeApiError } from '../../../shared/utils/api-error.util';
import { ExchangeRateResponse } from '../models/exchange-rate.models';
import { ExchangeRateApiService } from '../services/exchange-rate-api.service';

type ExchangeRateFormValue = {
  sourceCurrency: string | null;
  targetCurrency: string | null;
  rate: number | null;
  validAt: string | null;
};

function differentCurrencyValidator(
  control: AbstractControl<ExchangeRateFormValue>,
): ValidationErrors | null {
  const value = control.getRawValue();

  if (!value.sourceCurrency || !value.targetCurrency) {
    return null;
  }

  return value.sourceCurrency !== value.targetCurrency
    ? null
    : { sameCurrencyPair: true };
}

function validDateTimeValidator(
  control: AbstractControl<string | null>,
): ValidationErrors | null {
  const value = control.value;

  if (!value) {
    return null;
  }

  return Number.isNaN(new Date(value).getTime()) ? { invalidDateTime: true } : null;
}

@Component({
  selector: 'app-exchange-rates-page',
  imports: [
    CommonModule,
    DatePipe,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatListModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    PageShellComponent,
    ReactiveFormsModule,
  ],
  templateUrl: './exchange-rates-page.component.html',
  styleUrl: './exchange-rates-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ExchangeRatesPageComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly formBuilder = inject(FormBuilder);
  private readonly referenceDataApi = inject(ReferenceDataApiService);
  private readonly exchangeRateApi = inject(ExchangeRateApiService);

  protected readonly form = this.formBuilder.group(
    {
      sourceCurrency: this.formBuilder.control<string | null>(null, [
        Validators.required,
      ]),
      targetCurrency: this.formBuilder.control<string | null>(null, [
        Validators.required,
      ]),
      rate: this.formBuilder.control<number | null>(null, [
        Validators.required,
        Validators.min(0.00000001),
      ]),
      validAt: this.formBuilder.control<string | null>(null, [
        Validators.required,
        validDateTimeValidator,
      ]),
    },
    { validators: differentCurrencyValidator },
  );

  protected readonly currencies = signal<CurrencyReference[]>([]);
  protected readonly referenceDataLoading = signal(true);
  protected readonly referenceDataError = signal<ApiErrorResponse | null>(null);
  protected readonly submitting = signal(false);
  protected readonly latestLookupLoading = signal(false);
  protected readonly createdRate = signal<ExchangeRateResponse | null>(null);
  protected readonly latestRate = signal<ExchangeRateResponse | null>(null);
  protected readonly latestLookupError = signal<ApiErrorResponse | null>(null);
  protected readonly createError = signal<ApiErrorResponse | null>(null);
  protected readonly fieldErrors = signal<Record<string, string>>({});

  protected readonly hasReferenceData = computed(
    () => this.currencies().length > 0 && !this.referenceDataLoading(),
  );
  protected readonly canLookupLatest = computed(() => {
    const sourceCurrency = this.form.controls['sourceCurrency'].value;
    const targetCurrency = this.form.controls['targetCurrency'].value;

    return (
      !!sourceCurrency &&
      !!targetCurrency &&
      sourceCurrency !== targetCurrency &&
      !this.referenceDataLoading() &&
      !this.latestLookupLoading()
    );
  });
  protected readonly statusText = computed(() => {
    if (this.referenceDataLoading()) {
      return 'Loading reference data';
    }

    if (this.submitting()) {
      return 'Saving exchange rate';
    }

    if (this.latestLookupLoading()) {
      return 'Checking latest rate';
    }

    return 'Ready';
  });

  ngOnInit(): void {
    this.loadReferenceData();
    this.form.controls['sourceCurrency'].valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.clearLatestLookupState());
    this.form.controls['targetCurrency'].valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.clearLatestLookupState());
  }

  protected submit(): void {
    this.form.markAllAsTouched();
    this.clearCreateState();

    if (this.form.invalid || this.referenceDataLoading() || this.submitting()) {
      return;
    }

    const formValue = this.form.getRawValue() as ExchangeRateFormValue;

    if (!formValue.validAt) {
      return;
    }

    this.submitting.set(true);

    this.exchangeRateApi
      .create({
        sourceCurrency: formValue.sourceCurrency ?? '',
        targetCurrency: formValue.targetCurrency ?? '',
        rate: Number(formValue.rate),
        validAt: this.toIsoDateTime(formValue.validAt),
      })
      .pipe(finalize(() => this.submitting.set(false)))
      .subscribe({
        next: (response) => {
          this.createdRate.set(response);
        },
        error: (error: unknown) => {
          const apiError = normalizeApiError(error);
          this.createError.set(apiError);
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

  protected lookupLatest(): void {
    this.clearLatestLookupState();

    const sourceCurrency = this.form.controls['sourceCurrency'].value;
    const targetCurrency = this.form.controls['targetCurrency'].value;

    if (
      !sourceCurrency ||
      !targetCurrency ||
      sourceCurrency === targetCurrency ||
      this.referenceDataLoading()
    ) {
      return;
    }

    this.latestLookupLoading.set(true);

    this.exchangeRateApi
      .getLatest(sourceCurrency, targetCurrency)
      .pipe(finalize(() => this.latestLookupLoading.set(false)))
      .subscribe({
        next: (response) => {
          this.latestRate.set(response);
        },
        error: (error: unknown) => {
          this.latestLookupError.set(normalizeApiError(error));
        },
      });
  }

  protected retryReferenceData(): void {
    this.loadReferenceData();
  }

  protected trackByCurrencyCode(_: number, currency: CurrencyReference): string {
    return currency.code;
  }

  protected resolveControlError(controlName: keyof ExchangeRateFormValue): string | null {
    const backendError = this.fieldErrors()[controlName];

    if (backendError) {
      return backendError;
    }

    const control = this.form.controls[controlName];

    if (!control || !(control.touched || control.dirty)) {
      return null;
    }

    if (control.hasError('required')) {
      return 'This field is required.';
    }

    if (control.hasError('min')) {
      return 'Rate must be greater than zero.';
    }

    if (control.hasError('invalidDateTime')) {
      return 'Enter a valid ISO-compatible date and time.';
    }

    if (controlName === 'targetCurrency' && this.form.hasError('sameCurrencyPair')) {
      return 'Target currency must be different from source currency.';
    }

    return null;
  }

  private loadReferenceData(): void {
    this.referenceDataLoading.set(true);
    this.referenceDataError.set(null);

    forkJoin({
      currencies: this.referenceDataApi.listCurrencies(),
    })
      .pipe(finalize(() => this.referenceDataLoading.set(false)))
      .subscribe({
        next: ({ currencies }) => {
          this.currencies.set(currencies.currencies);
          this.applyDefaultSelections();
        },
        error: (error: unknown) => {
          this.referenceDataError.set(normalizeApiError(error));
        },
      });
  }

  private applyDefaultSelections(): void {
    const firstCurrency = this.currencies()[0]?.code ?? null;
    const secondCurrency = this.currencies()[1]?.code ?? firstCurrency;

    this.form.patchValue({
      sourceCurrency: this.form.controls['sourceCurrency'].value ?? firstCurrency,
      targetCurrency: this.form.controls['targetCurrency'].value ?? secondCurrency,
    });
  }

  private clearCreateState(): void {
    this.createdRate.set(null);
    this.createError.set(null);
    this.fieldErrors.set({});
  }

  private clearLatestLookupState(): void {
    this.latestRate.set(null);
    this.latestLookupError.set(null);
  }

  private toIsoDateTime(value: string): string {
    return new Date(value).toISOString();
  }
}
