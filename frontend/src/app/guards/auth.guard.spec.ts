import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { authGuard } from './auth.guard';
import { AuthService } from '../services/auth.service';
import { AuthStatus } from '../models/auth-status.model';

describe('authGuard', () => {
  let authService: jasmine.SpyObj<AuthService>;

  beforeEach(() => {
    authService = jasmine.createSpyObj('AuthService', ['getStatus', 'login']);
    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authService },
        { provide: Router, useValue: { url: '/upload' } }
      ]
    });
  });

  it('allows navigation when the student is signed in with Google', (done) => {
    authService.getStatus.and.returnValue(of({
      authenticated: true,
      name: 'Student',
      email: 'student@example.com',
      picture: null
    } as AuthStatus));

    TestBed.runInInjectionContext(() => {
      const result = authGuard({} as never, {} as never);
      if (result instanceof Object && 'subscribe' in result) {
        result.subscribe((allowed) => {
          expect(allowed).toBeTrue();
          expect(authService.login).not.toHaveBeenCalled();
          done();
        });
      }
    });
  });

  it('starts Google login when the student is not signed in', (done) => {
    authService.getStatus.and.returnValue(of({
      authenticated: false,
      name: null,
      email: null,
      picture: null
    }));

    TestBed.runInInjectionContext(() => {
      const result = authGuard({} as never, {} as never);
      if (result instanceof Object && 'subscribe' in result) {
        result.subscribe((allowed) => {
          expect(allowed).toBeFalse();
          expect(authService.login).toHaveBeenCalledWith('/upload');
          done();
        });
      }
    });
  });
});
