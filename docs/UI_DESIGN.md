# UI design guide

The Angular interface uses plain CSS with a white background, neutral grays, blue primary actions, and thin borders. The layout takes inspiration from Apple's restrained typography and spacing. There are no shadows, gradients, external fonts, or new UI dependencies.

## Where to change the design

| File | Responsibility |
| --- | --- |
| `airline-frontend/src/styles.css` | Shared colors, typography, spacing, controls, validation messages, tables, and focus indicators |
| `airline-frontend/src/app/app.component.css` | Navigation, page height, footer, and mobile header |
| Each feature's `*.component.css` | That page's grid and responsive layout |
| `airline-frontend/src/app/shared/confirm-modal.component.css` | Confirmation dialog, backdrop, and stacked mobile actions |

Templates remain in separate HTML files. Form validation, service calls, route guards, and backend rules remain in their existing TypeScript and Java files.

## Rules for adding a page

- Use the colors in `:root`. Change `--blue` to update the primary accent throughout the app.
- Use spacing in multiples of 8px: `.5rem`, `1rem`, `1.5rem`, `2rem`, `3rem`, and `4rem`. The base font size is 16px.
- Use `.intro` for the page heading, `.panel` for grouped content, and `.section-heading` for a heading with a secondary action or count.
- Use `.form-stack` for vertically stacked fields and `.form-field` to keep a label, control, and its error together.
- Reuse `.primary`, `.secondary`, `.text-button`, `.field-error`, `.error`, and `.success` instead of adding another button or feedback style.
- Keep inputs at least 48px high and ordinary action buttons at least 44px high. Keep keyboard focus outlines visible.
- Use `minmax(0, 1fr)` for flexible grid columns. It lets a column shrink without forcing the whole page wider.
- Put wide tables in `.table-wrap`, with `tabindex="0"` and a descriptive `aria-label`, so users can scroll the table with a keyboard or touch.

## Responsive behavior

The main container stops growing at 1120px. Its side margins adjust with `--gutter`.

| Width | Layout behavior |
| --- | --- |
| Above 1024px | Flights use one row; the admin overview has four columns |
| 901–1024px | Flight details wrap into two rows |
| 641–900px | Flight search uses two columns; navigation hides the account name |
| At or below 800px | Seat selection and passenger details stack |
| At or below 760px | Profile sections stack; admin forms and metrics use two columns |
| At or below 640px | The navigation wraps into two rows; search and admin forms use one column |
| At or below 480px | Modal actions stack vertically |
| At or below 400px | Reservation filters stack and the seat map uses smaller gaps |

Large tables and admin section tabs may scroll inside their own containers. The page itself should not scroll horizontally.

## Verification for this redesign

The frontend production build and live browser checks covered login and registration errors, flight search, profile validation, seat selection, reservation review, existing bookings and itinerary, admin forms, customer and reservation tables, and confirmation dialogs. Layouts were inspected at 320, 390, 768, 1024, and 1440 CSS pixels across these views. The checks found no page-level horizontal overflow or rendered CSS shadows. Print-media inspection confirmed that the itinerary hides the header, footer, and action buttons.

This was a UI regression check against the running local services. It does not replace the backend integration suite. No customer profile, password, flight, or reservation was changed during these checks.
