import { Component, inject, OnInit, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { FormBuilder, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { AdminBooking, AdminCustomer, Flight } from '../../core/models';
import { ConfirmModalComponent } from '../../shared/confirm-modal.component';

type AdminSection = 'flights' | 'customers' | 'bookings';

@Component({
  standalone: true,
  imports: [ReactiveFormsModule, ConfirmModalComponent],
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.css'
})
export class AdminComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly fb = inject(FormBuilder);

  readonly section = signal<AdminSection>('flights');
  readonly flights = signal<Flight[]>([]);
  readonly customers = signal<AdminCustomer[]>([]);
  readonly bookings = signal<AdminBooking[]>([]);

  readonly flightLoading = signal(true);
  readonly customerLoading = signal(false);
  readonly bookingLoading = signal(false);
  readonly error = signal('');
  readonly success = signal('');
  readonly customerError = signal('');
  readonly bookingError = signal('');
  readonly pendingFlight = signal<Flight | null>(null);

  readonly customerSearch = new FormControl('', { nonNullable: true });
  readonly bookingFlightId = new FormControl('', { nonNullable: true });
  readonly bookingStatus = new FormControl('ALL', { nonNullable: true });

  readonly form = this.fb.nonNullable.group({
    flightNumber: ['', Validators.required],
    origin: ['', Validators.required],
    destination: ['', Validators.required],
    departureTime: ['', Validators.required],
    arrivalTime: ['', Validators.required],
    price: [4500, [Validators.required, Validators.min(0.01)]],
    capacity: [30, [Validators.required, Validators.min(1), Validators.max(300)]]
  });

  ngOnInit(): void {
    this.refresh();
    this.searchCustomers();
    this.filterBookings();
  }

  refresh(): void {
    this.flightLoading.set(true);
    this.http.get<Flight[]>('/api/flights/manage').subscribe({
      next: flights => {
        this.flights.set(flights);
        this.flightLoading.set(false);
      },
      error: () => {
        this.error.set('Cannot load flights.');
        this.flightLoading.set(false);
      }
    });
  }

  searchCustomers(): void {
    this.customerError.set('');
    this.customerLoading.set(true);
    const params = new HttpParams().set('search', this.customerSearch.value.trim());

    this.http.get<AdminCustomer[]>('/api/auth/admin/customers', { params }).subscribe({
      next: customers => {
        this.customers.set(customers);
        this.customerLoading.set(false);
      },
      error: () => {
        this.customerError.set('Cannot load customers.');
        this.customerLoading.set(false);
      }
    });
  }

  filterBookings(): void {
    this.bookingError.set('');
    const rawFlightId = String(this.bookingFlightId.value).trim();

    if (rawFlightId && !/^[1-9][0-9]*$/.test(rawFlightId)) {
      this.bookingError.set('Enter a positive whole flight ID.');
      return;
    }

    let params = new HttpParams();
    if (rawFlightId) params = params.set('flightId', rawFlightId);
    if (this.bookingStatus.value !== 'ALL') {
      params = params.set('status', this.bookingStatus.value);
    }

    this.bookingLoading.set(true);
    this.http.get<AdminBooking[]>('/api/bookings/admin', { params }).subscribe({
      next: bookings => {
        this.bookings.set(bookings);
        this.bookingLoading.set(false);
      },
      error: () => {
        this.bookingError.set('Cannot load bookings.');
        this.bookingLoading.set(false);
      }
    });
  }

  clearBookingFilters(): void {
    this.bookingFlightId.setValue('');
    this.bookingStatus.setValue('ALL');
    this.filterBookings();
  }

  add(): void {
    this.error.set('');
    this.success.set('');

    if (this.form.invalid) {
      this.error.set('Complete all fields correctly.');
      return;
    }

    const flight = this.form.getRawValue();
    const sameCity = flight.origin.trim().toLowerCase() === flight.destination.trim().toLowerCase();
    if (sameCity || flight.arrivalTime <= flight.departureTime) {
      this.error.set('Check cities and arrival time.');
      return;
    }

    this.http.post('/api/flights', flight).subscribe({
      next: () => {
        this.success.set('Flight created.');
        this.form.reset();
        this.refresh();
      },
      error: response => this.error.set(response.error?.message || 'Could not create flight.')
    });
  }

  requestToggle(flight: Flight): void {
    this.pendingFlight.set(flight);
  }

  toggle(flight: Flight): void {
    this.pendingFlight.set(null);
    this.error.set('');
    const status = flight.status === 'OPEN' ? 'CLOSED' : 'OPEN';

    this.http.patch(`/api/flights/${flight.id}`, { status }).subscribe({
      next: () => this.refresh(),
      error: response => this.error.set(response.error?.message || 'Could not update flight.')
    });
  }

  togglePrompt(flight: Flight): string {
    if (flight.status === 'OPEN') {
      return `Close sales for ${flight.flightNumber}? New customers will not be able to book it.`;
    }
    return `Open sales for ${flight.flightNumber}?`;
  }
}
