import { Component, inject, OnInit, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Flight } from '../../core/models';
import { ConfirmModalComponent } from '../../shared/confirm-modal.component';

@Component({
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, ConfirmModalComponent],
  templateUrl: './book.component.html',
  styleUrl: './book.component.css'
})
export class BookComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  readonly flight = signal<Flight | null>(null);
  readonly occupied = signal<number[]>([]);
  readonly busy = signal(false);
  readonly error = signal('');
  readonly pendingSeat = signal<number | null>(null);

  readonly form = this.fb.nonNullable.group({
    seatNumber: [1, [Validators.required, Validators.min(1)]]
  });

  ngOnInit(): void {
    const flightId = Number(this.route.snapshot.paramMap.get('id'));

    this.http.get<Flight>(`/api/flights/${flightId}`).subscribe({
      next: flight => {
        this.flight.set(flight);
        this.form.controls.seatNumber.addValidators(Validators.max(flight.capacity));
        this.form.controls.seatNumber.updateValueAndValidity();
      },
      error: () => this.error.set('Could not load flight.')
    });

    this.loadOccupiedSeats(flightId);
  }

  requestBooking(): void {
    const flight = this.flight();
    const seat = this.form.getRawValue().seatNumber;

    if (!flight || this.form.invalid) {
      this.error.set('Enter a valid seat number.');
      return;
    }

    if (this.occupied().includes(seat)) {
      this.error.set('Seat is already occupied. Choose another.');
      return;
    }

    this.error.set('');
    this.pendingSeat.set(seat);
  }

  bookingPrompt(): string {
    const flight = this.flight();
    return `Reserve seat ${this.pendingSeat()} on ${flight?.flightNumber} for ₹${flight?.price}?`;
  }

  book(): void {
    const flight = this.flight();
    const seat = this.pendingSeat();
    if (!flight || seat === null) return;

    this.pendingSeat.set(null);
    this.busy.set(true);
    this.error.set('');

    this.http.post('/api/bookings', { flightId: flight.id, seatNumber: seat }).subscribe({
      next: () => this.router.navigate(['/my-bookings']),
      error: response => {
        this.busy.set(false);
        this.error.set(response.status === 409
          ? 'Someone already booked that seat. Please choose another.'
          : response.error?.message || 'Could not complete booking.');
        this.loadOccupiedSeats(flight.id);
      }
    });
  }

  private loadOccupiedSeats(flightId: number): void {
    this.http.get<number[]>('/api/bookings/seats', { params: { flightId } }).subscribe({
      next: seats => this.occupied.set(seats),
      error: () => this.error.set('Could not check seats.')
    });
  }
}
