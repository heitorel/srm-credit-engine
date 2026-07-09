export interface SettlementAssignor {
  readonly id?: string;
  readonly name: string;
  readonly document: string | null;
}

export interface SettlementItemSnapshot {
  readonly id: string;
  readonly receivableId: string;
  readonly externalReference: string;
  readonly receivableType: string;
  readonly faceValue: number;
  readonly sourceCurrency: string;
  readonly paymentCurrency: string;
  readonly baseRate: number;
  readonly spread: number;
  readonly termInMonths: number;
  readonly presentValueInSourceCurrency: number;
  readonly discountValue: number;
  readonly paymentValue: number;
  readonly exchangeRate: number | null;
  readonly calculatedAt: string;
}

export interface SettlementDetailResponse {
  readonly id: string;
  readonly assignor: SettlementAssignor;
  readonly sourceCurrency: string;
  readonly paymentCurrency: string;
  readonly status: string;
  readonly baseRate: number;
  readonly itemCount: number;
  readonly totalFaceValue: number;
  readonly totalPresentValue: number;
  readonly totalPaymentValue: number;
  readonly settledAt: string;
  readonly items: SettlementItemSnapshot[];
}

export interface CreateSettlementAssignorRequest {
  readonly name: string;
  readonly document?: string;
}

export interface CreateSettlementReceivableRequest {
  readonly externalReference: string;
  readonly faceValue: number;
  readonly sourceCurrency: string;
  readonly receivableType: string;
  readonly dueDate: string;
}

export interface CreateSettlementRequest {
  readonly assignor: CreateSettlementAssignorRequest;
  readonly paymentCurrency: string;
  readonly baseRate?: number;
  readonly receivables: CreateSettlementReceivableRequest[];
}
