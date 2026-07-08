import { ChangeDetectionStrategy, Component } from '@angular/core';
import { MatCardModule } from '@angular/material/card';

import { PageShellComponent } from '../../../shared/components/page-shell/page-shell.component';

@Component({
  selector: 'app-exchange-rates-page',
  imports: [MatCardModule, PageShellComponent],
  templateUrl: './exchange-rates-page.component.html',
  styleUrl: './exchange-rates-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ExchangeRatesPageComponent {}
