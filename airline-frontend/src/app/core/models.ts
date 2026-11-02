export type UserRole = 'ADMIN' | 'CUSTOMER';
export type FlightStatus = 'OPEN' | 'CLOSED';
export type BookingStatus = 'CONFIRMED' | 'CANCELLED';

export interface Flight {
  id: number;
  flightNumber: string;
  origin: string;
  destination: string;
  departureTime: string;
  arrivalTime: string;
  price: number;
  capacity: number;
  status: FlightStatus;
}

export interface Booking {
  id: number;
  flightId: number;
  seatNumber: number;
  fareAtBooking: number;
  status: BookingStatus;
  bookedAt: string;
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

export interface AdminBooking extends Booking {
  userId: number;
}
