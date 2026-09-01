import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { API_BASE_URL } from '../api';
import { AuthStatus } from '../models/auth-status.model';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('fetches the current Google sign-in status from the backend', () => {
    const status: AuthStatus = {
      authenticated: true,
      name: 'Student',
      email: 'student@example.com',
      picture: null
    };

    service.getStatus().subscribe((result) => {
      expect(result).toEqual(status);
    });

    const request = httpMock.expectOne(`${API_BASE_URL}/api/auth/status`);
    expect(request.request.method).toBe('GET');
    request.flush(status);
  });

  it('builds the backend Google login URL for the current page', () => {
    expect(service.loginUrl('/upload')).toBe(
      `${API_BASE_URL}/api/auth/login?returnTo=%2Fupload`
    );
  });
});
