import { Component, inject, OnInit, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Booking, Flight, OperationalStatus } from '../../core/models';
import { FlightPathComponent } from '../../shared/flight-path.component';

@Component({
  standalone: true,
  imports: [RouterLink, FlightPathComponent],
  templateUrl: './booking-details.component.html',
  styleUrl: './booking-details.component.css'
})
export class BookingDetailsComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly route = inject(ActivatedRoute);
  readonly booking = signal<Booking | null>(null);
  readonly operation = signal<OperationalStatus | null>(null);
  readonly loading = signal(true);
  readonly error = signal('');

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isInteger(id) || id < 1) {
      this.loading.set(false);
      this.error.set('Invalid booking reference.');
      return;
    }
    this.http.get<Booking[]>('/api/bookings/me').subscribe({
      next: bookings => {
        const booking = bookings.find(b => b.id === id);
        if (!booking) this.error.set('Booking not found in your account.');
        else {
          this.booking.set(booking);
          this.http.get<Flight>('/api/flights/' + booking.flightId).subscribe({
            next: flight => this.operation.set(flight.operationalStatus),
            error: () => {}
          });
        }
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Could not retrieve your booking.');
        this.loading.set(false);
      }
    });
  }
  print(): void { window.print(); }
}

