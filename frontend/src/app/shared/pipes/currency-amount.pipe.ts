import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'currencyAmount',
  standalone: true,
})
export class CurrencyAmountPipe implements PipeTransform {
  transform(value: number | string | null | undefined, currencyCode: string): string {
    if (value === null || value === undefined || value === '') {
      return '-';
    }

    const amount = typeof value === 'string' ? Number(value) : value;

    if (!Number.isFinite(amount)) {
      return `${currencyCode} ${value}`;
    }

    const formatted = new Intl.NumberFormat('en-US', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(amount);

    return `${currencyCode} ${formatted}`;
  }
}
