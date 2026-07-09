import { of } from 'rxjs';

import { TestBed } from '@angular/core/testing';

import { ApiClientService } from '../../../core/api/api-client.service';
import { SettlementApiService } from './settlement-api.service';

describe('SettlementApiService', () => {
  it('posts the settlement payload to the settlements endpoint', () => {
    const post = vi.fn().mockReturnValue(of({}));

    TestBed.configureTestingModule({
      providers: [
        SettlementApiService,
        {
          provide: ApiClientService,
          useValue: { post, get: vi.fn() },
        },
      ],
    });

    const service = TestBed.inject(SettlementApiService);
    const payload = {
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
          dueDate: '2026-09-07',
        },
      ],
    };

    service.create(payload).subscribe();

    expect(post).toHaveBeenCalledWith('/settlements', payload);
  });

  it('requests the settlement detail by id', () => {
    const get = vi.fn().mockReturnValue(of({}));

    TestBed.configureTestingModule({
      providers: [
        SettlementApiService,
        {
          provide: ApiClientService,
          useValue: { post: vi.fn(), get },
        },
      ],
    });

    const service = TestBed.inject(SettlementApiService);

    service.getById('abc-123').subscribe();

    expect(get).toHaveBeenCalledWith('/settlements/abc-123');
  });
});
