export interface PricingSimulationRequest {
  readonly faceValue: number;
  readonly sourceCurrency: string;
  readonly paymentCurrency: string;
  readonly baseRate?: number;
  readonly receivableType: string;
  readonly dueDate: string;
}

export interface PricingSimulationResponse {
  readonly faceValue: number;
  readonly sourceCurrency: string;
  readonly paymentCurrency: string;
  readonly presentValueInSourceCurrency: number;
  readonly netPaymentValue: number;
  readonly discountValue: number;
  readonly baseRate: number;
  readonly spread: number;
  readonly termInMonths: number;
  readonly exchangeRate: number | null;
  readonly calculatedAt: string;
}
