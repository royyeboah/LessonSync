import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';

/**
 * Keeps the timetable pages out of reach until a Google account has been connected, so the user
 * never gets halfway through an upload only to be bounced by a 401.
 */
export const googleConnectedGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return authService.loadStatus().pipe(
    map(status =>
      status.connected
        ? true
        : router.createUrlTree(['/'], { queryParams: { google_error: 'not_connected' } })
    )
  );
};
