import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

export const customerGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.token()) return router.createUrlTree(['/login']);
  if (auth.role() === 'CUSTOMER') return true;
  return router.createUrlTree([auth.role() === 'ADMIN' ? '/admin' : '/login']);
};

export const adminGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.token()) return router.createUrlTree(['/login']);
  if (auth.role() === 'ADMIN') return true;
  return router.createUrlTree([auth.role() === 'CUSTOMER' ? '/flights' : '/login']);
};
