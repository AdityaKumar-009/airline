import { Component } from '@angular/core';
import { AirplaneIconComponent } from './airplane-icon.component';

@Component({
  selector: 'app-flight-path',
  standalone: true,
  imports: [AirplaneIconComponent],
  templateUrl: './flight-path.component.html',
  styleUrl: './flight-path.component.css'
})
export class FlightPathComponent {}
