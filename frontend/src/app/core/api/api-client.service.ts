import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { RuntimeConfigService } from '../config/runtime-config.service';

type QueryValue = string | number | boolean | null | undefined;
type QueryParams = Record<string, QueryValue> | object;

@Injectable({
  providedIn: 'root',
})
export class ApiClientService {
  private readonly http = inject(HttpClient);
  private readonly runtimeConfig = inject(RuntimeConfigService);

  get<T>(path: string, query?: QueryParams): Observable<T> {
    return this.http.get<T>(this.buildUrl(path), {
      params: this.buildParams(query),
    });
  }

  post<TResponse, TRequest>(path: string, body: TRequest): Observable<TResponse> {
    return this.http.post<TResponse>(this.buildUrl(path), body);
  }

  private buildUrl(path: string): string {
    return `${this.runtimeConfig.apiBaseUrl}${path}`;
  }

  private buildParams(query?: QueryParams): HttpParams {
    if (!query) {
      return new HttpParams();
    }

    return Object.entries(query as Record<string, QueryValue>).reduce((params, [key, value]) => {
      if (value === null || value === undefined || value === '') {
        return params;
      }

      return params.set(key, String(value));
    }, new HttpParams());
  }
}
