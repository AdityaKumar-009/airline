import { Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { AuthService } from '../../core/auth.service';
import { UserProfile } from '../../core/models';

@Component({
  standalone: true,
  styleUrl: './profile.component.css',
  imports: [ReactiveFormsModule],
  templateUrl: './profile.component.html'
})
export class ProfileComponent implements OnInit {
  private auth = inject(AuthService);
  private fb = inject(FormBuilder);
  profile = signal<UserProfile | null>(null);
  loading = signal(true);
  saving = signal(false);
  error = signal('');
  success = signal('');
  form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(100), Validators.pattern(/\S/)]]
  });

  ngOnInit() {
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

  save() {
    this.error.set('');
    this.success.set('');
    this.form.controls.name.markAsTouched();
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
}
