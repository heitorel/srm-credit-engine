import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatToolbarModule } from '@angular/material/toolbar';

import { NavigationItem } from '../../models/navigation-item.model';

@Component({
  selector: 'app-shell-layout',
  imports: [
    MatButtonModule,
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
      label: 'Simulação',
      route: '/pricing-simulation',
    },
    {
      label: 'Nova liquidação',
      route: '/settlements/new',
    },
    {
      label: 'Extrato',
      route: '/settlements',
    },
    {
      label: 'Câmbio',
      route: '/exchange-rates',
    },
  ];
}
