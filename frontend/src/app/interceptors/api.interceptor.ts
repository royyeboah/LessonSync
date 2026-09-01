import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

const API_URL = 'http://localhost:8080';

/**
 * Sends the session cookie with every backend request and, when the backend
 * answers 401 (no valid Google credential), routes the user through the
 * Google sign-in flow before returning them to the current page.
 */
export const apiInterceptor: HttpInterceptorFn = (req, next) => {
  if (!req.url.startsWith(API_URL)) {
    return next(req);
  }

  return next(req.clone({ withCredentials: true })).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !req.url.includes('/auth/')) {
        const returnTo = encodeURIComponent(window.location.pathname);
        window.location.href = `${API_URL}/auth/google/login?returnTo=${returnTo}`;
      }
      return throwError(() => error);
    })
  );
};
