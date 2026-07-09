import { HttpErrorResponse } from '@angular/common/http';
import { of, throwError } from 'rxjs';

import { TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';

import { ReferenceDataApiService } from '../../../core/api/reference-data-api.service';
import { ExchangeRateApiService } from '../services/exchange-rate-api.service';
import { ExchangeRatesPageComponent } from './exchange-rates-page.component';

describe('ExchangeRatesPageComponent', () => {
  const currenciesResponse = {
    currencies: [
      { code: 'BRL', name: 'Brazilian Real', decimalPlaces: 2 },
      { code: 'USD', name: 'US Dollar', decimalPlaces: 2 },
    ],
  };

  async function createComponent(options?: {
    createImpl?: ReturnType<typeof vi.fn>;
    getLatestImpl?: ReturnType<typeof vi.fn>;
  }) {
    const referenceDataApi = {
      listCurrencies: vi.fn().mockReturnValue(of(currenciesResponse)),
      listReceivableTypes: vi.fn(),
    };
    const exchangeRateApi = {
      create:
        options?.createImpl ??
        vi.fn().mockReturnValue(
          of({
            id: '9d2c4e9e-15d9-4c77-9d26-48f87dd4fa01',
            sourceCurrency: 'USD',
            targetCurrency: 'BRL',
            rate: 5.25,
            validAt: '2026-07-07T13:00:00Z',
            createdAt: '2026-07-07T13:05:00Z',
          }),
        ),
      getLatest:
        options?.getLatestImpl ??
        vi.fn().mockReturnValue(
          of({
            id: '9d2c4e9e-15d9-4c77-9d26-48f87dd4fa01',
            sourceCurrency: 'USD',
            targetCurrency: 'BRL',
            rate: 5.25,
            validAt: '2026-07-07T13:00:00Z',
            createdAt: '2026-07-07T13:05:00Z',
          }),
        ),
    };

    await TestBed.configureTestingModule({
      imports: [ExchangeRatesPageComponent],
      providers: [
        provideNoopAnimations(),
        {
          provide: ReferenceDataApiService,
          useValue: referenceDataApi,
        },
        {
          provide: ExchangeRateApiService,
          useValue: exchangeRateApi,
        },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(ExchangeRatesPageComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    return { fixture, referenceDataApi, exchangeRateApi };
  }

  it('loads currencies and submits the creation payload to the backend', async () => {
    const create = vi.fn().mockReturnValue(of({}));
    const { fixture, referenceDataApi, exchangeRateApi } = await createComponent({
      createImpl: create,
    });
    const component = fixture.componentInstance;
    const localDateTime = '2026-07-07T13:00';

    component['form'].setValue({
      sourceCurrency: 'USD',
      targetCurrency: 'BRL',
      rate: 5.25,
      validAt: localDateTime,
    });

    component['submit']();

    expect(referenceDataApi.listCurrencies).toHaveBeenCalledTimes(1);
    expect(exchangeRateApi.create).toHaveBeenCalledWith({
      sourceCurrency: 'USD',
      targetCurrency: 'BRL',
      rate: 5.25,
      validAt: new Date(localDateTime).toISOString(),
    });
  });

  it('renders the persisted exchange-rate response after a successful save', async () => {
    const { fixture } = await createComponent();
    const component = fixture.componentInstance;

    component['form'].patchValue({
      sourceCurrency: 'USD',
      targetCurrency: 'BRL',
      rate: 5.25,
      validAt: '2026-07-07T13:00',
    });

    component['submit']();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Taxa cadastrada');
    expect(fixture.nativeElement.textContent).toContain('5.25000000');
    expect(fixture.nativeElement.textContent).toContain('USD -> BRL');
  });

  it('shows structured backend errors when save fails', async () => {
    const { fixture } = await createComponent({
      createImpl: vi.fn().mockReturnValue(
        throwError(
          () =>
            new HttpErrorResponse({
              status: 400,
              statusText: 'Bad Request',
              url: '/api/exchange-rates',
              error: {
                timestamp: '2026-07-07T13:05:00Z',
                status: 400,
                error: 'Bad Request',
                message: 'Source currency and target currency must be different.',
                path: '/api/exchange-rates',
                details: [
                  {
                    field: 'targetCurrency',
                    message: 'Target currency must be different from source currency.',
                  },
                ],
              },
            }),
        ),
      ),
    });
    const component = fixture.componentInstance;

    component['form'].patchValue({
      sourceCurrency: 'USD',
      targetCurrency: 'BRL',
      rate: 5.25,
      validAt: '2026-07-07T13:00',
    });

    component['submit']();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Não foi possível salvar a taxa');
    expect(fixture.nativeElement.textContent).toContain(
      'Source currency and target currency must be different.',
    );
    expect(fixture.nativeElement.textContent).toContain(
      'Target currency must be different from source currency.',
    );
  });

  it('looks up the latest exchange rate for the selected pair', async () => {
    const getLatest = vi.fn().mockReturnValue(
      of({
        id: '9d2c4e9e-15d9-4c77-9d26-48f87dd4fa01',
        sourceCurrency: 'USD',
        targetCurrency: 'BRL',
        rate: 5.25,
        validAt: '2026-07-07T13:00:00Z',
        createdAt: '2026-07-07T13:05:00Z',
      }),
    );
    const { fixture, exchangeRateApi } = await createComponent({
      getLatestImpl: getLatest,
    });
    const component = fixture.componentInstance;

    component['form'].patchValue({
      sourceCurrency: 'USD',
      targetCurrency: 'BRL',
      rate: 5.25,
      validAt: '2026-07-07T13:00',
    });

    component['lookupLatest']();
    fixture.detectChanges();

    expect(exchangeRateApi.getLatest).toHaveBeenCalledWith('USD', 'BRL');
    expect(fixture.nativeElement.textContent).toContain('Última taxa do par');
    expect(fixture.nativeElement.textContent).toContain('5.25000000');
  });
});
