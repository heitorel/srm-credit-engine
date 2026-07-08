import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiClientService } from '../../../core/api/api-client.service';
import {
  SettlementStatementFilters,
  SettlementStatementResponse,
} from '../models/settlement-statement.models';

@Injectable({
  providedIn: 'root',
})
export class SettlementStatementApiService {
  private readonly apiClient = inject(ApiClientService);

  getStatement(
    filters: SettlementStatementFilters,
  ): Observable<SettlementStatementResponse> {
    return this.apiClient.get('/settlements/statement', filters);
  }
}
