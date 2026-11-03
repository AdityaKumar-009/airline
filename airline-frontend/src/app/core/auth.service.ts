import { HttpClient } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';
import { tap } from 'rxjs';
import { UserProfile, UserRole } from './models';

interface LoginResponse {
  token: string;
  name: string;
  role: UserRole;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  readonly token = signal(sessionStorage.getItem('airline_token'));
  readonly role = signal<UserRole | null>(sessionStorage.getItem('airline_role') as UserRole | null);
  readonly name = signal(sessionStorage.getItem('airline_name'));

  constructor(private readonly http: HttpClient) {}

  login(email: string, password: string) {
    return this.http.post<LoginResponse>('/api/auth/login', { email, password }).pipe(
      tap(response => {
        sessionStorage.setItem('airline_token', response.token);
        sessionStorage.setItem('airline_role', response.role);
        sessionStorage.setItem('airline_name', response.name);

        this.token.set(response.token);
        this.role.set(response.role);
        this.name.set(response.name);
      })
    );
  }

  register(name: string, email: string, password: string) {
    return this.http.post('/api/auth/register', { name, email, password });
  }

  profile() {
    return this.http.get<UserProfile>('/api/auth/me');
  }

  updateName(name: string) {
    return this.http.patch<UserProfile>('/api/auth/me', { name }).pipe(
      tap(user => {
        sessionStorage.setItem('airline_name', user.name);
        this.name.set(user.name);
      })
    );
  }

  changePassword(currentPassword: string, newPassword: string) {
    return this.http.patch<void>('/api/auth/me/password', {currentPassword, newPassword});
  }

  logout(): void {
    const keys = ['airline_token', 'airline_role', 'airline_name'];
    keys.forEach(key => sessionStorage.removeItem(key));

    this.token.set(null);
    this.role.set(null);
    this.name.set(null);
  }
}

