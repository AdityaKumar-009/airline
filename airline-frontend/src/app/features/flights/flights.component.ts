import { Component, inject, OnInit, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Flight } from '../../core/models';
import { AuthService } from '../../core/auth.service';

@Component({
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './flights.component.html',
  styleUrl: './flights.component.css'
})
export class FlightsComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly fb = inject(FormBuilder);
  readonly auth = inject(AuthService);

  readonly form = this.fb.nonNullable.group({
    origin: [''],
    destination: [''],
    date: ['']
  });
  readonly flights = signal<Flight[]>([]);
  readonly loading = signal(false);
  readonly error = signal('');

  ngOnInit(): void {
    this.search();
  }

  search(): void {
    this.loading.set(true);
    this.error.set('');

    const values = this.form.getRawValue();
    const params: Record<string, string> = {};
    for (const field of ['origin', 'destination', 'date'] as const) {
      const value = values[field].trim();
      if (value) params[field] = value;
    }

    this.http.get<Flight[]>('/api/flights', { params }).subscribe({
      next: flights => {
        this.flights.set(flights);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Unable to load flights. Check backend services.');
        this.loading.set(false);
      }
    });
  }

  formatDate(value: string): string {
    return value.slice(0, 10);
  }

  formatTime(value: string): string {
    return value.slice(11, 16);
  }
}
