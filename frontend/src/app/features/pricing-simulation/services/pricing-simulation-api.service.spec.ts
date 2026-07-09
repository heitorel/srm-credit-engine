import { of } from 'rxjs';

import { TestBed } from '@angular/core/testing';

import { ApiClientService } from '../../../core/api/api-client.service';
import { PricingSimulationApiService } from './pricing-simulation-api.service';

describe('PricingSimulationApiService', () => {
  it('posts the simulation payload to the pricing endpoint', () => {
    const post = vi.fn().mockReturnValue(of({}));

    TestBed.configureTestingModule({
      providers: [
        PricingSimulationApiService,
        {
          provide: ApiClientService,
          useValue: { post },
        },
      ],
    });

    const service = TestBed.inject(PricingSimulationApiService);
    const payload = {
      faceValue: 10000,
      sourceCurrency: 'BRL',
      paymentCurrency: 'USD',
      baseRate: 0.01,
      receivableType: 'MERCANTILE_DUPLICATE',
      dueDate: '2026-09-07',
    };

    service.simulate(payload).subscribe();

    expect(post).toHaveBeenCalledWith('/pricing/simulations', payload);
  });
});
