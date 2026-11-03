export type UserRole = 'ADMIN' | 'CUSTOMER';
export type FlightStatus = 'OPEN' | 'CLOSED';
export type OperationalStatus = 'SCHEDULED' | 'DELAYED' | 'CANCELLED' | 'DEPARTED' | 'COMPLETED';
export type BookingStatus = 'CONFIRMED' | 'CANCELLED';

export interface Airport {
  code: string;
  name: string;
  city: string;
}

export interface Flight {
  id: number;
  flightNumber: string;
  origin: string;
  destination: string;
  originCode: string | null;
  destinationCode: string | null;
  departureTime: string;
  arrivalTime: string;
  price: number;
  capacity: number;
  aircraftType: string;
  status: FlightStatus;
  operationalStatus: OperationalStatus;
}

export interface Passenger {
  id: number;
  fullName: string;
  age: number;
  seatNumber: number;
  seatCode: string;
  fare: number;
}

export interface Booking {
  id: number;
  pnr: string;
  flightId: number;
  seatNumber: number;
  totalFare: number;
  status: BookingStatus;
  bookedAt: string;
  contactEmail: string | null;
  flightNumber: string | null;
  origin: string | null;
  destination: string | null;
  originCode: string | null;
  destinationCode: string | null;
  departureTime: string | null;
  arrivalTime: string | null;
  passengers: Passenger[];
}

export interface UserProfile {
  id: number;
  name: string;
  email: string;
  role: UserRole;
}

export interface AdminCustomer extends UserProfile {
  createdAt: string;
}

export interface AdminBooking {
  id: number;
  pnr: string;
  userId: number;
  flightId: number;
  totalFare: number;
  status: BookingStatus;
  bookedAt: string;
  passengerCount: number;
  flightNumber: string | null;
  contactEmail: string | null;
}

export interface FlightActivity {
  id: number;
  flightId: number;
  action: string;
  description: string;
  createdAt: string;
}

