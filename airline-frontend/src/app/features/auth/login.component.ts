import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly registering = signal(false);
  readonly submitted = signal(false);
  readonly busy = signal(false);
  readonly error = signal('');
  readonly success = signal('');

  readonly form = this.fb.nonNullable.group({
    name: [''],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]]
  });

  toggle(): void {
    this.registering.update(isRegistering => !isRegistering);
    this.submitted.set(false);
    this.error.set('');
    this.success.set('');
  }

  submit(): void {
    this.submitted.set(true);
    this.error.set('');
    this.success.set('');

    if (this.form.invalid || (this.registering() && !this.form.value.name?.trim())) return;

    const { name, email, password } = this.form.getRawValue();
    this.busy.set(true);

    if (this.registering()) {
      this.auth.register(name, email, password).subscribe({
        next: () => {
          this.busy.set(false);
          this.registering.set(false);
          this.success.set('Account created. Please log in.');
        },
        error: response => {
          this.busy.set(false);
          this.error.set(response.error?.message || 'Registration failed');
        }
      });
      return;
    }

    this.auth.login(email, password).subscribe({
      next: () => {
        this.busy.set(false);
        this.router.navigate([this.auth.role() === 'ADMIN' ? '/admin' : '/flights']);
      },
      error: () => {
        this.busy.set(false);
        this.error.set('Invalid email or password');
      }
    });
  }
}
