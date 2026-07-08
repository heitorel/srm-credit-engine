import { HttpErrorResponse } from '@angular/common/http';

import { ApiErrorResponse } from '../../models/api-error.model';

export function normalizeApiError(error: unknown): ApiErrorResponse {
  if (error instanceof HttpErrorResponse && error.error) {
    const response = error.error as Partial<ApiErrorResponse>;

    if (
      typeof response.message === 'string' &&
      typeof response.status === 'number'
    ) {
      return {
        timestamp: response.timestamp ?? new Date().toISOString(),
        status: response.status,
        error: response.error ?? 'HTTP Error',
        message: response.message,
        path: response.path ?? '',
        details: response.details ?? [],
      };
    }
  }

  return {
    timestamp: new Date().toISOString(),
    status: 0,
    error: 'Unexpected Error',
    message:
      'The request failed before a structured backend response could be displayed.',
    path: '',
    details: [],
  };
}
