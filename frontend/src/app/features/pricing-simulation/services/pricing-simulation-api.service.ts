import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiClientService } from '../../../core/api/api-client.service';
import {
  PricingSimulationRequest,
  PricingSimulationResponse,
} from '../models/pricing-simulation.models';

@Injectable({
  providedIn: 'root',
})
export class PricingSimulationApiService {
  private readonly apiClient = inject(ApiClientService);

  simulate(
    payload: PricingSimulationRequest,
  ): Observable<PricingSimulationResponse> {
    return this.apiClient.post('/pricing/simulations', payload);
  }
}
