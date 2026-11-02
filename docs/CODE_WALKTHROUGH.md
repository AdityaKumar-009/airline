# AirlineMS — Beginner's Code Walkthrough

This guide is a source-by-source learning companion. It identifies the production files, explains their statements in execution order for the important flows, defines the concepts, and links every explanation to the real code. The source files are kept as the one source of truth instead of pasting a second, quickly outdated copy of every file into Markdown. Open each linked source and follow its blocks in the order described here.

## 1. First understand the common pattern

```text
HTTP JSON → Controller → Service interface → Service implementation
          → Repository → MySQL table
          ← Response DTO ← Service implementation
```

| Word | Plain-language meaning | Where this project uses it |
|---|---|---|
| **Class** | A named type that groups data and behavior. | `Flight`, `AuthServiceImpl`, `AdminComponent`. |
| **Interface** | A promise listing methods a class must implement. | `AuthService`, `FlightService`, `BookingService`. |
| **Dependency injection (DI)** | Spring or Angular creates an object and supplies it where needed. | Constructor parameters in Java; `inject(...)` / constructor in Angular. |
| **Bean / provider** | An object managed by Spring / Angular's dependency container. | `@Service`, `@Repository`, `@Bean`, `@Injectable`. |
| **Controller** | Converts an HTTP route and JSON into a call to application code. | `AuthController`, `FlightController`, `BookingController`. |
| **DTO** | A small object used as the input or output format of an API. | The records inside `AuthDtos`, `FlightDtos`, `BookingDtos`. |
| **Entity** | A Java object that JPA stores as a database row. | `User`, `Flight`, `Booking`, `SeatAllocation`. |
| **Repository** | A database access interface. Spring Data generates the implementation. | `UserRepository`, `FlightRepository`, booking repositories. |
| **Enum** | A closed list of allowed named values. | Roles and `OPEN`, `CLOSED`, `CONFIRMED`, `CANCELLED`. |
| **Validation** | Rejecting malformed input before it changes data. | Jakarta annotations on DTOs, service checks, Angular validators. |
| **Transaction** | A group of database changes that commit together or roll back together. | Booking and seat-allocation writes; cancellation and seat release. |
| **JWT** | A signed token carrying a verified account ID and role. | Auth creates it; every protected service checks it. |
| **Migration** | A numbered SQL change that can be applied once and tracked. | `V1__users.sql`, `V1__flights.sql`, `V1__bookings.sql`. |
| **Signal** | Angular's small reactive value; templates update when it changes. | `loading`, `flights`, `error`, user name/role. |
| **Observable** | A value that may arrive later, commonly an HTTP response. | `HttpClient.get/post/patch(...).subscribe(...)`. |

The repeated design rule is: the controller speaks HTTP, the service speaks business rules, the repository speaks database. Keeping those jobs separate makes each class easier to explain and test.

## 2. Backend entry points and configuration

| File | Read it this way |
|---|---|
| [AuthApplication.java](../auth-service/src/main/java/com/airline/auth/AuthApplication.java) | `package` and `import` lines name the class and dependencies; `@SpringBootApplication` turns on Spring scanning and auto-configuration; `main` starts the Auth process. Flight, Booking, Gateway, and Discovery have the same small entry-point shape in their respective folders. |
| [FlightApplication.java](../flight-service/src/main/java/com/airline/flight/FlightApplication.java) | Starts Flight. It also brings component scanning to `com.airline.flight`, where its controllers, services, and repositories live. |
| [BookingApplication.java](../booking-service/src/main/java/com/airline/booking/BookingApplication.java) | Starts Booking; its scan finds the Feign/load-balanced client config and booking components too. |
| [GatewayApplication.java](../api-gateway/src/main/java/com/airline/gateway/GatewayApplication.java) | Starts the public HTTP entry point, which reads its route table from `application.properties`. |
| [DiscoveryApplication.java](../discovery-server/src/main/java/com/airline/discovery/DiscoveryApplication.java) | Starts Eureka, the address book for running services. It stores service registrations in memory. |
| [GatewayCors.java](../api-gateway/src/main/java/com/airline/gateway/GatewayCors.java) | Builds one CORS rule for local Angular development on port 4200. CORS is a browser permission rule; it is not authentication. |
| [Discovery properties](../discovery-server/src/main/resources/application.properties) | Selects the Eureka port and local service registry behavior. |
| [Gateway properties](../api-gateway/src/main/resources/application.properties) | Selects port 8080, Eureka lookup, routes, Swagger aggregation, and the faster local registry refresh. |
| [Auth properties](../auth-service/src/main/resources/application.properties) | Selects service name/port, `airline_auth`, MySQL credentials, Flyway, and JWT key paths. |
| [Flight properties](../flight-service/src/main/resources/application.properties) | Selects service name/port, `airline_flights`, MySQL credentials, Flyway, and JWT verification key. |
| [Booking properties](../booking-service/src/main/resources/application.properties) | Selects service name/port, `airline_bookings`, MySQL credentials, Flyway, and JWT verification key. |

In each service properties file, `spring.application.name` is the Eureka service name; `server.port` is the local port; `eureka.client...` points at the registry; the JDBC URL selects a schema; datasource username/password configure MySQL; `spring.jpa.hibernate.ddl-auto=validate` checks entity/table compatibility; Flyway runs the SQL migrations; JWT resource paths load the PEM keys. The setup guide lists the exact values and startup commands.

### Build and frontend configuration files

| File | What it controls |
|---|---|
| [Root pom.xml](../pom.xml) | The Maven parent and five-module build. Shared Java version and dependency versions are declared once so modules stay compatible. `mvn clean package` builds the reactor in dependency order. |
| [Auth pom.xml](../auth-service/pom.xml), [Flight pom.xml](../flight-service/pom.xml), [Booking pom.xml](../booking-service/pom.xml) | Each business service's Spring starters and libraries. A module inherits common versions from the root parent. |
| [Gateway pom.xml](../api-gateway/pom.xml), [Discovery pom.xml](../discovery-server/pom.xml) | Dependencies for routing/OpenAPI and Eureka respectively. |
| [Angular package.json](../airline-frontend/package.json) | Human-readable npm scripts and exact direct dependency ranges; `package-lock.json` pins the resolved dependency tree used by `npm ci`. |
| [Angular CLI configuration](../airline-frontend/angular.json) | Tells the Angular CLI the app entry point, build output, production build options, and global stylesheet. |
| [Angular TypeScript configuration](../airline-frontend/tsconfig.json) | Compiler options for TypeScript and template checking. The compiler catches type mistakes before the browser runs. |
| [Development API proxy](../airline-frontend/proxy.conf.json) | Forwards `/api` requests from the local Angular server at 4200 to Gateway at 8080, avoiding a separate hard-coded API hostname in the components. |
| [Global frontend styles](../airline-frontend/src/styles.css) | Shared page background, typography, form fields, buttons, panels, tables, status badges, and responsive base styling. Component CSS then adds page-specific layout. |

### Eureka refresh settings

`discovery-server/src/main/resources/application.properties` sets `eureka.server.response-cache-update-interval-ms=5000`. `api-gateway/src/main/resources/application.properties` sets `eureka.client.registry-fetch-interval-seconds=5`. These local-demo settings make new registrations visible sooner, so a Gateway started shortly after the business services is less likely to make its first request before it has discovered them.

## 3. Auth service: users, passwords, JWT, profile

### Read these files in this order

1. [UserRole.java](../auth-service/src/main/java/com/airline/auth/domain/UserRole.java): `ADMIN` and `CUSTOMER` are the only role values. An enum prevents misspellings from becoming a new role.
2. [User.java](../auth-service/src/main/java/com/airline/auth/entity/User.java): `@Entity` says JPA persists this class; `@Table` selects `users`; each `@Column` maps a field to a SQL column; `@Id` and `@GeneratedValue` make the database generate the ID; `@Enumerated(EnumType.STRING)` stores `ADMIN` or `CUSTOMER` as readable text; the protected empty constructor is required by JPA; the public constructor is used by application code.
3. [UserRepository.java](../auth-service/src/main/java/com/airline/auth/repository/UserRepository.java): extending `JpaRepository<User, Long>` supplies common CRUD methods. Spring derives `findByEmail`, `existsByEmail`, and role/sort queries from their names. `@Query` is used only for a case-insensitive name-or-email search; `:role` and `:search` are named parameters, not string-concatenated SQL.
4. [AuthDtos.java](../auth-service/src/main/java/com/airline/auth/dto/AuthDtos.java): each `record` is an immutable request or response shape. `@NotBlank`, `@Email`, and `@Size` are server-side input rules. `AdminCustomerResponse` intentionally does not have a password field.
5. [AuthService.java](../auth-service/src/main/java/com/airline/auth/service/AuthService.java): declares the use cases the controller may call. It makes the implementation replaceable and easy to mock in tests.
6. [AuthServiceImpl.java](../auth-service/src/main/java/com/airline/auth/service/AuthServiceImpl.java): contains the real use case logic:

| Method / block | What each statement is doing |
|---|---|
| Fields and constructor | Keeps the repository, password encoder, and JWT encoder. Spring injects them. No code manually creates a database connection. |
| `register` | Normalizes email; checks for a duplicate; hashes the password with BCrypt; sets role to `CUSTOMER` in Java; saves the entity; maps it to a response that omits the hash. Role is not accepted from JSON. |
| `login` | Finds the normalized email; compares the entered password to the BCrypt hash; uses the same “invalid credentials” error for missing account and wrong password; creates issued-at, expiry, subject, role, email, and name claims; signs with RS256; returns the token plus display data. |
| `me` | Looks up the ID that came from the verified JWT subject; converts missing user to HTTP 404; returns a safe DTO. |
| `updateMe` | Loads the signed-in user, replaces only `name`, trims whitespace, saves, and maps to the safe DTO. The method has no code that accepts email or role changes. |
| `customers` | Treats null/blank search as “all customers”; otherwise asks the repository for matches; maps each entity to a safe admin DTO. Filtering by `CUSTOMER` is enforced in the query path. |
| `normalizeEmail` | Trims and lowercases using `Locale.ROOT`, so case differences do not create separate classroom accounts. |
| `invalidCredentials` | Centralizes the 401 response for bad login details. |
| `toResponse` | One mapping function prevents repeating entity-to-API field selection. It deliberately does not map `passwordHash`. |

7. [AuthController.java](../auth-service/src/main/java/com/airline/auth/controller/AuthController.java): `@RestController` makes methods HTTP endpoints; `@RequestMapping("/api/auth")` is their common prefix; `@PostMapping`, `@GetMapping`, and `@PatchMapping` select HTTP verbs and paths; `@RequestBody` reads JSON; `@Valid` triggers DTO validation; `@RequestParam` reads `search`; `@ResponseStatus(CREATED)` returns 201 for registration. For profile routes, `JwtAuthenticationToken` contains claims accepted by Spring Security; the controller reads only the verified `sub` claim and passes its ID to the service.
8. [JwtConfig.java](../auth-service/src/main/java/com/airline/auth/security/JwtConfig.java): a `@Bean` makes each object available for dependency injection; `BCryptPasswordEncoder` is the password hash implementation; the public key builds a JWT decoder; the PKCS#8 private key plus public key form an RSA JWK; `NimbusJwtEncoder` signs claims. A try-with-resources closes each key file after reading.
9. [SecurityConfig.java](../auth-service/src/main/java/com/airline/auth/security/SecurityConfig.java): disables CSRF for this stateless API; selects no server-side session; permits only register/login anonymously; requires ADMIN for `/api/auth/admin/**`; enables JWT resource-server checks for other requests; maps the JWT's `role` claim to Spring's `ROLE_ADMIN` / `ROLE_CUSTOMER`; permits internal `ERROR` dispatches so validation failures retain HTTP 400.
10. [DemoUsers.java](../auth-service/src/main/java/com/airline/auth/service/DemoUsers.java): on startup it creates the two classroom accounts only when their emails are absent; passwords come from environment-overridable properties and are passed through the same password encoder. It does not reinsert accounts on every restart.
11. [ApiErrors.java](../auth-service/src/main/java/com/airline/auth/controller/ApiErrors.java): `@RestControllerAdvice` converts known validation, duplicate-data, and business exceptions into compact JSON and the correct HTTP status. Advice prevents controller methods from repeating error formatting.

## 4. Flight service: schedule and sales status

| File | Explanation from top to bottom |
|---|---|
| [FlightStatus.java](../flight-service/src/main/java/com/airline/flight/domain/FlightStatus.java) | Allows only `OPEN` or `CLOSED`; used in both database entity and API DTO. |
| [Flight.java](../flight-service/src/main/java/com/airline/flight/entity/Flight.java) | JPA maps fields to `flights`; the ID is generated by MySQL; date/time uses `LocalDateTime`; fare uses `BigDecimal` to avoid floating-point rounding; status stores enum text; the unique constraint prevents the same flight number at the same departure time. |
| [FlightDtos.java](../flight-service/src/main/java/com/airline/flight/dto/FlightDtos.java) | `CreateFlight` contains user-supplied flight data and Bean Validation rules; `EditFlight` uses nullable fare/status so an admin can change one field without overwriting the other; `FlightResponse` is the safe read shape. |
| [FlightRepository.java](../flight-service/src/main/java/com/airline/flight/repository/FlightRepository.java) | Extends Spring Data CRUD and declares an ordered query for open flights. Spring implements it from the method name. |
| [FlightService.java](../flight-service/src/main/java/com/airline/flight/service/FlightService.java) | Lists public search, admin list, get, create, and edit use cases independently from HTTP routes. |
| [FlightServiceImpl.java](../flight-service/src/main/java/com/airline/flight/service/FlightServiceImpl.java) | `search` starts from open flights, filters out past departures, then applies optional origin, destination, and date filters; `manage` returns all flights for admins; `get` looks up one flight; `create` checks future departure, different cities, and arrival-after-departure before saving; `edit` changes only non-null fields; `find` converts missing IDs to 404; `toResponse` maps entity fields to the API DTO. `Clock.systemUTC()` makes date comparisons explicit and predictable. |
| [FlightController.java](../flight-service/src/main/java/com/airline/flight/controller/FlightController.java) | Maps `/api/flights` and child paths to the interface; `@DateTimeFormat(ISO.DATE)` parses the optional date; `@Valid` checks the create/edit request before service logic; the Spring Security config protects admin routes. |
| [SecurityConfig.java](../flight-service/src/main/java/com/airline/flight/security/SecurityConfig.java) | Reads `public.pem` and uses RSA JWT decoding; public GET search and single-flight GET are open; `/manage` and every write require ADMIN; docs are public; a bad date becomes a proper 400 via the internal error-dispatch rule. |
| [DemoFlights.java](../flight-service/src/main/java/com/airline/flight/service/DemoFlights.java) | Inserts eight flights when the flight table is empty. The dates are generated for the first seed run, not recalculated on each restart. |
| [ApiErrors.java](../flight-service/src/main/java/com/airline/flight/controller/ApiErrors.java) | Converts validation issues to 400, `ResponseStatusException` to its chosen status, and duplicate schedule constraints to 409. |

## 5. Booking service: the most important business transaction

| File | What to notice |
|---|---|
| [BookingStatus.java](../booking-service/src/main/java/com/airline/booking/domain/BookingStatus.java) | `CONFIRMED` or `CANCELLED`; a cancellation changes the existing booking's state instead of deleting its history. |
| [FlightStatus.java](../booking-service/src/main/java/com/airline/booking/domain/FlightStatus.java) | A local enum copy of Flight's status. It avoids making the Flight service's Java class a shared library dependency. |
| [Booking.java](../booking-service/src/main/java/com/airline/booking/entity/Booking.java) | JPA table mapping; saves user ID and flight ID as logical references; snapshots the fare and UTC booking time; defaults to `CONFIRMED`. |
| [SeatAllocation.java](../booking-service/src/main/java/com/airline/booking/entity/SeatAllocation.java) | Represents only an actively occupied seat. A unique database constraint on flight plus seat prevents simultaneous duplicate reservations. |
| [BookingDtos.java](../booking-service/src/main/java/com/airline/booking/dto/BookingDtos.java) | Request validation plus separate customer and admin response records; the admin record contains customer ID but never joins or exposes auth records. `FlightSnapshot` is Booking's local view of the Flight API response. |
| [BookingRepository.java](../booking-service/src/main/java/com/airline/booking/repository/BookingRepository.java) | Finds one customer's rows and all rows newest-first. |
| [SeatAllocationRepository.java](../booking-service/src/main/java/com/airline/booking/repository/SeatAllocationRepository.java) | Reads active seat allocations by flight. `deleteById` is inherited from `JpaRepository`. |
| [FlightClientConfig.java](../booking-service/src/main/java/com/airline/booking/config/FlightClientConfig.java) | Builds `RestClient` instances with short connect/read timeouts; `@LoadBalanced` lets `http://flight-service` resolve through Eureka; `@Qualifier` selects this named builder in Booking. |
| [BookingService.java](../booking-service/src/main/java/com/airline/booking/service/BookingService.java) | Declares seat lookup, own bookings, admin report, book, and cancel use cases. |

### `BookingServiceImpl.java` line by line by method

Read [BookingServiceImpl.java](../booking-service/src/main/java/com/airline/booking/service/BookingServiceImpl.java) top to bottom:

1. The imports name the model, repositories, UTC clock, Spring transaction tools, and HTTP client/error types used below.
2. `@Service` registers the class with Spring.
3. Three `final` fields hold injected dependencies. The constructor receives repositories and the qualified `RestClient.Builder`; `baseUrl("http://flight-service")` uses the Eureka service name, not a hard-coded machine IP.
4. `occupied` asks the seat repository for allocations, maps each row to its seat number, sorts the result, and returns an immutable list.
5. `mine` asks the repository for only the JWT owner's ID and maps each row to the customer-safe response.
6. `adminBookings` rejects non-positive flight IDs, then applies optional flight and status filters to the ordered data and maps to an admin-only DTO. The controller's security rule still checks ADMIN.
7. `book` is marked `@Transactional`. It first asks Flight for a snapshot; checks that the flight is open and departs in the future; checks seat number against capacity; writes the booking and flushes it to obtain its generated ID; writes and flushes the seat allocation; converts a database uniqueness failure to 409; returns a DTO. If the second write fails, the transaction rolls the first write back.
8. `cancel` is also `@Transactional`. It loads the booking or returns 404; compares stored owner ID to the verified JWT ID; rejects other users with 403; rejects already-cancelled bookings with 409; asks Flight for the schedule and rejects past flights; marks cancelled, removes the active seat row, and saves the booking in the same transaction.
9. `toResponse` and `toAdminResponse` select which fields leave the service. Keeping those mappings explicit prevents entity fields from leaking accidentally.
10. `lookupFlight` sends `GET /api/flights/{id}` through the load-balanced client. Flight 404 becomes a local 404; transport or service errors become 503 so the caller gets a meaningful API response.

`@Transactional` is not an annotation that makes several microservices atomic. It covers only writes in Booking's own MySQL schema. Flight is read through HTTP.

## 6. The API controllers are intentionally thin

| File | Responsibilities |
|---|---|
| [BookingController.java](../booking-service/src/main/java/com/airline/booking/controller/BookingController.java) | Turns `/seats`, `/me`, `/admin`, POST, and cancellation routes into calls to `BookingService`. It obtains the customer ID from the JWT subject. `@ResponseStatus(CREATED)` makes reservation creation return 201. |
| [AuthController.java](../auth-service/src/main/java/com/airline/auth/controller/AuthController.java) | Routes registration, login, current profile, profile patch, and admin directory calls. |
| [FlightController.java](../flight-service/src/main/java/com/airline/flight/controller/FlightController.java) | Routes public search/get and admin management/create/edit calls. |
| `*/controller/ApiErrors.java` | Service-specific `@RestControllerAdvice`; maps validation/business/database failures to HTTP responses. |
| [Booking SecurityConfig.java](../booking-service/src/main/java/com/airline/booking/security/SecurityConfig.java) | Verifies JWTs with the public key, permits public seat lookup and API docs, requires CUSTOMER for customer booking routes and ADMIN for the report, and permits internal error dispatches so validation keeps its correct status. |

## 7. Angular startup and core files

| File | Read it block by block |
|---|---|
| [main.ts](../airline-frontend/src/main.ts) | Imports Angular bootstrap, router and HTTP providers; imports the root component, routes, and interceptor; `bootstrapApplication` starts the standalone app; `provideRouter(...withHashLocation())` uses `/#/path` routes so the built site works without server-side route rewrites; `provideHttpClient(withInterceptors(...))` registers the token interceptor; `.catch` logs a startup error. |
| [app.routes.ts](../airline-frontend/src/app/app.routes.ts) | Each object maps a URL path to a component; `canActivate` calls a role guard before a protected route; the empty path redirects to flights; `**` is the last-resort unknown path. |
| [auth.service.ts](../airline-frontend/src/app/core/auth.service.ts) | The injectable singleton owns three signals; constructor DI receives `HttpClient`; `login` calls the API and uses RxJS `tap` for the successful-response side effect of storing token, role, and name in `sessionStorage`; `register`, `profile`, and `updateName` wrap API calls; `logout` removes the three stored keys and clears signals. |
| [auth.interceptor.ts](../airline-frontend/src/app/core/auth.interceptor.ts) | Reads the token once per request; without a token it forwards the request unchanged; with a token it clones the immutable request and adds the Bearer header; `next` hands the request to the next interceptor or network layer. |
| [auth.guard.ts](../airline-frontend/src/app/core/auth.guard.ts) | `CanActivateFn` is a function the router calls; `inject` gets AuthService and Router; guards return `true` for the correct role, otherwise a `UrlTree` to redirect. It is a navigation aid; the backend still authorizes every protected API. |
| [models.ts](../airline-frontend/src/app/core/models.ts) | TypeScript unions mirror the allowed role and status text; interfaces describe flight, booking, profile, and admin JSON. These are compile-time types and do not validate untrusted server input at runtime. |

## 8. Angular page components and external templates

All components use `templateUrl` and `styleUrl`. For each page, read the TypeScript behavior, then its HTML, then its CSS. [src/styles.css](../airline-frontend/src/styles.css) holds shared colors, buttons, panels, spacing, form controls, and responsive base rules.

| Component source | HTML template | CSS | What the TypeScript and template do together |
|---|---|---|---|
| [AppComponent](../airline-frontend/src/app/app.component.ts) | [app.component.html](../airline-frontend/src/app/app.component.html) | [app.component.css](../airline-frontend/src/app/app.component.css) | Injects auth/router; shows role-aware navigation; logout clears session and routes to login; `<router-outlet>` is where the selected page appears. |
| [LoginComponent](../airline-frontend/src/app/features/auth/login.component.ts) | [login.component.html](../airline-frontend/src/app/features/auth/login.component.html) | [login.component.css](../airline-frontend/src/app/features/auth/login.component.css) | A reactive form holds name/email/password; email and password validators are declared once; `toggle` switches modes and clears messages; `submit` checks form validity, uses AuthService, shows success/error text, and redirects after login. The form never prints credentials. |
| [FlightsComponent](../airline-frontend/src/app/features/flights/flights.component.ts) | [flights.component.html](../airline-frontend/src/app/features/flights/flights.component.html) | [flights.component.css](../airline-frontend/src/app/features/flights/flights.component.css) | Starts a search in `ngOnInit`; trims optional fields into query parameters; stores loading/error/results in signals; template loops over results and changes the action according to role; `formatDate` and `formatTime` keep display formatting readable. |
| [BookComponent](../airline-frontend/src/app/features/bookings/book.component.ts) | [book.component.html](../airline-frontend/src/app/features/bookings/book.component.html) | [book.component.css](../airline-frontend/src/app/features/bookings/book.component.css) | Reads flight ID from the route; fetches flight and occupied seats; adds max-capacity validation after flight loads; rejects an occupied seat in the UI; opens a review modal; confirms with POST; refreshes occupied seats after a race/409. The database remains the final duplicate-seat check. |
| [MyBookingsComponent](../airline-frontend/src/app/features/bookings/my-bookings.component.ts) | [my-bookings.component.html](../airline-frontend/src/app/features/bookings/my-bookings.component.html) | [my-bookings.component.css](../airline-frontend/src/app/features/bookings/my-bookings.component.css) | Loads current user's rows, shows loading/empty/error states, opens a confirmation modal for confirmed bookings, PATCHes cancellation, and reloads the list. Cancelled bookings remain visible as history. |
| [ProfileComponent](../airline-frontend/src/app/features/profile/profile.component.ts) | [profile.component.html](../airline-frontend/src/app/features/profile/profile.component.html) | [profile.component.css](../airline-frontend/src/app/features/profile/profile.component.css) | Loads user profile into a form; name is required, max 100 chars, and must contain a non-whitespace character; email is readonly; save trims name and shows pending/success/error feedback. |
| [AdminComponent](../airline-frontend/src/app/features/admin/admin.component.ts) | [admin.component.html](../airline-frontend/src/app/features/admin/admin.component.html) | [admin.component.css](../airline-frontend/src/app/features/admin/admin.component.css) | Admin tabs switch between flight management, customer directory, booking report. Flight form uses reactive validators plus city/time rules; customer search sends trimmed text; booking filter checks a positive whole ID and optional enum status; flight changes use the shared modal. |
| [ConfirmModalComponent](../airline-frontend/src/app/shared/confirm-modal.component.ts) | [confirm-modal.component.html](../airline-frontend/src/app/shared/confirm-modal.component.html) | [confirm-modal.component.css](../airline-frontend/src/app/shared/confirm-modal.component.css) | `@Input` values customize labels/message/destructive color; `@Output` events notify the parent; it moves initial focus to Cancel, supports Escape and backdrop dismissal, and gives the dialog an accessible name and description. CSS turns it into a bottom sheet at narrow widths and respects reduced-motion preference. |

Angular syntax used in the templates:

| Syntax | Meaning |
|---|---|
| `[value]`, `[disabled]`, `[formGroup]` | Property binding: send a TypeScript value into an HTML/component property. |
| `(click)`, `(ngSubmit)`, `(keydown.enter)` | Event binding: call a method when the user acts. |
| `{{ value }}` | Interpolation: display a value as text. Angular escapes inserted strings. |
| `@if (...) { ... } @else { ... }` | Show one template branch based on a condition. |
| `@for (item of items(); track item.id)` | Render a list and use stable IDs so Angular can efficiently update rows. |
| `formControlName` | Connect an HTML input to a control in the reactive form model. |

## 9. SQL files: what gets created

| File | Purpose |
|---|---|
| [create-databases.sql](../database/create-databases.sql) | Optional Workbench helper: creates the three empty schemas. JDBC URLs also allow local schema creation for the classroom root account. |
| `auth-service/src/main/resources/db/migration/V1__users.sql` | Creates `users` and unique email storage. |
| `flight-service/src/main/resources/db/migration/V1__flights.sql` | Creates `flights`, fare/status/schedule columns, and the flight number/departure uniqueness rule. |
| `booking-service/src/main/resources/db/migration/V1__bookings.sql` | Creates booking history, seat allocations, booking FK, and unique active flight/seat rule. |

The filenames are ordered migrations. Do not rename an already-applied migration casually: Flyway records its name/checksum in the schema history table.

## 10. Test source: what is and is not proven

| Test file | Current test cases |
|---|---|
| [AuthServiceTest.java](../auth-service/src/test/java/com/airline/auth/AuthServiceTest.java) | Customer report returns only safe customer fields; profile update trims name but preserves email and role. |
| [FlightServiceTest.java](../flight-service/src/test/java/com/airline/flight/FlightServiceTest.java) | Reject same origin/destination and past departure. |
| [BookingServiceTest.java](../booking-service/src/test/java/com/airline/booking/BookingServiceTest.java) | Sort occupied seats; combine flight and status filters for admin report. |

These six unit tests mock repositories or the outside dependency. They run quickly but do not replace live API checks. The live demo checks performed on this project also covered MySQL migrations, JWT authorization, route status codes, browser flows, modal behavior, and actual booking/cancellation. The complete checklist is in [qa/demo-checklist.md](../qa/demo-checklist.md).

## 11. Support code, keys, and scripts

| File/folder | Explanation |
|---|---|
| [`keys/private.pem`](../keys/private.pem) | Secret RSA key used only by Auth to sign JWTs. Do not send it to classmates or commit it. |
| [`keys/public.pem`](../keys/public.pem) | Public verification key read by Auth, Flight, and Booking. It is paired with that private key. |
| [GenerateKeys.java](../tools/GenerateKeys.java) | Creates a 2048-bit RSA pair using Java's crypto API; encodes DER bytes as PEM Base64; skips generation if `private.pem` already exists so it does not replace an active pair. Run from the project root because its path is relative. |
| [use-local-tools.ps1](../scripts/use-local-tools.ps1) | Finds the portable JDK/Maven and pinned Node folders; sets `JAVA_HOME`, `MAVEN_HOME`, and the current PowerShell PATH only. |
| [check-laptop.ps1](../scripts/check-laptop.ps1) | Prints and compares Java, Maven, Node, npm, Angular, and MySQL readiness against the classroom versions. |
| [setup-local-tools.ps1](../scripts/setup-local-tools.ps1) | Downloads pinned tool archives, checks their published checksums, extracts them under ignored `.tools`, and leaves machine-wide PATH untouched. |
| [setup-local-mysql.ps1](../scripts/setup-local-mysql.ps1) | Downloads and checksum-verifies portable MySQL Server, writes a local config, initializes `.tools/mysql-data`; it does not install a Windows service. |
| [start-local-mysql.ps1](../scripts/start-local-mysql.ps1) | Starts the local `mysqld` in the current terminal; Ctrl+C stops it. |
| [package-demo.ps1](../scripts/package-demo.ps1) | Ensures keys exist, runs `npm ci` and Angular build, copies the browser output into Gateway's static folder, and runs Maven `clean package`. |
| [proxy.conf.json](../airline-frontend/proxy.conf.json) | For development only, forwards Angular `/api` calls from port 4200 to Gateway port 8080. |

Scripts are helpers; the app itself runs from Java jars, MySQL, and its required RSA keys. You can perform the same steps manually without the scripts.

## 12. Interview practice: questions with short answers

### Why keep a service interface and implementation?

The interface names the use cases a controller needs; the implementation contains the steps. It decouples the HTTP layer from those steps and lets unit tests mock the interface or its repository dependencies.

### Why does the app use DTOs instead of returning entities?

An entity contains persistence details such as `passwordHash`. A DTO explicitly lists public fields, so internal database fields do not leak and API shape can evolve separately from tables.

### Why BCrypt instead of encrypting passwords?

Passwords should not be recoverable. BCrypt is a slow one-way password hash; login checks an entered password with `matches` rather than decrypting anything.

### What does a JWT signature prove?

It proves that the token was signed by the holder of Auth's private key and was not changed afterward. A public-key verifier can check it without possessing the signing key. The token expiry still must be checked.

### Does a frontend route guard secure an admin API?

No. A user can call the URL directly. Guards only guide UI navigation; Spring Security on the backend verifies the JWT and role for every protected request.

### Why is a seat-allocation table separate from the booking history?

The booking row preserves the record after cancellation; the allocation row represents the seat currently taken. Removing the allocation releases the seat without erasing history.

### How do you prevent two people taking the last same seat at once?

The UI's seat list is only a hint because another request could race. Booking's database has a unique constraint for flight plus seat. The insert is flushed inside a transaction; one request succeeds and the duplicate maps to HTTP 409.

### What does `@Transactional` guarantee here?

It groups the Booking service's local inserts or updates. It does not create a distributed transaction across Booking, Flight, Auth, Eureka, or the Gateway.

### Why no SQL foreign key from Booking to Flight or User?

Those records live in separate microservice schemas. A database constraint cannot safely join independent service databases; Booking stores logical IDs and calls Flight by API when it needs flight facts.

### Why use `BigDecimal` for money?

Binary floating point cannot exactly represent many decimal fractions. `BigDecimal` maps naturally to SQL `DECIMAL` and avoids fare rounding surprises.

### What does Eureka do, and what does Gateway do?

Eureka tracks service names and current addresses. Gateway receives browser traffic and routes each API path to the right registered service. Gateway does not own business tables.

### What does Flyway add over Hibernate auto-create?

Flyway applies explicit numbered SQL migrations and records which have run. It makes the schema change visible and repeatable. Hibernate is set to validate mappings, not create tables invisibly.

### What happens when a query parameter contains an invalid date?

Spring cannot convert it to `LocalDate`, so the service responds 400. The security chain permits the framework's internal error dispatch so it does not become an incorrect 401.

### Why can a service run but Gateway still return “no instance” briefly?

Eureka and the Gateway both cache registrations. This project's local demo refreshes both caches every five seconds and waits for services to register before launching Gateway.

## 13. How to study it in an hour

1. Read `BookingServiceImpl.book` and `cancel`; explain the rules aloud.
2. Follow a POST from `BookingController` to `BookingService` to `BookingServiceImpl` to each repository.
3. Open `Booking` and `V1__bookings.sql`; match Java fields to column constraints.
4. Open `AuthServiceImpl.login`, then `JwtConfig` and `SecurityConfig`; explain who signs and who verifies.
5. Open Angular `BookComponent.ts` and its HTML; distinguish client validation from the database uniqueness rule.
6. Use Swagger to call a protected path with and without a Bearer token; explain 401 versus 403.
7. Run `mvn test`, `npm run build`, then follow the setup guide to start the five JVMs.

## 14. Statement-by-statement walkthroughs of the main use cases

The following numbered steps follow the statements in each linked method from top to bottom. Java imports, package declarations, annotations, fields, constructors, and helper methods are also covered in the file tables above; the walkthrough focuses on why each executable statement exists.

### Registration and login — `AuthServiceImpl`

Read [AuthServiceImpl.java](../auth-service/src/main/java/com/airline/auth/service/AuthServiceImpl.java) beside this list.

**`register(request)`**

1. Read `request.email()` and call `normalizeEmail`: leading/trailing spaces are removed and email casing is made consistent.
2. Ask `users.existsByEmail(email)` before inserting. If it already exists, throw HTTP 409 Conflict so the database's unique email rule is also supported by a useful API error.
3. Create `User` from the trimmed name, normalized email, `passwords.encode(...)`, and the hard-coded `CUSTOMER` enum. The client cannot request an ADMIN role by changing JSON.
4. Save through the injected repository. Spring Data performs the insert and populates generated fields such as ID and creation time.
5. Pass the saved entity to `toResponse`; the returned DTO contains safe profile fields and never the password hash.

**`login(request)`**

1. Normalize the submitted email with the same method registration uses.
2. Search by email. If no row exists, `orElseThrow(this::invalidCredentials)` returns the same 401 message used for a wrong password, so the response does not reveal which emails are registered.
3. Run BCrypt `matches(plainText, storedHash)`. The raw password is compared to its one-way hash; it is never decoded.
4. Capture one `Instant` as `issuedAt`. Reusing that value keeps the token's start and one-hour expiration internally consistent.
5. Build claims: issuer identifies Auth; `subject` is the database user ID; issued-at and expiry bound token lifetime; role, email, and name provide verified identity claims to the other services.
6. Select `RS256` in the JWS header and ask the injected encoder to sign the claims with the private RSA key.
7. Return the token and basic account display fields in `AuthResponse`. The browser sends the token as a Bearer token for later API calls.

**`updateMe(id, request)`**

1. Find only the row whose ID came from the verified token subject. A missing database row returns 404.
2. Replace only `user.name` with the trimmed validated request value. Email, role, and password are not accepted by this method.
3. Save, then map to `UserResponse`. The API therefore reflects the persisted value, not an untrusted guess from the browser.

**`customers(search)`**

1. Convert null search to an empty string and trim it.
2. For blank input, request customers ordered newest first; for text, request a case-insensitive name/email match.
3. Both repository paths explicitly filter the `CUSTOMER` role. Admins are not mixed into the customer directory.
4. Map each entity into `AdminCustomerResponse` and collect the immutable stream result. No password field exists in that response type.

### Search and create flights — `FlightServiceImpl`

Read [FlightServiceImpl.java](../flight-service/src/main/java/com/airline/flight/service/FlightServiceImpl.java).

**`search(origin, destination, date)`**

1. Get the current UTC time. The API's flight times are treated consistently as UTC in this demo.
2. Create a result list and ask the repository for OPEN flights ordered by departure time.
3. Skip any flight that is not strictly after the current time; past and departing-now flights cannot be newly booked from public search.
4. If an origin filter exists, compare trimmed text without case sensitivity. A failed condition uses `continue` to skip the current row.
5. Apply the same optional comparison to destination.
6. If a date exists, compare it to the departure's calendar date and skip non-matches.
7. Convert accepted entities to `FlightResponse` and return the results. A response DTO prevents JPA fields from becoming an accidental API contract.

**`create(request)`**

1. Reject a departure that is not in the future with 400.
2. Reject equal origin and destination names, ignoring case.
3. Reject an arrival that is not after departure.
4. Construct an entity from request values. Flight number and city names are trimmed; flight number is stored uppercase for consistency.
5. Save and map the resulting entity to a response. DTO field constraints reject missing, too-long, non-positive, or out-of-range values before this business-rule method runs.

**`edit(id, changes)`**

1. Load the flight using `find`; return 404 if the ID does not exist.
2. Update price only when the nullable patch field is present.
3. Update status only when supplied. Because this endpoint changes these two fields only, it cannot accidentally overwrite schedule values.
4. Save once and return a DTO.

### Book and cancel a seat — `BookingServiceImpl`

Read [BookingServiceImpl.java](../booking-service/src/main/java/com/airline/booking/service/BookingServiceImpl.java). The short numbered summary in section 5 describes each method; these steps expand the critical database behavior.

**`book(userId, request)`**

1. `@Transactional` opens a transaction for the Booking database work performed in this method.
2. `lookupFlight(request.flightId())` calls the Flight service over HTTP using its Eureka service name. If Flight is unreachable, booking stops with 503; there is no guessed local schedule.
3. Confirm the flight status is OPEN and its departure is still in the future.
4. Confirm the requested seat is at least 1 and no greater than the capacity returned by Flight.
5. Build a Booking with the authenticated user ID, flight ID, selected seat, and a copy of the current fare. Saving the fare makes old receipts stable if an admin later edits the flight's current price.
6. Save and flush the booking to make its generated ID available before inserting the seat row.
7. Insert a `SeatAllocation` for flight plus seat and flush it. MySQL's unique constraint is the final race-safe check even if two users saw the seat as free a moment earlier.
8. Convert a duplicate allocation into HTTP 409 Conflict. The transaction is rolled back, so the first booking insert is not left orphaned.
9. Return the safe booking DTO. The HTTP controller marks new bookings as 201 Created.

**`cancel(userId, bookingId)`**

1. Start a local Booking transaction and look up the booking; missing ID returns 404.
2. Compare its stored owner ID with the ID from the verified token. A mismatch returns 403 before any write.
3. Reject a booking already in `CANCELLED` state with 409, avoiding confusing repeat cancellation.
4. Look up the flight through Flight and reject cancellation after departure under the demo's rule.
5. Change status to `CANCELLED` and delete the active seat allocation.
6. Save the booking and return its response. Both local changes commit together; the booking remains history while the seat becomes available.

### Angular request-to-screen flow

Read each linked `.ts`, then the matching `.html`, and finally its `.css` file in [the component inventory above](#8-angular-page-components-and-external-templates).

**Login or registration** — [login.component.ts](../airline-frontend/src/app/features/auth/login.component.ts)

1. `FormBuilder.nonNullable.group` creates string controls; `required`, `email`, and `minLength(8)` are client-side checks for fast feedback.
2. `toggle()` switches modes and clears old messages. The name is required in registration logic but is optional for login.
3. `submit()` marks the attempt, clears prior feedback, and returns early if the form is invalid or a registration name contains only spaces.
4. `getRawValue()` reads the form, then `busy` disables repeat submissions while the HTTP request is pending.
5. Registration calls `AuthService.register`; success changes back to login mode and invites the customer to sign in.
6. Login calls `AuthService.login`; the service stores the returned token, role, and name, then the component routes Admin to `/admin` and Customer to `/flights`.
7. Each subscription clears `busy` on success or failure and shows a short message. Backend validation and authorization still apply even when a user bypasses the browser form.

**Flight search** — [flights.component.ts](../airline-frontend/src/app/features/flights/flights.component.ts)

1. `ngOnInit()` starts the first search when the router creates the page.
2. `search()` turns on loading, clears the prior error, and reads origin/destination/date from the form.
3. It trims each optional field and omits blank query parameters; the GET request therefore works for all flights or any combination of filters.
4. The success callback replaces the `flights` signal and ends loading. Angular rerenders `@for` rows from the updated signal.
5. On failure it shows a useful backend/connectivity hint and ends loading. `formatDate` / `formatTime` select display portions from the ISO response string.

**Seat selection** — [book.component.ts](../airline-frontend/src/app/features/bookings/book.component.ts)

1. `ngOnInit()` reads the `id` route parameter and requests flight details.
2. Once capacity arrives, it adds a maximum seat validator and recalculates control validity; the valid range is now 1 through the real plane capacity.
3. It separately loads currently occupied seats. This improves the screen but can become stale if another customer books concurrently.
4. `requestBooking()` checks that flight data exists, the form is valid, and the seat is absent from the latest occupied list. It records `pendingSeat` instead of immediately sending a booking.
5. The HTML passes the seat and fare text to the shared confirmation modal. Confirm calls `book`; Cancel clears the pending choice.
6. `book()` protects against missing state, turns on the pending indicator, and POSTs only the flight ID and seat number. The Auth interceptor supplies the token.
7. Success navigates to booking history. A 409 error explains the race and reloads occupied seats; other errors use the server message when available.

**Profile and admin pages** — [profile.component.ts](../airline-frontend/src/app/features/profile/profile.component.ts) and [admin.component.ts](../airline-frontend/src/app/features/admin/admin.component.ts)

1. Profile loads `/api/auth/me`, populates name in a reactive form, and leaves email read-only in the template.
2. The required, maximum-length, and non-whitespace validators prevent common mistakes. `save()` trims the value, sends only the name, and exposes loading/success/error feedback.
3. Admin initialization loads flights, customers, and bookings so each tab has data ready when selected.
4. Customer search trims text and sends it as a query parameter. The backend decides which matching rows are safe to return.
5. Booking filters reject a non-positive or non-integer ID locally, then send optional flight/status values. `clearBookingFilters()` resets and reloads.
6. Flight creation checks required form fields and cross-field city/time rules before POST. The backend repeats the important rules authoritatively.
7. Sales open/close uses `pendingFlight` to show the shared responsive confirmation modal, then sends only the new status in a PATCH.

### How the component HTML and CSS connect

1. `templateUrl: './name.component.html'` tells Angular where the component's markup lives. Moving HTML out of the decorator makes TypeScript easier to scan and gives the template editor normal HTML formatting.
2. `styleUrl: './name.component.css'` attaches local styles to that component. A class such as `.search-form` styles only markup in its component rather than changing every page.
3. In HTML, `{{ expression }}` displays component data; `[disabled]` / `[value]` pass a property into an element; `(click)` and `(ngSubmit)` call component methods; `formControlName` connects an input to the TypeScript form.
4. `@if` chooses loading, empty, error, or content states. `@for (...; track item.id)` renders rows using stable database IDs.
5. The shared `src/styles.css` defines repeated controls and layout; component CSS handles page-specific spacing and responsive changes. The confirmation modal also handles Escape/backdrop dismissal and moves to a bottom sheet on small screens.

## 15. Project guide index

- Start visually with [PROJECT_ARCHITECTURE.md](PROJECT_ARCHITECTURE.md).
- Install and run it using [SETUP_AND_DEMO_GUIDE.md](SETUP_AND_DEMO_GUIDE.md).
- For scope, schema and team responsibilities, read [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md).
- The acceptance walkthrough is [demo-checklist.md](../qa/demo-checklist.md).

