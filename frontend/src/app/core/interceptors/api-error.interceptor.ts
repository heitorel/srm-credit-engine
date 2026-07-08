import { HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

import { normalizeApiError } from '../../shared/utils/api-error.util';

export const apiErrorInterceptor: HttpInterceptorFn = (request, next) =>
  next(request).pipe(
    catchError((error) => throwError(() => normalizeApiError(error))),
  );
