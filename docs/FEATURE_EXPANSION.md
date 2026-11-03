# Airline Management System — Expanded Features

This page explains the newly expanded student-friendly version. The older implementation plan describes the original minimal booking request; the expanded API contract below takes precedence where it differs.

## Architectural rules retained

The project still has three business microservices: Auth, Flight, and Booking. Each follows the same simple layering:

    HTTP Controller -> Service interface -> Service implementation -> JPA repository -> MySQL

DTO records handle requests and responses, Bean Validation checks request fields, business rules live in services, and Spring Security enforces ADMIN and CUSTOMER roles. Angular uses the same standalone components, reactive forms, signals, route guards and authorization interceptor. Eureka and the API Gateway are unchanged.

Java 17 and the pinned Angular 19.2 toolchain are retained for the classroom laptop. No payment gateway, Docker, Kafka, Redis, extra microservice, baggage, check-in, or email service was introduced.

## Business changes

1. **Airport catalog:** search by city or three-letter airport code; admins select known airports. The Flight service owns the airport table and returns the list through GET /api/flights/airports.
2. **Flight administration:** create flights, adjust fares, update future schedules when no seats are actively reserved, choose aircraft type, manage sales OPEN/CLOSED separately from operational SCHEDULED/DELAYED/CANCELLED/DEPARTED/COMPLETED. An activity list records changes.
3. **Multi-passenger reservations:** a customer books one to four named passengers in a transaction, one seat per passenger. The server generates an eight-character PNR and derives the fare from Flight, not the browser.
4. **Visual seats:** six seats per row (A/B/C aisle D/E/F). Occupied seats cannot be selected and a selection limit of four is enforced. The database unique constraint on active flight/seat allocations is authoritative even if two browsers book simultaneously.
5. **Itineraries:** each booking stores a flight/fare snapshot, PNR and passenger list. My Bookings links to a print-friendly details page. If an admin cancels the flight, the itinerary displays a warning.
6. **Customer password change:** current password verification, new password validation, BCrypt rehashing.
7. **Admin overview:** active reservations, seats reserved, booking value, flight overview and activity history.

## API examples (Swagger)

Flight creation (ADMIN):

~~~json
{
  "flightNumber": "AI109",
  "originCode": "DEL",
  "destinationCode": "BOM",
  "departureTime": "2026-12-10T10:00:00",
  "arrivalTime": "2026-12-10T12:00:00",
  "price": 4500,
  "capacity": 30,
  "aircraftType": "A320"
}
~~~

Booking request (CUSTOMER, Bearer token):

~~~json
{
  "flightId": 1,
  "contactEmail": "traveller@example.test",
  "contactPhone": "+919876543210",
  "passengers": [
    {"fullName": "Demo One", "age": 24, "seatNumber": 7},
    {"fullName": "Demo Two", "age": 25, "seatNumber": 8}
  ]
}
~~~

Seat numbers 7 and 8 display as 2A and 2B in the UI. Omit contactPhone if unavailable. All API timestamps are treated as UTC.

Other routes:

| Method | Path | Access | Description |
| --- | --- | --- | --- |
| GET | /api/flights/airports | Public | Airport catalog |
| GET | /api/flights | Public | Available future flights; search by city/airport |
| PATCH | /api/flights/{id} | ADMIN | Fare, sales status, schedule, operation, aircraft |
| GET | /api/flights/activity | ADMIN | Latest flight changes |
| POST | /api/bookings | CUSTOMER | 1–4 passengers in a reservation |
| GET | /api/bookings/seats?flightId=1 | Public | Occupied seat numbers |
| GET | /api/bookings/me | CUSTOMER | Personal PNR and itinerary details |
| PATCH | /api/bookings/{id}/cancel | CUSTOMER owner | Cancel and release all booked seats |
| GET | /api/bookings/admin | ADMIN | Booking history and passenger counts |
| PATCH | /api/auth/me/password | Authenticated | Change password |

Password change JSON: currentPassword and newPassword. The backend verifies both and returns 204 on success.

## Validation and error handling

The Booking service checks a positive flight ID, a real future/open flight, 1–4 nonempty passengers, passenger names (at most 100 characters), ages 1–120, distinct seats inside the flight's capacity, contact email format, and current seat uniqueness. The Flight service validates known airports, distinct origin and destination, departure in the future, arrival after departure, positive fare, capacity 1–300 and aircraft name.

Angular prevents common invalid inputs before submit, but all security-sensitive checks run again on the server. JWT user ID and ADMIN/CUSTOMER access rules remain server-controlled. Two simultaneous requests for the same seat cannot both succeed because of a database uniqueness constraint.

## Migration and backward compatibility

Back up any existing local MySQL demo data first. Flyway V2 runs after the existing V1 migrations:
- Flight V2 creates airport and activity tables, and adds airport, aircraft and operational-state columns.
- Booking V2 introduces PNRs, contact details and passenger rows; migrates old single-seat bookings into one explicitly labelled legacy passenger; changes active seat allocations to permit multiple seats per booking.

Do not edit an already-applied V1 migration. Seeded classroom accounts/flights are still managed by the original DemoUsers and DemoFlights classes. Seeded flight departure dates are determined at first database setup; if they have passed before your presentation, create a new future flight.

## Demonstration order

1. Log in as Admin; view overview, airports, current flights and activity.
2. Create a new future flight using airport codes. Show the difference between ticket sales and flight status.
3. Log in as Customer. Search flights and select two available seats in the visual cabin.
4. Enter two passenger names, ages and a contact email. Confirm and view the generated PNR itinerary.
5. Print the itinerary, return to My Bookings and cancel the reservation. Both seats become available again.
6. Log in as Admin and view booking history, current availability and the activity list.
7. Try invalid age, repeated seat selection, cancelling another customer's booking, and rescheduling a flight with active reservations to demonstrate validation and ownership protections.

## Deliberate limits

This is an educational airline reservation system, not a commercial ticketing platform. It has no payment/refund handling, email notifications, baggage or check-in. Cross-service booking/schedule operations are not a distributed transaction; the admin schedule check reduces invalid changes but is not an absolute cross-service locking guarantee. Known historical Angular 19 dependency advisories and demo credentials mean the app should remain local to the classroom environment until the toolchain and security are modernized.
