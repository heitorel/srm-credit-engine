import { Routes } from '@angular/router';

import { AppShellLayoutComponent } from './core/layout/app-shell-layout.component';

export const routes: Routes = [
  {
    path: '',
    component: AppShellLayoutComponent,
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'pricing-simulation',
      },
      {
        path: 'pricing-simulation',
        loadComponent: () =>
          import(
            './features/pricing-simulation/pages/pricing-simulation-page.component'
          ).then((module) => module.PricingSimulationPageComponent),
      },
      {
        path: 'settlements',
        loadComponent: () =>
          import('./features/settlements/pages/settlements-page.component').then(
            (module) => module.SettlementsPageComponent,
          ),
      },
      {
        path: 'exchange-rates',
        loadComponent: () =>
          import(
            './features/exchange-rates/pages/exchange-rates-page.component'
          ).then((module) => module.ExchangeRatesPageComponent),
      },
    ],
  },
  {
    path: '**',
    redirectTo: '',
  },
];
