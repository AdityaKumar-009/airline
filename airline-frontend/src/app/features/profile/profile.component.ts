import { Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { AuthService } from '../../core/auth.service';
import { UserProfile } from '../../core/models';
import { NAME_PATTERN } from '../../core/form-validators';

@Component({
  standalone: true,
  styleUrl: './profile.component.css',
  imports: [ReactiveFormsModule],
  templateUrl: './profile.component.html'
})
export class ProfileComponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly fb = inject(FormBuilder);
  readonly profile = signal<UserProfile | null>(null);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly passwordSaving = signal(false);
  readonly error = signal('');
  readonly success = signal('');
  readonly passwordError = signal('');
  readonly currentPasswordError = signal('');
  readonly passwordSuccess = signal('');
  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(100), Validators.pattern(NAME_PATTERN)]]
  });
  readonly passwordForm = this.fb.nonNullable.group({
    currentPassword: ['', Validators.required],
    newPassword: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]]
  });

  ngOnInit(): void {
    this.auth.profile().subscribe({
      next: user => {
        this.profile.set(user);
        this.form.controls.name.setValue(user.name);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Could not load your profile. Please sign in again.');
        this.loading.set(false);
      }
    });
  }

  save(): void {
    this.error.set('');this.success.set('');
    this.form.markAllAsTouched();
    if (this.form.invalid) return;
    this.saving.set(true);
    this.auth.updateName(this.form.controls.name.value.trim()).subscribe({
      next: user => {
        this.profile.set(user);
        this.form.controls.name.setValue(user.name);
        this.success.set('Profile updated.');
        this.saving.set(false);
      },
      error: response => {
        this.error.set(response.error?.message || 'Could not update your profile.');
        this.saving.set(false);
      }
    });
  }

  updatePassword(): void {
    this.passwordError.set('');this.passwordSuccess.set('');
    this.currentPasswordError.set('');
    this.passwordForm.markAllAsTouched();
    if (this.passwordForm.invalid || this.passwordsSame()) return;
    this.passwordSaving.set(true);
    const v = this.passwordForm.getRawValue();
    this.auth.changePassword(v.currentPassword, v.newPassword).subscribe({
      next: () => {
        this.passwordSaving.set(false);
        this.passwordForm.reset();
        this.passwordSuccess.set('Password changed. Use your new password on your next login.');
      },
      error: response => {
        this.passwordSaving.set(false);
        const message = response.error?.message || 'Could not update password.';
        if (message.includes('Current password')) this.currentPasswordError.set(message);
        else this.passwordError.set(message);
      }
    });
  }

  passwordsSame(): boolean {
    const {currentPassword, newPassword} = this.passwordForm.getRawValue();
    return !!newPassword && currentPassword === newPassword;
  }
}

