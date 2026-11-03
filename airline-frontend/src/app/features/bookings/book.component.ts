import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Booking, Flight } from '../../core/models';
import { ConfirmModalComponent } from '../../shared/confirm-modal.component';

interface DraftPassenger {
  seatNumber: number;
  fullName: string;
  age: number;
}

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
  readonly passengers = signal<DraftPassenger[]>([]);
  readonly busy = signal(false);
  readonly error = signal('');
  readonly showConfirmation = signal(false);
  readonly seatLetters = ['A', 'B', 'C', '', 'D', 'E', 'F'];
  readonly rows = computed(() => Array.from(
    {length: Math.ceil((this.flight()?.capacity ?? 0) / 6)}, (_, i) => i + 1));
  readonly total = computed(() => (this.flight()?.price ?? 0) * this.passengers().length);
  readonly form = this.fb.nonNullable.group({
    contactEmail: ['', [Validators.required, Validators.email]],
    contactPhone: ['', [Validators.pattern(/^\+?[0-9]{10,15}$/)]]
  });

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isInteger(id) || id < 1) {
      this.error.set('Invalid flight reference.');
      return;
    }
    this.http.get<Flight>('/api/flights/' + id).subscribe({
      next: flight => {
        this.flight.set(flight);
        if (flight.status !== 'OPEN' ||
            !['SCHEDULED', 'DELAYED'].includes(flight.operationalStatus)) {
          this.error.set('This flight is no longer open for reservations.');
        }
      },
      error: () => this.error.set('Could not load flight details.')
    });
    this.loadOccupiedSeats(id);
  }

  seatNumber(row: number, letter: string): number {
    return (row - 1) * 6 + 'ABCDEF'.indexOf(letter) + 1;
  }
  seatCode(n: number): string {
    return Math.ceil(n / 6) + 'ABCDEF'.charAt((n - 1) % 6);
  }
  isSelected(n: number): boolean { return this.passengers().some(p => p.seatNumber === n); }
  isOccupied(n: number): boolean { return this.occupied().includes(n); }

  toggleSeat(n: number): void {
    const f = this.flight();
    if (!f || this.busy() || n < 1 || n > f.capacity || this.isOccupied(n) ||
        f.status !== 'OPEN' || !['SCHEDULED', 'DELAYED'].includes(f.operationalStatus)) return;
    if (this.isSelected(n)) {
      this.passengers.update(list => list.filter(p => p.seatNumber !== n));
    } else if (this.passengers().length < 4) {
      this.passengers.update(list => [...list, {seatNumber: n, fullName: '', age: 18}]);
    } else {
      this.error.set('You can reserve up to four seats in one booking.');
      return;
    }
    this.error.set('');
  }

  changeName(seat: number, value: string): void {
    this.passengers.update(items => items.map(p =>
      p.seatNumber === seat ? {...p, fullName: value} : p));
  }
  changeAge(seat: number, value: string): void {
    this.passengers.update(items => items.map(p =>
      p.seatNumber === seat ? {...p, age: Number(value)} : p));
  }

  requestBooking(): void {
    if (!this.passengers().length) {
      this.error.set('Select at least one available seat.');
      return;
    }
    if (this.form.invalid || this.passengers().some(p =>
      !p.fullName.trim() || p.fullName.trim().length > 100 ||
      !Number.isInteger(p.age) || p.age < 1 || p.age > 120)) {
      this.error.set('Enter a contact email, each passenger name and a valid age (1–120).');
      this.form.markAllAsTouched();
      return;
    }
    this.error.set('');
    this.showConfirmation.set(true);
  }

  bookingPrompt(): string {
    return 'Reserve ' + this.passengers().length + ' seat(s) for ₹' + this.total()
      + '? Seat(s): ' + this.passengers().map(p => this.seatCode(p.seatNumber)).join(', ') + '.';
  }

  book(): void {
    const flight = this.flight();
    if (!flight || this.busy()) return;
    this.showConfirmation.set(false);
    this.busy.set(true);
    const details = this.form.getRawValue();
    const payload = {
      flightId: flight.id,
      contactEmail: details.contactEmail,
      contactPhone: details.contactPhone,
      passengers: this.passengers().map(p => ({
        fullName: p.fullName.trim(), age: p.age, seatNumber: p.seatNumber
      }))
    };
    this.http.post<Booking>('/api/bookings', payload).subscribe({
      next: booking => this.router.navigate(['/bookings', booking.id]),
      error: response => {
        this.busy.set(false);
        this.error.set(response.status === 409
          ? 'A selected seat was booked by somebody else. Please choose again.'
          : response.error?.message || 'Could not complete reservation.');
        this.loadOccupiedSeats(flight.id);
      }
    });
  }

  private loadOccupiedSeats(id: number): void {
    this.http.get<number[]>('/api/bookings/seats', {params: {flightId: id}}).subscribe({
      next: seats => {
        this.occupied.set(seats);
        this.passengers.update(list => list.filter(p => !seats.includes(p.seatNumber)));
      },
      error: () => this.error.set('Could not check current seat availability.')
    });
  }
}

