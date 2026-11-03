import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { EMAIL_DOMAIN_PATTERN, NAME_PATTERN } from '../../core/form-validators';

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
    email: ['', [Validators.required, Validators.email, Validators.pattern(EMAIL_DOMAIN_PATTERN), Validators.maxLength(150)]],
    password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]]
  });

  toggle(): void {
    this.registering.update(isRegistering => !isRegistering);
    this.form.controls.name.setValidators(this.registering()
      ? [Validators.required, Validators.maxLength(100), Validators.pattern(NAME_PATTERN)] : []);
    this.form.controls.name.updateValueAndValidity();
    this.submitted.set(false);
    this.error.set('');
    this.success.set('');
  }

  submit(): void {
    this.submitted.set(true);
    this.error.set('');
    this.success.set('');
    this.form.markAllAsTouched();

    if (this.form.invalid) return;

    const { name, email, password } = this.form.getRawValue();
    this.busy.set(true);

    if (this.registering()) {
      this.auth.register(name.trim(), email.trim(), password).subscribe({
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
