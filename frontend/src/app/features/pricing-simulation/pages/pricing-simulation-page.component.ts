import { ChangeDetectionStrategy, Component } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatListModule } from '@angular/material/list';

import { PageShellComponent } from '../../../shared/components/page-shell/page-shell.component';

@Component({
  selector: 'app-pricing-simulation-page',
  imports: [MatCardModule, MatListModule, PageShellComponent],
  templateUrl: './pricing-simulation-page.component.html',
  styleUrl: './pricing-simulation-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PricingSimulationPageComponent {
  protected readonly integrationPoints = [
    'Typed request/response models mirror POST /api/pricing/simulations.',
    'Reference-data service is ready to populate currencies and receivable types.',
    'Official present-value calculation remains exclusively in the backend.',
  ];
}
