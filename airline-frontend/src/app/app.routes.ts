import { Routes } from '@angular/router';
import { adminGuard, customerGuard } from './core/auth.guard';
import { AdminComponent } from './features/admin/admin.component';
import { BookComponent } from './features/bookings/book.component';
import { MyBookingsComponent } from './features/bookings/my-bookings.component';
import { FlightsComponent } from './features/flights/flights.component';
import { LoginComponent } from './features/auth/login.component';
import { ProfileComponent } from './features/profile/profile.component';

export const routes: Routes = [
  { path: '', redirectTo: 'flights', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'flights', component: FlightsComponent },
  { path: 'flights/:id/book', component: BookComponent, canActivate: [customerGuard] },
  { path: 'my-bookings', component: MyBookingsComponent, canActivate: [customerGuard] },
  { path: 'profile', component: ProfileComponent, canActivate: [customerGuard] },
  { path: 'admin', component: AdminComponent, canActivate: [adminGuard] },
  { path: '**', redirectTo: 'flights' }
];
