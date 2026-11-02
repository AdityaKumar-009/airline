# Live demo QA checklist

**Last run:** 3 November 2026, in the local Windows project environment.

## Verified in this workspace

- [x] Java 17.0.12, Node 22.9.0, npm 10.8.3, Angular CLI 19.2.27, and Angular 19.2.25 are available.
- [x] MySQL 8.0.46 is running; Auth, Flight, and Booking database migrations and tables are present.
- [x] Eureka is HTTP 200 and has one registered instance each for API Gateway, Auth, Flight, and Booking.
- [x] The packaged Angular app is served by Gateway at `http://localhost:8080`; the Gateway returns all eight seeded flights.
- [x] Gateway Swagger UI loads Authentication, Flights, and Bookings definitions; each definition was opened.
- [x] Seed customer and admin logins work; invalid login and duplicate registration show feedback.
- [x] Flight search filters by origin, destination, and date.
- [x] Customer can book a seat, view the booking, cancel it, and see the seat released.
- [x] Duplicate-seat booking returns 409; an out-of-capacity seat and a closed flight are rejected.
- [x] Booking, cancellation, and flight-sales changes use responsive in-app confirmation dialogs; no browser alert/confirm calls remain.
- [x] Customer cannot manage/create flights; admin can view, create, close, and reopen flights.
- [x] Customer can edit their name from My Profile; invalid names are blocked, and email/role remain unchanged.
- [x] Admin customer directory searches name/email, shows an empty state for no match, and exposes no credential fields.
- [x] Admin booking report lists customer/flight IDs and supports combined flight/status filters, clear, and invalid-ID feedback.
- [x] Admin search/filter controls preserve the selected tab and work with Enter; narrow-screen tables scroll horizontally.
- [x] Flight search actions match account role: customers can select a seat, admins return to flight management, and guests are sent to login.
- [x] Anonymous protected requests return 401; wrong-role requests return 403; a customer cannot cancel another user's booking.
- [x] Unknown booking URLs show an error instead of an endless loading message; a customer sent to `/admin` returns to Flights.
- [x] The narrow-screen flight-search page was visually checked; the browser console had no errors.
- [x] Angular production build passes. The complete Maven unit suite passes (six tests), including profile, customer directory, and admin booking filter cases.
- [x] The temporary end-to-end booking was removed after QA; the eight flights and two demo accounts were preserved. Three pre-existing cancelled booking rows were left untouched.

## Before presenting on a separate laptop

- [ ] Run `scripts/check-laptop.ps1` on that laptop and confirm Java, Node, and npm versions.
- [ ] Start MySQL and the five services using the project guide, then repeat the customer booking/cancellation demo there.
- [ ] Workbench is optional; the schemas and tables were verified with the local MySQL client in this workspace.
