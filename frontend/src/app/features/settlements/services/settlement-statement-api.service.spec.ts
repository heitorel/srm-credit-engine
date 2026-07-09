import { of } from 'rxjs';

import { TestBed } from '@angular/core/testing';

import { ApiClientService } from '../../../core/api/api-client.service';
import { SettlementStatementApiService } from './settlement-statement-api.service';

describe('SettlementStatementApiService', () => {
  it('requests the paginated statement endpoint with query filters', () => {
    const get = vi.fn().mockReturnValue(of({}));

    TestBed.configureTestingModule({
      providers: [
        SettlementStatementApiService,
        {
          provide: ApiClientService,
          useValue: { get },
        },
      ],
    });

    const service = TestBed.inject(SettlementStatementApiService);
    const filters = {
      from: '2026-07-01',
      to: '2026-07-31',
      paymentCurrency: 'USD',
      status: 'SETTLED',
      page: 1,
      size: 20,
      sort: 'settledAt,desc',
    };

    service.getStatement(filters).subscribe();

    expect(get).toHaveBeenCalledWith('/settlements/statement', filters);
  });
});
