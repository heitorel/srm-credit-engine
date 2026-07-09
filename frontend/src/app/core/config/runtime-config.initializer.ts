import { HttpClient } from '@angular/common/http';
import { inject } from '@angular/core';
import { firstValueFrom, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { RuntimeConfigService } from './runtime-config.service';
import { RuntimeConfig } from '../../models/runtime-config.model';

export function initializeRuntimeConfig(): Promise<void> {
  const http = inject(HttpClient);
  const runtimeConfigService = inject(RuntimeConfigService);

  return firstValueFrom(
    http
      .get<RuntimeConfig>('/assets/config/runtime-config.json')
      .pipe(catchError(() => of({ apiBaseUrl: runtimeConfigService.apiBaseUrl }))),
  ).then((config) => {
    runtimeConfigService.setConfig(config);
  });
}
