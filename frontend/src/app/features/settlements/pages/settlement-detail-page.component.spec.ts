import { HttpErrorResponse } from '@angular/common/http';
import { of, throwError } from 'rxjs';

import { TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { provideRouter } from '@angular/router';

import { SettlementApiService } from '../services/settlement-api.service';
import { SettlementDetailPageComponent } from './settlement-detail-page.component';

describe('SettlementDetailPageComponent', () => {
  async function createComponent(options?: {
    getByIdImpl?: ReturnType<typeof vi.fn>;
    id?: string | null;
  }) {
    const settlementApi = {
      create: vi.fn(),
      getById:
        options?.getByIdImpl ??
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
            items: [
              {
                id: 'ff5579e1-b0a9-4df2-974e-82f2d598c003',
                receivableId: 'fa4b2781-c6c1-4041-b564-d75932e421c0',
                externalReference: 'NF-1001',
                receivableType: 'MERCANTILE_DUPLICATE',
                faceValue: 10000,
                sourceCurrency: 'BRL',
                paymentCurrency: 'USD',
                baseRate: 0.01,
                spread: 0.015,
                termInMonths: 2.06666667,
                presentValueInSourceCurrency: 9509.18,
                discountValue: 490.82,
                paymentValue: 1811.27,
                exchangeRate: 5.25,
                calculatedAt: '2026-07-07T13:30:00Z',
              },
            ],
          }),
        ),
    };

    await TestBed.configureTestingModule({
      imports: [SettlementDetailPageComponent],
      providers: [
        provideNoopAnimations(),
        provideRouter([]),
        { provide: SettlementApiService, useValue: settlementApi },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: vi.fn().mockReturnValue(options?.id ?? '7b4b65ab-c30d-47f8-8356-0f0c2ab2e2df'),
              },
            },
          },
        },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(SettlementDetailPageComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    return { fixture, settlementApi };
  }

  it('loads and renders the persisted settlement detail', async () => {
    const { fixture, settlementApi } = await createComponent();

    expect(settlementApi.getById).toHaveBeenCalledWith(
      '7b4b65ab-c30d-47f8-8356-0f0c2ab2e2df',
    );
    expect(fixture.nativeElement.textContent).toContain('Detalhe da liquidação');
    expect(fixture.nativeElement.textContent).toContain('ACME Comercio Ltda.');
    expect(fixture.nativeElement.textContent).toContain('NF-1001');
    expect(fixture.nativeElement.textContent).toContain('USD 1,811.27');
  });

  it('shows structured backend errors when the detail request fails', async () => {
    const { fixture } = await createComponent({
      getByIdImpl: vi.fn().mockReturnValue(
        throwError(
          () =>
            new HttpErrorResponse({
              status: 404,
              statusText: 'Not Found',
              url: '/api/settlements/123',
              error: {
                timestamp: '2026-07-07T13:35:00Z',
                status: 404,
                error: 'Not Found',
                message: 'Settlement not found.',
                path: '/api/settlements/123',
                details: [],
              },
            }),
        ),
      ),
    });

    expect(fixture.nativeElement.textContent).toContain(
      'Não foi possível carregar a liquidação',
    );
    expect(fixture.nativeElement.textContent).toContain('Settlement not found.');
  });
});
