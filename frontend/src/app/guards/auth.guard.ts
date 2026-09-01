import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return authService.getStatus().pipe(
    map((status) => {
      if (status.authenticated) {
        return true;
      }
      authService.login(router.url || '/upload');
      return false;
    })
  );
};
