import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { FormBuilder, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  AdminBooking, AdminCustomer, Airport, Flight, FlightActivity, FlightStatus, OperationalStatus
} from '../../core/models';
import { ConfirmModalComponent } from '../../shared/confirm-modal.component';

type AdminSection = 'overview' | 'flights' | 'customers' | 'bookings' | 'activity';

@Component({
  standalone: true,
  imports: [ReactiveFormsModule, ConfirmModalComponent],
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.css'
})
export class AdminComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly fb = inject(FormBuilder);
  readonly section = signal<AdminSection>('overview');
  readonly flights = signal<Flight[]>([]);
  readonly airports = signal<Airport[]>([]);
  readonly customers = signal<AdminCustomer[]>([]);
  readonly bookings = signal<AdminBooking[]>([]);
  readonly allBookings = signal<AdminBooking[]>([]);
  readonly activities = signal<FlightActivity[]>([]);
  readonly flightLoading = signal(false);
  readonly customerLoading = signal(false);
  readonly bookingLoading = signal(false);
  readonly error = signal('');
  readonly success = signal('');
  readonly customerError = signal('');
  readonly bookingError = signal('');
  readonly editFlight = signal<Flight | null>(null);
  readonly pendingFlight = signal<Flight | null>(null);
  readonly pendingCancellation = signal<Flight | null>(null);
  readonly activeReservations = computed(() =>
    this.allBookings().filter(b => b.status === 'CONFIRMED').length);
  readonly seatsReserved = computed(() =>
    this.allBookings().filter(b => b.status === 'CONFIRMED')
      .reduce((sum, b) => sum + b.passengerCount, 0));
  readonly bookedValue = computed(() =>
    this.allBookings().filter(b => b.status === 'CONFIRMED')
      .reduce((sum, b) => sum + Number(b.totalFare), 0));
  readonly upcomingFlights = computed(() => this.flights()
    .filter(f => f.status === 'OPEN' &&
      (f.operationalStatus === 'SCHEDULED' || f.operationalStatus === 'DELAYED') &&
      new Date(f.departureTime + 'Z').getTime() > Date.now()).length);

  readonly customerSearch = new FormControl('', {nonNullable: true});
  readonly bookingFlightId = new FormControl('', {nonNullable: true});
  readonly bookingStatus = new FormControl('ALL', {nonNullable: true});

  readonly form = this.fb.nonNullable.group({
    flightNumber: ['', Validators.required],
    originCode: ['', Validators.required],
    destinationCode: ['', Validators.required],
    departureTime: ['', Validators.required],
    arrivalTime: ['', Validators.required],
    price: [4500, [Validators.required, Validators.min(0.01)]],
    capacity: [30, [Validators.required, Validators.min(1), Validators.max(300)]],
    aircraftType: ['A320', Validators.required]
  });
  readonly editForm = this.fb.nonNullable.group({
    price: [4500, [Validators.required, Validators.min(0.01)]],
    status: ['OPEN', Validators.required],
    operationalStatus: ['SCHEDULED', Validators.required],
    departureTime: ['', Validators.required],
    arrivalTime: ['', Validators.required],
    aircraftType: ['A320', Validators.required]
  });

  ngOnInit(): void {
    this.refresh();
    this.searchCustomers();
    this.filterBookings();
    this.loadDashboard();
    this.loadActivity();
    this.http.get<Airport[]>('/api/flights/airports').subscribe({
      next: result => this.airports.set(result),
      error: () => this.error.set('Could not load airport list.')
    });
  }

  refresh(): void {
    this.flightLoading.set(true);
    this.http.get<Flight[]>('/api/flights/manage').subscribe({
      next: flights => {this.flights.set(flights);this.flightLoading.set(false);},
      error: () => {this.error.set('Cannot load flights.');this.flightLoading.set(false);}
    });
  }

  loadDashboard(): void {
    this.http.get<AdminBooking[]>('/api/bookings/admin').subscribe({
      next: bookings => this.allBookings.set(bookings),
      error: () => {}
    });
  }

  loadActivity(): void {
    this.http.get<FlightActivity[]>('/api/flights/activity').subscribe({
      next: values => this.activities.set(values),
      error: () => {}
    });
  }

  searchCustomers(): void {
    this.customerError.set('');
    this.customerLoading.set(true);
    const params = new HttpParams().set('search', this.customerSearch.value.trim());
    this.http.get<AdminCustomer[]>('/api/auth/admin/customers', {params}).subscribe({
      next: result => {this.customers.set(result);this.customerLoading.set(false);},
      error: () => {this.customerError.set('Cannot load customers.');this.customerLoading.set(false);}
    });
  }

  filterBookings(): void {
    this.bookingError.set('');
    const raw = String(this.bookingFlightId.value).trim();
    if (raw && !/^[1-9][0-9]*$/.test(raw)) {
      this.bookingError.set('Enter a positive whole flight ID.');
      return;
    }
    let params = new HttpParams();
    if (raw) params = params.set('flightId', raw);
    if (this.bookingStatus.value !== 'ALL') params = params.set('status', this.bookingStatus.value);
    this.bookingLoading.set(true);
    this.http.get<AdminBooking[]>('/api/bookings/admin', {params}).subscribe({
      next: result => {this.bookings.set(result);this.bookingLoading.set(false);},
      error: () => {this.bookingError.set('Cannot load bookings.');this.bookingLoading.set(false);}
    });
  }

  clearBookingFilters(): void {
    this.bookingFlightId.setValue('');
    this.bookingStatus.setValue('ALL');
    this.filterBookings();
  }

  add(): void {
    this.error.set('');this.success.set('');
    if (this.form.invalid) {
      this.error.set('Complete all flight details.');this.form.markAllAsTouched();return;
    }
    const f = this.form.getRawValue();
    if (f.originCode === f.destinationCode || f.arrivalTime <= f.departureTime) {
      this.error.set('Choose different airports and check the schedule.');return;
    }
    this.http.post('/api/flights', f).subscribe({
      next: () => {
        this.success.set('Flight created.');
        this.form.reset();
        this.refresh();this.loadActivity();
      },
      error: response => this.error.set(response.error?.message || 'Could not create flight.')
    });
  }

  beginEdit(f: Flight): void {
    this.error.set('');this.success.set('');
    this.editFlight.set(f);
    this.editForm.setValue({
      price: f.price,
      status: f.status,
      operationalStatus: f.operationalStatus,
      departureTime: f.departureTime.slice(0, 16),
      arrivalTime: f.arrivalTime.slice(0, 16),
      aircraftType: f.aircraftType
    });
  }

  saveEdit(): void {
    const original = this.editFlight();
    if (!original || this.editForm.invalid) {this.error.set('Check all flight details.');return;}
    const raw = this.editForm.getRawValue();
    if (raw.arrivalTime <= raw.departureTime) {
      this.error.set('Arrival must follow departure.');return;
    }
    const changes: Record<string, string | number> = {};
    if (raw.price !== original.price) changes['price'] = raw.price;
    if (raw.status !== original.status) changes['status'] = raw.status as FlightStatus;
    if (raw.operationalStatus !== original.operationalStatus)
      changes['operationalStatus'] = raw.operationalStatus as OperationalStatus;
    if (raw.departureTime !== original.departureTime.slice(0, 16))
      changes['departureTime'] = raw.departureTime;
    if (raw.arrivalTime !== original.arrivalTime.slice(0, 16))
      changes['arrivalTime'] = raw.arrivalTime;
    if (raw.aircraftType !== original.aircraftType) changes['aircraftType'] = raw.aircraftType;
    if (!Object.keys(changes).length) {this.editFlight.set(null);return;}
    this.error.set('');this.success.set('');
    this.http.patch('/api/flights/' + original.id, changes).subscribe({
      next: () => {
        this.editFlight.set(null);
        this.success.set('Flight updated.');
        this.refresh();this.loadActivity();
      },
      error: response => this.error.set(response.error?.message || 'Could not update flight.')
    });
  }

  requestToggle(f: Flight): void {this.pendingFlight.set(f);}
  toggle(f: Flight): void {
    this.pendingFlight.set(null);this.error.set('');
    const status: FlightStatus = f.status === 'OPEN' ? 'CLOSED' : 'OPEN';
    this.http.patch('/api/flights/' + f.id, {status}).subscribe({
      next: () => {this.refresh();this.loadActivity();},
      error: response => this.error.set(response.error?.message || 'Could not change sales.')
    });
  }

  requestCancel(f: Flight): void {this.pendingCancellation.set(f);}
  cancelFlight(f: Flight): void {
    this.pendingCancellation.set(null);this.error.set('');
    this.http.patch('/api/flights/' + f.id, {operationalStatus: 'CANCELLED'}).subscribe({
      next: () => {this.refresh();this.loadActivity();},
      error: response => this.error.set(response.error?.message || 'Could not cancel flight.')
    });
  }
}

