import { Component, inject, OnInit, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { Booking } from '../../core/models';
import { ConfirmModalComponent } from '../../shared/confirm-modal.component';

@Component({
  standalone: true,
  imports: [RouterLink, ConfirmModalComponent],
  templateUrl: './my-bookings.component.html',
  styleUrl: './my-bookings.component.css'
})
export class MyBookingsComponent implements OnInit {
  private readonly http = inject(HttpClient);
  readonly bookings = signal<Booking[]>([]);
  readonly loading = signal(true);
  readonly error = signal('');
  readonly pendingCancellation = signal<Booking | null>(null);
  ngOnInit(): void { this.refresh(); }

  refresh(): void {
    this.loading.set(true);
    this.error.set('');
    this.http.get<Booking[]>('/api/bookings/me').subscribe({
      next: values => { this.bookings.set(values); this.loading.set(false); },
      error: () => { this.error.set('Could not load bookings.'); this.loading.set(false); }
    });
  }
  canCancel(booking: Booking): boolean {
    return booking.status === 'CONFIRMED' &&
      (!booking.departureTime || new Date(booking.departureTime + 'Z').getTime() > Date.now());
  }
  requestCancellation(booking: Booking): void { this.pendingCancellation.set(booking); }
  cancel(booking: Booking): void {
    this.pendingCancellation.set(null);
    this.http.patch('/api/bookings/' + booking.id + '/cancel', {}).subscribe({
      next: () => this.refresh(),
      error: response => this.error.set(response.error?.message || 'Cancellation failed.')
    });
  }
}

