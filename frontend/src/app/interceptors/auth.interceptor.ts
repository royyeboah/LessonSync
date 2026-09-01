import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { API_BASE_URL } from '../api';
import { AuthService } from '../services/auth.service';

export const credentialsInterceptor: HttpInterceptorFn = (req, next) => {
  if (req.url.startsWith(API_BASE_URL)) {
    return next(req.clone({ withCredentials: true }));
  }
  return next(req);
};

export const authErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      const isAuthStatus = req.url.includes('/api/auth/status');
      if (error.status === 401 && !isAuthStatus) {
        authService.login(router.url || '/');
      }
      return throwError(() => error);
    })
  );
};
