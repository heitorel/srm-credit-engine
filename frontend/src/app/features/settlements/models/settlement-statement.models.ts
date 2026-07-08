import { PageResponse } from '../../../models/page-response.model';

export interface SettlementStatementFilters {
  readonly from?: string;
  readonly to?: string;
  readonly assignorId?: string;
  readonly assignorDocument?: string;
  readonly paymentCurrency?: string;
  readonly sourceCurrency?: string;
  readonly receivableType?: string;
  readonly status?: string;
  readonly page?: number;
  readonly size?: number;
  readonly sort?: string;
}

export interface SettlementStatementRow {
  readonly settlementId: string;
  readonly assignorId: string;
  readonly assignorName: string;
  readonly assignorDocument: string | null;
  readonly paymentCurrency: string;
  readonly status: string;
  readonly itemCount: number;
  readonly totalFaceValue: number;
  readonly totalPresentValue: number;
  readonly totalPaymentValue: number;
  readonly settledAt: string;
}

export type SettlementStatementResponse = PageResponse<SettlementStatementRow>;
