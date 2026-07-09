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
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatNativeDateModule } from '@angular/material/core';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { debounceTime, finalize, forkJoin } from 'rxjs';

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
  SettlementStatementFilters,
  SettlementStatementResponse,
  SettlementStatusOption,
} from '../models/settlement-statement.models';
import { SettlementStatementApiService } from '../services/settlement-statement-api.service';

type StatementFiltersFormValue = {
  from: Date | null;
  to: Date | null;
  assignorId: string | null;
  assignorDocument: string | null;
  paymentCurrency: string | null;
  sourceCurrency: string | null;
  receivableType: string | null;
  status: string | null;
  size: number;
};

function dateRangeValidator(
  control: AbstractControl<StatementFiltersFormValue>,
): ValidationErrors | null {
  const value = control.getRawValue();

  if (!value.from || !value.to) {
    return null;
  }

  return value.from <= value.to ? null : { dateRange: true };
}

@Component({
  selector: 'app-settlements-page',
  imports: [
    CommonModule,
    CurrencyAmountPipe,
    DatePipe,
    MatButtonModule,
    MatCardModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatInputModule,
    MatNativeDateModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatTableModule,
    PageShellComponent,
    ReactiveFormsModule,
  ],
  templateUrl: './settlements-page.component.html',
  styleUrl: './settlements-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SettlementsPageComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly formBuilder = inject(FormBuilder);
  private readonly referenceDataApi = inject(ReferenceDataApiService);
  private readonly settlementStatementApi = inject(SettlementStatementApiService);

  protected readonly displayedColumns = [
    'assignor',
    'paymentCurrency',
    'status',
    'itemCount',
    'totalFaceValue',
    'totalPresentValue',
    'totalPaymentValue',
    'settledAt',
  ];

  protected readonly pageSizeOptions = [10, 20, 50, 100];
  protected readonly settlementStatuses: SettlementStatusOption[] = [
    { value: 'PENDING', label: 'Pending' },
    { value: 'SETTLED', label: 'Settled' },
    { value: 'FAILED', label: 'Failed' },
    { value: 'CANCELLED', label: 'Cancelled' },
  ];

  protected readonly form = this.formBuilder.group(
    {
      from: this.formBuilder.control<Date | null>(null),
      to: this.formBuilder.control<Date | null>(null),
      assignorId: this.formBuilder.control<string | null>(null),
      assignorDocument: this.formBuilder.control<string | null>(null),
      paymentCurrency: this.formBuilder.control<string | null>(null),
      sourceCurrency: this.formBuilder.control<string | null>(null),
      receivableType: this.formBuilder.control<string | null>(null),
      status: this.formBuilder.control<string | null>(null),
      size: this.formBuilder.nonNullable.control(20, [
        Validators.min(1),
        Validators.max(100),
      ]),
    },
    { validators: dateRangeValidator },
  );

  protected readonly currencies = signal<CurrencyReference[]>([]);
  protected readonly receivableTypes = signal<ReceivableTypeReference[]>([]);
  protected readonly referenceDataLoading = signal(true);
  protected readonly statementLoading = signal(false);
  protected readonly statement = signal<SettlementStatementResponse | null>(null);
  protected readonly apiError = signal<ApiErrorResponse | null>(null);
  protected readonly pageIndex = signal(0);

  protected readonly hasRows = computed(
    () => (this.statement()?.content.length ?? 0) > 0,
  );
  protected readonly emptyStateVisible = computed(
    () =>
      !this.referenceDataLoading() &&
      !this.statementLoading() &&
      !this.apiError() &&
      !this.hasRows(),
  );
  protected readonly summaryText = computed(() => {
    const statement = this.statement();

    if (!statement || statement.totalElements === 0) {
      return 'No settlements match the current filters.';
    }

    const firstItem = statement.page * statement.size + 1;
    const lastItem = firstItem + statement.content.length - 1;

    return `Showing ${firstItem}-${lastItem} of ${statement.totalElements} settlements.`;
  });

  ngOnInit(): void {
    this.loadReferenceData();
    this.form.valueChanges
      .pipe(debounceTime(300), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        if (this.referenceDataLoading()) {
          return;
        }

        this.pageIndex.set(0);

        if (this.form.valid) {
          this.loadStatement();
        }
      });
  }

  protected handlePageChange(event: PageEvent): void {
    const sizeChanged = event.pageSize !== this.form.controls['size'].value;
    this.pageIndex.set(event.pageIndex);

    if (sizeChanged) {
      this.form.controls['size'].setValue(event.pageSize);
      return;
    }

    this.loadStatement();
  }

  protected clearFilters(): void {
    this.form.reset(
      {
        from: null,
        to: null,
        assignorId: null,
        assignorDocument: null,
        paymentCurrency: null,
        sourceCurrency: null,
        receivableType: null,
        status: null,
        size: 20,
      },
      { emitEvent: false },
    );
    this.pageIndex.set(0);
    this.loadStatement();
  }

  protected retry(): void {
    if (this.referenceDataLoading()) {
      this.loadReferenceData();
      return;
    }

    this.loadStatement();
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

  protected trackByStatus(_: number, status: SettlementStatusOption): string {
    return status.value;
  }

  protected resolveFormError(): string | null {
    if (!(this.form.dirty || this.form.touched)) {
      return null;
    }

    if (this.form.hasError('dateRange')) {
      return 'From date must be less than or equal to to date.';
    }

    if (
      this.form.controls['size'].hasError('min') ||
      this.form.controls['size'].hasError('max')
    ) {
      return 'Page size must be between 1 and 100.';
    }

    return null;
  }

  private loadReferenceData(): void {
    this.referenceDataLoading.set(true);
    this.apiError.set(null);

    forkJoin({
      currencies: this.referenceDataApi.listCurrencies(),
      receivableTypes: this.referenceDataApi.listReceivableTypes(),
    })
      .pipe(finalize(() => this.referenceDataLoading.set(false)))
      .subscribe({
        next: ({ currencies, receivableTypes }) => {
          this.currencies.set(currencies.currencies);
          this.receivableTypes.set(receivableTypes.receivableTypes);
          this.loadStatement();
        },
        error: (error: unknown) => {
          this.apiError.set(normalizeApiError(error));
        },
      });
  }

  private loadStatement(): void {
    if (this.form.invalid) {
      this.statement.set(null);
      return;
    }

    this.statementLoading.set(true);
    this.apiError.set(null);

    this.settlementStatementApi
      .getStatement(this.buildFilters())
      .pipe(finalize(() => this.statementLoading.set(false)))
      .subscribe({
        next: (response) => {
          this.statement.set(response);
        },
        error: (error: unknown) => {
          this.statement.set(null);
          this.apiError.set(normalizeApiError(error));
        },
      });
  }

  private buildFilters(): SettlementStatementFilters {
    const formValue = this.form.getRawValue() as StatementFiltersFormValue;

    return {
      ...(formValue.from ? { from: this.toIsoDate(formValue.from) } : {}),
      ...(formValue.to ? { to: this.toIsoDate(formValue.to) } : {}),
      ...(formValue.assignorId ? { assignorId: formValue.assignorId.trim() } : {}),
      ...(formValue.assignorDocument
        ? { assignorDocument: formValue.assignorDocument.trim() }
        : {}),
      ...(formValue.paymentCurrency ? { paymentCurrency: formValue.paymentCurrency } : {}),
      ...(formValue.sourceCurrency ? { sourceCurrency: formValue.sourceCurrency } : {}),
      ...(formValue.receivableType ? { receivableType: formValue.receivableType } : {}),
      ...(formValue.status ? { status: formValue.status } : {}),
      page: this.pageIndex(),
      size: formValue.size,
      sort: 'settledAt,desc',
    };
  }

  private toIsoDate(value: Date): string {
    const year = value.getFullYear();
    const month = `${value.getMonth() + 1}`.padStart(2, '0');
    const day = `${value.getDate()}`.padStart(2, '0');

    return `${year}-${month}-${day}`;
  }
}
