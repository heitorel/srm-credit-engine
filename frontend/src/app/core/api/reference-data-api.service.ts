import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import {
  CurrencyReference,
  ReceivableTypeReference,
} from '../../models/reference-data.model';
import { ApiClientService } from './api-client.service';

interface CurrencyReferenceResponse {
  readonly currencies: CurrencyReference[];
}

interface ReceivableTypeReferenceResponse {
  readonly receivableTypes: ReceivableTypeReference[];
}

@Injectable({
  providedIn: 'root',
})
export class ReferenceDataApiService {
  private readonly apiClient = inject(ApiClientService);

  listCurrencies(): Observable<CurrencyReferenceResponse> {
    return this.apiClient.get('/reference-data/currencies');
  }

  listReceivableTypes(): Observable<ReceivableTypeReferenceResponse> {
    return this.apiClient.get('/reference-data/receivable-types');
  }
}
