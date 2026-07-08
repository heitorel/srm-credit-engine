import { Injectable, signal } from '@angular/core';

import { environment } from '../../../environments/environment';
import { RuntimeConfig } from '../../models/runtime-config.model';

const DEFAULT_CONFIG: RuntimeConfig = {
  apiBaseUrl: environment.apiBaseUrl,
};

@Injectable({
  providedIn: 'root',
})
export class RuntimeConfigService {
  private readonly configState = signal<RuntimeConfig>(DEFAULT_CONFIG);

  readonly config = this.configState.asReadonly();

  get apiBaseUrl(): string {
    return this.configState().apiBaseUrl;
  }

  setConfig(config: RuntimeConfig): void {
    this.configState.set({
      apiBaseUrl: config.apiBaseUrl || DEFAULT_CONFIG.apiBaseUrl,
    });
  }
}
