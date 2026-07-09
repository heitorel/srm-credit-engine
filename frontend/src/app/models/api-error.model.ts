export interface ApiErrorDetail {
  readonly field?: string;
  readonly message: string;
}

export interface ApiErrorResponse {
  readonly timestamp: string;
  readonly status: number;
  readonly error: string;
  readonly message: string;
  readonly path: string;
  readonly details?: ApiErrorDetail[];
}
