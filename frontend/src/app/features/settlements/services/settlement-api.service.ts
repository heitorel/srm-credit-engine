import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiClientService } from '../../../core/api/api-client.service';
import {
  CreateSettlementRequest,
  SettlementDetailResponse,
} from '../models/settlement.models';

@Injectable({
  providedIn: 'root',
})
export class SettlementApiService {
  private readonly apiClient = inject(ApiClientService);

  create(
    payload: CreateSettlementRequest,
  ): Observable<SettlementDetailResponse> {
    return this.apiClient.post('/settlements', payload);
  }

  getById(id: string): Observable<SettlementDetailResponse> {
    return this.apiClient.get(`/settlements/${id}`);
  }
}
