import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { environment } from '../../environments/environment';

/**
 * Attaches the session cookie to every backend call and sends the user back to the landing page
 * when the backend reports that the Google connection is gone.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // In a same-origin production build apiUrl is empty, which makes every request an API request.
  const isApiRequest = req.url.startsWith(environment.apiUrl);
  const request = isApiRequest ? req.clone({ withCredentials: true }) : req;

  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      if (isApiRequest && error.status === 401) {
        authService.markDisconnected();
        router.navigate(['/'], { queryParams: { google_error: 'session_expired' } });
      }
      return throwError(() => error);
    })
  );
};
