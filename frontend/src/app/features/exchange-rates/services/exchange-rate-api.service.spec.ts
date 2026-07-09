import { of } from 'rxjs';

import { TestBed } from '@angular/core/testing';

import { ApiClientService } from '../../../core/api/api-client.service';
import { ExchangeRateApiService } from './exchange-rate-api.service';

describe('ExchangeRateApiService', () => {
  it('posts the exchange-rate payload to the creation endpoint', () => {
    const post = vi.fn().mockReturnValue(of({}));

    TestBed.configureTestingModule({
      providers: [
        ExchangeRateApiService,
        {
          provide: ApiClientService,
          useValue: { post, get: vi.fn() },
        },
      ],
    });

    const service = TestBed.inject(ExchangeRateApiService);
    const payload = {
      sourceCurrency: 'USD',
      targetCurrency: 'BRL',
      rate: 5.25,
      validAt: '2026-07-07T13:00:00.000Z',
    };

    service.create(payload).subscribe();

    expect(post).toHaveBeenCalledWith('/exchange-rates', payload);
  });

  it('requests the latest exchange rate for the exact pair direction', () => {
    const get = vi.fn().mockReturnValue(of({}));

    TestBed.configureTestingModule({
      providers: [
        ExchangeRateApiService,
        {
          provide: ApiClientService,
          useValue: { get, post: vi.fn() },
        },
      ],
    });

    const service = TestBed.inject(ExchangeRateApiService);

    service.getLatest('USD', 'BRL').subscribe();

    expect(get).toHaveBeenCalledWith('/exchange-rates/latest', {
      sourceCurrency: 'USD',
      targetCurrency: 'BRL',
    });
  });
});
