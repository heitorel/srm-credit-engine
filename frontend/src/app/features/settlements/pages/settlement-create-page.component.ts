import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  signal,
} from '@angular/core';
import {
  AbstractControl,
  FormArray,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
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
import {
  CreateSettlementRequest,
  SettlementDetailResponse,
} from '../models/settlement.models';
import { SettlementApiService } from '../services/settlement-api.service';

type SettlementFormValue = {
  assignor: {
    name: string | null;
    document: string | null;
  };
  paymentCurrency: string | null;
  baseRate: number | null;
  receivables: Array<{
    externalReference: string | null;
    faceValue: number | null;
    sourceCurrency: string | null;
    receivableType: string | null;
    dueDate: Date | null;
  }>;
};

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

function settlementBatchValidator(
  control: AbstractControl,
): ValidationErrors | null {
  const receivables = control.get('receivables') as FormArray | null;

  if (!receivables) {
    return null;
  }

  const length = receivables.length;

  if (length < 1 || length > 100) {
    return { batchSize: true };
  }

  const sourceCurrencies = receivables.controls
    .map((item) => item.get('sourceCurrency')?.value)
    .filter((value): value is string => typeof value === 'string' && value.length > 0);

  if (sourceCurrencies.length <= 1) {
    return null;
  }

  return sourceCurrencies.every((value) => value === sourceCurrencies[0])
    ? null
    : { mixedSourceCurrency: true };
}

@Component({
  selector: 'app-settlement-create-page',
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
    RouterLink,
  ],
  templateUrl: './settlement-create-page.component.html',
  styleUrl: './settlement-create-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SettlementCreatePageComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly referenceDataApi = inject(ReferenceDataApiService);
  private readonly settlementApi = inject(SettlementApiService);

  protected readonly form = this.formBuilder.group(
    {
      assignor: this.formBuilder.group({
        name: this.formBuilder.control<string | null>(null, [Validators.required]),
        document: this.formBuilder.control<string | null>(null),
      }),
      paymentCurrency: this.formBuilder.control<string | null>(null, [
        Validators.required,
      ]),
      baseRate: this.formBuilder.control<number | null>(null, [Validators.min(0)]),
      receivables: this.formBuilder.array([this.createReceivableGroup()]),
    },
    { validators: settlementBatchValidator },
  );

  protected readonly currencies = signal<CurrencyReference[]>([]);
  protected readonly receivableTypes = signal<ReceivableTypeReference[]>([]);
  protected readonly referenceDataLoading = signal(true);
  protected readonly submitting = signal(false);
  protected readonly apiError = signal<ApiErrorResponse | null>(null);
  protected readonly fieldErrors = signal<Record<string, string>>({});
  protected readonly createdSettlement = signal<SettlementDetailResponse | null>(null);

  protected readonly hasReferenceData = computed(
    () =>
      this.currencies().length > 0 &&
      this.receivableTypes().length > 0 &&
      !this.referenceDataLoading(),
  );

  constructor() {
    this.loadReferenceData();
  }

  protected get receivables(): FormArray {
    return this.form.controls.receivables as FormArray;
  }

  protected addReceivable(): void {
    if (this.receivables.length >= 100) {
      this.form.markAllAsTouched();
      return;
    }

    this.receivables.push(this.createReceivableGroup());
  }

  protected removeReceivable(index: number): void {
    if (this.receivables.length === 1) {
      return;
    }

    this.receivables.removeAt(index);
    this.form.updateValueAndValidity();
  }

  protected submit(): void {
    this.form.markAllAsTouched();
    this.clearApiErrors();

    if (this.form.invalid || this.referenceDataLoading() || this.submitting()) {
      return;
    }

    const payload = this.buildPayload();

    if (!payload) {
      return;
    }

    this.submitting.set(true);
    this.createdSettlement.set(null);

    this.settlementApi
      .create(payload)
      .pipe(finalize(() => this.submitting.set(false)))
      .subscribe({
        next: (response) => {
          this.createdSettlement.set(response);
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

  protected openCreatedSettlement(): void {
    const settlementId = this.createdSettlement()?.id;

    if (!settlementId) {
      return;
    }

    void this.router.navigate(['/settlements', settlementId]);
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

  protected resolveTopLevelError(
    path: 'assignor.name' | 'assignor.document' | 'paymentCurrency' | 'baseRate',
  ): string | null {
    const backendError = this.fieldErrors()[path];

    if (backendError) {
      return backendError;
    }

    const control = this.form.get(path);

    if (!control || !(control.touched || control.dirty)) {
      return null;
    }

    if (control.hasError('required')) {
      return 'Campo obrigatório.';
    }

    if (control.hasError('min')) {
      return 'A taxa base deve ser maior ou igual a zero.';
    }

    return null;
  }

  protected resolveReceivableControlError(
    index: number,
    controlName:
      | 'externalReference'
      | 'faceValue'
      | 'sourceCurrency'
      | 'receivableType'
      | 'dueDate',
  ): string | null {
    const backendError = this.fieldErrors()[`receivables[${index}].${controlName}`];

    if (backendError) {
      return backendError;
    }

    const control = this.receivables.at(index)?.get(controlName);

    if (!control || !(control.touched || control.dirty)) {
      return null;
    }

    if (control.hasError('required')) {
      return 'Campo obrigatório.';
    }

    if (control.hasError('min')) {
      return 'O valor deve ser maior que zero.';
    }

    if (control.hasError('futureDate')) {
      return 'A data de vencimento deve ser futura.';
    }

    return null;
  }

  protected resolveBatchError(): string | null {
    const backendError = this.fieldErrors()['receivables'];

    if (backendError) {
      return backendError;
    }

    if (!(this.form.touched || this.form.dirty)) {
      return null;
    }

    if (this.form.hasError('batchSize')) {
      return 'O lote deve conter entre 1 e 100 recebíveis.';
    }

    if (this.form.hasError('mixedSourceCurrency')) {
      return 'Todos os recebíveis do lote devem ter a mesma moeda de origem.';
    }

    return null;
  }

  private createReceivableGroup() {
    return this.formBuilder.group({
      externalReference: this.formBuilder.control<string | null>(null, [
        Validators.required,
      ]),
      faceValue: this.formBuilder.control<number | null>(null, [
        Validators.required,
        Validators.min(0.01),
      ]),
      sourceCurrency: this.formBuilder.control<string | null>(null, [
        Validators.required,
      ]),
      receivableType: this.formBuilder.control<string | null>(null, [
        Validators.required,
      ]),
      dueDate: this.formBuilder.control<Date | null>(null, [
        Validators.required,
        ({ value }) => futureDateValidator(value),
      ]),
    });
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
      paymentCurrency: this.form.controls.paymentCurrency.value ?? firstCurrencyCode,
    });

    this.receivables.controls.forEach((control) => {
      control.patchValue({
        sourceCurrency: control.get('sourceCurrency')?.value ?? firstCurrencyCode,
        receivableType: control.get('receivableType')?.value ?? firstReceivableType,
      });
    });
  }

  private buildPayload(): CreateSettlementRequest | null {
    const formValue = this.form.getRawValue() as SettlementFormValue;

    const receivables = formValue.receivables
      .map((receivable) => {
        if (!receivable.dueDate) {
          return null;
        }

        return {
          externalReference: receivable.externalReference ?? '',
          faceValue: Number(receivable.faceValue),
          sourceCurrency: receivable.sourceCurrency ?? '',
          receivableType: receivable.receivableType ?? '',
          dueDate: this.toIsoDate(receivable.dueDate),
        };
      })
      .filter(
        (
          receivable,
        ): receivable is CreateSettlementRequest['receivables'][number] =>
          receivable !== null,
      );

    if (receivables.length !== formValue.receivables.length) {
      return null;
    }

    return {
      assignor: {
        name: formValue.assignor.name ?? '',
        ...(formValue.assignor.document
          ? { document: formValue.assignor.document }
          : {}),
      },
      paymentCurrency: formValue.paymentCurrency ?? '',
      ...(formValue.baseRate === null ? {} : { baseRate: Number(formValue.baseRate) }),
      receivables,
    };
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
