import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-airplane-icon',
  standalone: true,
  templateUrl: './airplane-icon.component.html',
  styleUrl: './airplane-icon.component.css'
})
export class AirplaneIconComponent {
  @Input() direction: 'diagonal' | 'right' = 'diagonal';
}
