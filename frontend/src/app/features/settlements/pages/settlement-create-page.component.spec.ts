import { HttpErrorResponse } from '@angular/common/http';
import { of, throwError } from 'rxjs';

import { TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { provideRouter } from '@angular/router';

import { ReferenceDataApiService } from '../../../core/api/reference-data-api.service';
import { SettlementApiService } from '../services/settlement-api.service';
import { SettlementCreatePageComponent } from './settlement-create-page.component';

describe('SettlementCreatePageComponent', () => {
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
    ],
  };

  async function createComponent(options?: {
    createImpl?: ReturnType<typeof vi.fn>;
  }) {
    const referenceDataApi = {
      listCurrencies: vi.fn().mockReturnValue(of(currenciesResponse)),
      listReceivableTypes: vi.fn().mockReturnValue(of(receivableTypesResponse)),
    };
    const settlementApi = {
      create:
        options?.createImpl ??
        vi.fn().mockReturnValue(
          of({
            id: '7b4b65ab-c30d-47f8-8356-0f0c2ab2e2df',
            assignor: {
              id: '5b66f1d1-906e-42cc-bdd2-f7d9d8a08389',
              name: 'ACME Comercio Ltda.',
              document: '12345678000199',
            },
            sourceCurrency: 'BRL',
            paymentCurrency: 'USD',
            status: 'SETTLED',
            baseRate: 0.01,
            itemCount: 1,
            totalFaceValue: 10000,
            totalPresentValue: 9509.18,
            totalPaymentValue: 1811.27,
            settledAt: '2026-07-07T13:30:00Z',
            items: [],
          }),
        ),
      getById: vi.fn(),
    };
    await TestBed.configureTestingModule({
      imports: [SettlementCreatePageComponent],
      providers: [
        provideNoopAnimations(),
        provideRouter([]),
        { provide: ReferenceDataApiService, useValue: referenceDataApi },
        { provide: SettlementApiService, useValue: settlementApi },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(SettlementCreatePageComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    return { fixture, referenceDataApi, settlementApi };
  }

  it('loads reference data and submits the settlement payload', async () => {
    const create = vi.fn().mockReturnValue(of({}));
    const { fixture, settlementApi } = await createComponent({ createImpl: create });
    const component = fixture.componentInstance;
    const dueDate = new Date();
    dueDate.setDate(dueDate.getDate() + 10);

    component['form'].patchValue({
      assignor: {
        name: 'ACME Comercio Ltda.',
        document: '12345678000199',
      },
      paymentCurrency: 'USD',
      baseRate: 0.01,
    });
    component['receivables'].at(0).patchValue({
      externalReference: 'NF-1001',
      faceValue: 10000,
      sourceCurrency: 'BRL',
      receivableType: 'MERCANTILE_DUPLICATE',
      dueDate,
    });

    component['submit']();

    expect(settlementApi.create).toHaveBeenCalledWith({
      assignor: {
        name: 'ACME Comercio Ltda.',
        document: '12345678000199',
      },
      paymentCurrency: 'USD',
      baseRate: 0.01,
      receivables: [
        {
          externalReference: 'NF-1001',
          faceValue: 10000,
          sourceCurrency: 'BRL',
          receivableType: 'MERCANTILE_DUPLICATE',
          dueDate: `${dueDate.getFullYear()}-${`${dueDate.getMonth() + 1}`.padStart(2, '0')}-${`${dueDate.getDate()}`.padStart(2, '0')}`,
        },
      ],
    });
  });

  it('renders the success state after a settlement is created', async () => {
    const { fixture } = await createComponent();
    const component = fixture.componentInstance;
    const dueDate = new Date();
    dueDate.setDate(dueDate.getDate() + 10);

    component['form'].patchValue({
      assignor: {
        name: 'ACME Comercio Ltda.',
        document: '12345678000199',
      },
      paymentCurrency: 'USD',
    });
    component['receivables'].at(0).patchValue({
      externalReference: 'NF-1001',
      faceValue: 10000,
      sourceCurrency: 'BRL',
      receivableType: 'MERCANTILE_DUPLICATE',
      dueDate,
    });

    component['submit']();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Liquidação registrada');
    expect(fixture.nativeElement.textContent).toContain('ACME Comercio Ltda.');
    expect(fixture.nativeElement.textContent).toContain('USD 1,811.27');
  });

  it('shows structured backend errors when settlement creation fails', async () => {
    const { fixture } = await createComponent({
      createImpl: vi.fn().mockReturnValue(
        throwError(
          () =>
            new HttpErrorResponse({
              status: 422,
              statusText: 'Unprocessable Entity',
              url: '/api/settlements',
              error: {
                timestamp: '2026-07-07T13:30:00Z',
                status: 422,
                error: 'Unprocessable Entity',
                message:
                  'An exchange rate was not found for BRL -> USD at the settlement timestamp.',
                path: '/api/settlements',
                details: [
                  {
                    field: 'paymentCurrency',
                    message:
                      'Exchange rate snapshot is unavailable for the selected payment currency.',
                  },
                ],
              },
            }),
        ),
      ),
    });
    const component = fixture.componentInstance;
    const dueDate = new Date();
    dueDate.setDate(dueDate.getDate() + 10);

    component['form'].patchValue({
      assignor: {
        name: 'ACME Comercio Ltda.',
        document: '12345678000199',
      },
      paymentCurrency: 'USD',
    });
    component['receivables'].at(0).patchValue({
      externalReference: 'NF-1001',
      faceValue: 10000,
      sourceCurrency: 'BRL',
      receivableType: 'MERCANTILE_DUPLICATE',
      dueDate,
    });
    component['submit']();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain(
      'Não foi possível registrar a liquidação',
    );
    expect(fixture.nativeElement.textContent).toContain(
      'An exchange rate was not found for BRL -> USD at the settlement timestamp.',
    );
  });
});
