import { HttpErrorResponse } from '@angular/common/http';
import { of, throwError } from 'rxjs';

import { TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { provideRouter } from '@angular/router';

import { ReferenceDataApiService } from '../../../core/api/reference-data-api.service';
import { SettlementStatementApiService } from '../services/settlement-statement-api.service';
import { SettlementsPageComponent } from './settlements-page.component';

describe('SettlementsPageComponent', () => {
  const currenciesResponse = {
    currencies: [
      { code: 'BRL', name: 'Brazilian Real', decimalPlaces: 2 },
      { code: 'USD', name: 'US Dollar', decimalPlaces: 2 },
    ],
  };
  const receivableTypesResponse = {
    receivableTypes: [
      {
        code: 'MERCANTILE_DUPLICATE',
        description: 'Mercantile Duplicate',
        monthlySpread: 0.015,
      },
      {
        code: 'POST_DATED_CHECK',
        description: 'Post-Dated Check',
        monthlySpread: 0.025,
      },
    ],
  };
  const statementResponse = {
    content: [
      {
        settlementId: '7b4b65ab-c30d-47f8-8356-0f0c2ab2e2df',
        assignorId: '5b66f1d1-906e-42cc-bdd2-f7d9d8a08389',
        assignorName: 'ACME Comercio Ltda.',
        assignorDocument: '12345678000199',
        sourceCurrency: 'BRL',
        paymentCurrency: 'USD',
        status: 'SETTLED',
        itemCount: 2,
        totalFaceValue: 15000,
        totalPresentValue: 13980.15,
        totalPaymentValue: 2662.89,
        settledAt: '2026-07-07T13:30:00Z',
      },
    ],
    page: 0,
    size: 20,
    totalElements: 1,
    totalPages: 1,
    first: true,
    last: true,
  };

  async function createComponent(options?: {
    getStatementImpl?: ReturnType<typeof vi.fn>;
  }) {
    vi.useFakeTimers();

    const referenceDataApi = {
      listCurrencies: vi.fn().mockReturnValue(of(currenciesResponse)),
      listReceivableTypes: vi.fn().mockReturnValue(of(receivableTypesResponse)),
    };
    const settlementStatementApi = {
      getStatement:
        options?.getStatementImpl ??
        vi.fn().mockReturnValue(of(statementResponse)),
    };

    await TestBed.configureTestingModule({
      imports: [SettlementsPageComponent],
      providers: [
        provideNoopAnimations(),
        provideRouter([]),
        {
          provide: ReferenceDataApiService,
          useValue: referenceDataApi,
        },
        {
          provide: SettlementStatementApiService,
          useValue: settlementStatementApi,
        },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(SettlementsPageComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    return { fixture, referenceDataApi, settlementStatementApi };
  }

  afterEach(() => {
    vi.useRealTimers();
  });

  it('loads the first backend page on initialization', async () => {
    const { referenceDataApi, settlementStatementApi } = await createComponent();

    expect(referenceDataApi.listCurrencies).toHaveBeenCalledTimes(1);
    expect(referenceDataApi.listReceivableTypes).toHaveBeenCalledTimes(1);
    expect(settlementStatementApi.getStatement).toHaveBeenCalledWith({
      page: 0,
      size: 20,
      sort: 'settledAt,desc',
    });
  });

  it('requests a new page when filters change', async () => {
    const { fixture, settlementStatementApi } = await createComponent();
    const component = fixture.componentInstance;

    component['form'].patchValue({
      paymentCurrency: 'USD',
      status: 'SETTLED',
    });
    vi.advanceTimersByTime(300);
    await fixture.whenStable();
    fixture.detectChanges();

    expect(settlementStatementApi.getStatement).toHaveBeenLastCalledWith({
      paymentCurrency: 'USD',
      status: 'SETTLED',
      page: 0,
      size: 20,
      sort: 'settledAt,desc',
    });
  });

  it('requests the selected backend page through the paginator event', async () => {
    const { fixture, settlementStatementApi } = await createComponent();
    const component = fixture.componentInstance;

    component['handlePageChange']({
      pageIndex: 1,
      pageSize: 20,
      length: 25,
      previousPageIndex: 0,
    });
    fixture.detectChanges();

    expect(settlementStatementApi.getStatement).toHaveBeenLastCalledWith({
      page: 1,
      size: 20,
      sort: 'settledAt,desc',
    });
  });

  it('shows backend errors when the statement request fails', async () => {
    const { fixture } = await createComponent({
      getStatementImpl: vi.fn().mockReturnValue(
        throwError(
          () =>
            new HttpErrorResponse({
              status: 400,
              statusText: 'Bad Request',
              url: '/api/settlements/statement',
              error: {
                timestamp: '2026-07-07T13:40:00Z',
                status: 400,
                error: 'Bad Request',
                message: 'Invalid statement date range.',
                path: '/api/settlements/statement',
                details: [
                  {
                    field: 'from',
                    message: 'From date must be less than or equal to to date.',
                  },
                ],
              },
            }),
        ),
      ),
    });

    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Não foi possível carregar o extrato');
    expect(fixture.nativeElement.textContent).toContain('Invalid statement date range.');
    expect(fixture.nativeElement.textContent).toContain(
      'From date must be less than or equal to to date.',
    );
  });
});
