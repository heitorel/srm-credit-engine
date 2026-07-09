import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  inject,
  signal,
} from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatListModule } from '@angular/material/list';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { finalize } from 'rxjs';

import { ApiErrorResponse } from '../../../models/api-error.model';
import { PageShellComponent } from '../../../shared/components/page-shell/page-shell.component';
import { CurrencyAmountPipe } from '../../../shared/pipes/currency-amount.pipe';
import { normalizeApiError } from '../../../shared/utils/api-error.util';
import { SettlementDetailResponse } from '../models/settlement.models';
import { SettlementApiService } from '../services/settlement-api.service';

@Component({
  selector: 'app-settlement-detail-page',
  imports: [
    CommonModule,
    CurrencyAmountPipe,
    MatButtonModule,
    MatCardModule,
    MatListModule,
    MatProgressSpinnerModule,
    PageShellComponent,
    RouterLink,
  ],
  templateUrl: './settlement-detail-page.component.html',
  styleUrl: './settlement-detail-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SettlementDetailPageComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly settlementApi = inject(SettlementApiService);

  protected readonly loading = signal(true);
  protected readonly apiError = signal<ApiErrorResponse | null>(null);
  protected readonly settlement = signal<SettlementDetailResponse | null>(null);

  ngOnInit(): void {
    const settlementId = this.route.snapshot.paramMap.get('id');

    if (!settlementId) {
      this.loading.set(false);
      this.apiError.set({
        timestamp: new Date().toISOString(),
        status: 404,
        error: 'Not Found',
        message: 'Liquidação não encontrada.',
        path: '',
        details: [],
      });
      return;
    }

    this.loadSettlement(settlementId);
  }

  protected retry(): void {
    const settlementId = this.route.snapshot.paramMap.get('id');

    if (!settlementId) {
      return;
    }

    this.loadSettlement(settlementId);
  }

  private loadSettlement(id: string): void {
    this.loading.set(true);
    this.apiError.set(null);

    this.settlementApi
      .getById(id)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (response) => {
          this.settlement.set(response);
        },
        error: (error: unknown) => {
          this.settlement.set(null);
          this.apiError.set(normalizeApiError(error));
        },
      });
  }
}
