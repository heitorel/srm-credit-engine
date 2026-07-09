import { HttpErrorResponse } from '@angular/common/http';
import { of, throwError } from 'rxjs';

import { TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';

import { ReferenceDataApiService } from '../../../core/api/reference-data-api.service';
import { PricingSimulationApiService } from '../services/pricing-simulation-api.service';
import { PricingSimulationPageComponent } from './pricing-simulation-page.component';

describe('PricingSimulationPageComponent', () => {
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
    simulateImpl?: ReturnType<typeof vi.fn>;
  }) {
    const referenceDataApi = {
      listCurrencies: vi.fn().mockReturnValue(of(currenciesResponse)),
      listReceivableTypes: vi.fn().mockReturnValue(of(receivableTypesResponse)),
    };
    const pricingSimulationApi = {
      simulate:
        options?.simulateImpl ??
        vi.fn().mockReturnValue(
          of({
            faceValue: 10000,
            sourceCurrency: 'BRL',
            paymentCurrency: 'USD',
            presentValueInSourceCurrency: 9509.18,
            netPaymentValue: 1811.27,
            discountValue: 490.82,
            baseRate: 0.01,
            spread: 0.015,
            termInMonths: 2.06666667,
            exchangeRate: 5.25,
            calculatedAt: '2026-07-07T13:20:00Z',
          }),
        ),
    };

    await TestBed.configureTestingModule({
      imports: [PricingSimulationPageComponent],
      providers: [
        provideNoopAnimations(),
        {
          provide: ReferenceDataApiService,
          useValue: referenceDataApi,
        },
        {
          provide: PricingSimulationApiService,
          useValue: pricingSimulationApi,
        },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(PricingSimulationPageComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    return { fixture, referenceDataApi, pricingSimulationApi };
  }

  it('loads reference data and submits a valid simulation payload', async () => {
    const simulate = vi.fn().mockReturnValue(of({}));
    const { fixture, referenceDataApi, pricingSimulationApi } =
      await createComponent({ simulateImpl: simulate });
    const component = fixture.componentInstance;
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 10);

    component['form'].setValue({
      faceValue: 10000,
      sourceCurrency: 'BRL',
      paymentCurrency: 'USD',
      baseRate: null,
      receivableType: 'MERCANTILE_DUPLICATE',
      dueDate: tomorrow,
    });

    component['submit']();

    expect(referenceDataApi.listCurrencies).toHaveBeenCalledTimes(1);
    expect(referenceDataApi.listReceivableTypes).toHaveBeenCalledTimes(1);
    expect(pricingSimulationApi.simulate).toHaveBeenCalledWith({
      faceValue: 10000,
      sourceCurrency: 'BRL',
      paymentCurrency: 'USD',
      receivableType: 'MERCANTILE_DUPLICATE',
      dueDate: `${tomorrow.getFullYear()}-${`${tomorrow.getMonth() + 1}`.padStart(2, '0')}-${`${tomorrow.getDate()}`.padStart(2, '0')}`,
    });
  });

  it('renders backend-calculated values after a successful simulation', async () => {
    const { fixture } = await createComponent();
    const component = fixture.componentInstance;
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 10);

    component['form'].patchValue({
      faceValue: 10000,
      sourceCurrency: 'BRL',
      paymentCurrency: 'USD',
      baseRate: 0.01,
      receivableType: 'MERCANTILE_DUPLICATE',
      dueDate: tomorrow,
    });

    component['submit']();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Resultado calculado');
    expect(fixture.nativeElement.textContent).toContain('BRL 9,509.18');
    expect(fixture.nativeElement.textContent).toContain('USD 1,811.27');
    expect(fixture.nativeElement.textContent).toContain('5.25000000');
  });

  it('shows structured backend errors when the simulation fails', async () => {
    const { fixture } = await createComponent({
      simulateImpl: vi.fn().mockReturnValue(
        throwError(
          () =>
            new HttpErrorResponse({
              status: 422,
              statusText: 'Unprocessable Entity',
              url: '/api/pricing/simulations',
              error: {
                timestamp: '2026-07-07T13:20:00Z',
                status: 422,
                error: 'Unprocessable Entity',
                message: 'Missing exchange rate for currency pair BRL -> USD.',
                path: '/api/pricing/simulations',
                details: [
                  {
                    field: 'paymentCurrency',
                    message:
                      'Exchange rate BRL -> USD is required for cross-currency operation.',
                  },
                ],
              },
            }),
        ),
      ),
    });
    const component = fixture.componentInstance;
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 5);

    component['form'].patchValue({
      faceValue: 10000,
      sourceCurrency: 'BRL',
      paymentCurrency: 'USD',
      baseRate: 0.01,
      receivableType: 'MERCANTILE_DUPLICATE',
      dueDate: tomorrow,
    });

    component['submit']();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Não foi possível simular');
    expect(fixture.nativeElement.textContent).toContain(
      'Missing exchange rate for currency pair BRL -> USD.',
    );
    expect(fixture.nativeElement.textContent).toContain(
      'Exchange rate BRL -> USD is required for cross-currency operation.',
    );
  });
});
