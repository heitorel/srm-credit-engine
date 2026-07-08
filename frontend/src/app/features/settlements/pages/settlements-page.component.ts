import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';

import { PageShellComponent } from '../../../shared/components/page-shell/page-shell.component';
import { CurrencyAmountPipe } from '../../../shared/pipes/currency-amount.pipe';
import { SettlementStatementRow } from '../models/settlement-statement.models';

@Component({
  selector: 'app-settlements-page',
  imports: [
    CurrencyAmountPipe,
    DatePipe,
    MatCardModule,
    MatTableModule,
    PageShellComponent,
  ],
  templateUrl: './settlements-page.component.html',
  styleUrl: './settlements-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SettlementsPageComponent {
  protected readonly displayedColumns = [
    'assignor',
    'paymentCurrency',
    'status',
    'totalPaymentValue',
    'settledAt',
  ];

  protected readonly previewRows: SettlementStatementRow[] = [
    {
      settlementId: 'preview-1',
      assignorId: 'assignor-1',
      assignorName: 'ACME Comercio Ltda.',
      assignorDocument: '12345678000199',
      paymentCurrency: 'USD',
      status: 'SETTLED',
      itemCount: 2,
      totalFaceValue: 15000,
      totalPresentValue: 13980.15,
      totalPaymentValue: 2662.89,
      settledAt: '2026-07-07T13:30:00Z',
    },
  ];
}
