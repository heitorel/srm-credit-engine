import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { MatChipsModule } from '@angular/material/chips';

@Component({
  selector: 'app-page-shell',
  imports: [MatChipsModule],
  templateUrl: './page-shell.component.html',
  styleUrl: './page-shell.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PageShellComponent {
  readonly title = input.required<string>();
  readonly subtitle = input.required<string>();
  readonly status = input<string>('Ready');
}
