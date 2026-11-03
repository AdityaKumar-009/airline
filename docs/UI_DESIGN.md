# AirlineMS UI design

The interface follows the selected **Heritage Burgundy** direction: warm ivory surfaces, a burgundy navigation bar and primary actions, restrained champagne accents, fine borders, and quiet travel photography. It uses plain CSS, the existing Angular components, and no extra UI library. Cards and dialogs have no drop shadows.

## Shared visual system

The design tokens live in `airline-frontend/src/styles.css`:

| Token | Purpose |
| --- | --- |
| `--page`, `--surface`, `--soft` | Ivory page, white card, and warm secondary surface |
| `--ink`, `--muted` | Main and supporting text |
| `--primary`, `--primary-dark` | Burgundy links, buttons, and focus states |
| `--gold`, `--gold-ink` | Champagne borders and readable gold labels |
| `--border` | Thin warm-gray outlines |

Use the shared `.panel`, `.primary`, `.secondary`, `.text-button`, `.field-error`, `.error`, and `.success` styles before creating another component style. Spacing follows simple half-rem steps. Inputs stay at least 48px tall; buttons stay at least 44px tall.

## Page artwork

The compressed JPEG backgrounds are stored under `airline-frontend/src/assets/images/` and served from `/assets/images/`:

| File | Page or section |
| --- | --- |
| `flight-search-art.jpg` | Flight search hero, featuring India Gate |
| `account-terminal-art.jpg` | Login page, airport terminal at sunrise |
| `city-itinerary-art.jpg` | Registration, bookings, and itinerary sections |
| `flight-window-art.jpg` | Profile hero and seat-booking flight summary |
| `admin-tower-art.jpg` | Administration page hero |

Artwork is decorative CSS background imagery. Text and controls remain real HTML, with a light gradient behind text for contrast. These JPEGs are optimized copies of the generated artwork, sized for the page layouts without adding a frontend dependency.

## Where to change the design

| File | Responsibility |
| --- | --- |
| `airline-frontend/src/styles.css` | Shared palette, typography, spacing, controls, validation, tables, and focus styles |
| `airline-frontend/src/app/app.component.css` | Burgundy navigation, app height, footer, and compact navigation |
| Each feature's `*.component.css` | Page artwork, grid, and responsive layout |
| `airline-frontend/src/app/shared/confirm-modal.component.css` | Confirmation dialog and mobile actions |

Templates remain in separate HTML files. Form validation, API calls, route guards, and backend rules stay in their existing TypeScript and Java files.

## Responsive layout

The shared page container stops growing at 1120px. Its side margins use `--gutter` so content remains readable on narrow devices.

| Width | Layout behavior |
| --- | --- |
| Above 1024px | Flight results fit in one row; dashboard metrics use four columns |
| 901–1024px | Flight details wrap into two rows |
| At or below 900px | Flight search switches to two columns |
| At or below 800px | Seat and passenger panels stack |
| At or below 760px | Login artwork and form stack; profile and admin grids tighten |
| At or below 640px | Page heroes become shorter, search/admin forms use one column, and navigation wraps |
| At or below 480px | Confirmation actions stack vertically |
| At or below 400px | Reservation filters and seat-map gaps compact |

Wide tables and admin tabs scroll inside their own regions, so they do not make the whole page wider. The itinerary print stylesheet hides navigation and action buttons.

## Accessibility and interaction

- Form-level validation and live error text remain in the templates.
- `:focus-visible` adds a burgundy keyboard focus ring.
- Buttons and links keep clear hover and disabled states.
- Background art is decorative; headings, labels, and action text remain accessible HTML.
- Confirmation dialogs continue to use the shared Angular modal component.

## Verification

The latest theme update was verified with a production Angular build and a live review against the local app.

- Production `npm run build` passed with the project's bundled Node.js 22.9.0 and Angular CLI 19.2.27.
- Both demo roles signed in successfully. The browser review covered flight search, login and registration, seat selection, profile, booking history, itinerary, admin overview, flight editor, customer and reservation tables, activity, and both cancellation prompts.
- The Delhi-to-Mumbai search returned the matching flight. Inline validation and disabled submit buttons were checked for login, registration, flight search, profile, seat booking, and the empty admin flight form.
- Responsive layouts were checked at 320, 390, 768, 1024, and 1440px. The checked pages had no document-level horizontal overflow and no computed CSS box shadows.
- All five new artwork URLs returned HTTP 200, and Angular copied them into the production output.
- No runtime or browser-console error events were observed during the monitored route check. The dialogs were dismissed without confirming a cancellation, and no booking, flight, or profile write was submitted.

The frontend package does not define an automated test script or contain `*.spec.ts` files, so this review used the production build and live browser checks. It does not replace a backend integration suite for booking creation or cancellation.
