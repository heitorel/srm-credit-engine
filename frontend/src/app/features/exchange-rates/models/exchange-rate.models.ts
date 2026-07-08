export interface CreateExchangeRateRequest {
  readonly sourceCurrency: string;
  readonly targetCurrency: string;
  readonly rate: number;
  readonly validAt: string;
}

export interface ExchangeRateResponse {
  readonly id: string;
  readonly sourceCurrency: string;
  readonly targetCurrency: string;
  readonly rate: number;
  readonly validAt: string;
  readonly createdAt: string;
}
