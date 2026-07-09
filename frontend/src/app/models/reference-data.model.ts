export interface CurrencyReference {
  readonly code: string;
  readonly name: string;
  readonly decimalPlaces: number;
}

export interface ReceivableTypeReference {
  readonly code: string;
  readonly description: string;
  readonly monthlySpread: number;
}
