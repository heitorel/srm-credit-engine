import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiClientService } from '../../../core/api/api-client.service';
import {
  CreateExchangeRateRequest,
  ExchangeRateResponse,
} from '../models/exchange-rate.models';

@Injectable({
  providedIn: 'root',
})
export class ExchangeRateApiService {
  private readonly apiClient = inject(ApiClientService);

  create(payload: CreateExchangeRateRequest): Observable<ExchangeRateResponse> {
    return this.apiClient.post('/exchange-rates', payload);
  }

  getLatest(
    sourceCurrency: string,
    targetCurrency: string,
  ): Observable<ExchangeRateResponse> {
    return this.apiClient.get('/exchange-rates/latest', {
      sourceCurrency,
      targetCurrency,
    });
  }
}
