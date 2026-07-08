import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatToolbarModule } from '@angular/material/toolbar';

import { NavigationItem } from '../../models/navigation-item.model';

@Component({
  selector: 'app-shell-layout',
  imports: [
    MatButtonModule,
    MatCardModule,
    MatToolbarModule,
    RouterLink,
    RouterLinkActive,
    RouterOutlet,
  ],
  templateUrl: './app-shell-layout.component.html',
  styleUrl: './app-shell-layout.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppShellLayoutComponent {
  protected readonly navigationItems: NavigationItem[] = [
    {
      label: 'Pricing Simulation',
      route: '/pricing-simulation',
      description: 'Frontend shell for POST /api/pricing/simulations.',
    },
    {
      label: 'Settlements',
      route: '/settlements',
      description: 'Foundation for statement filters and server-side pagination.',
    },
    {
      label: 'Exchange Rates',
      route: '/exchange-rates',
      description: 'Base flow for POST /api/exchange-rates and latest lookups.',
    },
  ];
}
