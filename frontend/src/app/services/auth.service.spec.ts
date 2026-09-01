import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  const statusUrl = `${environment.apiUrl}/auth/google/status`;
  const logoutUrl = `${environment.apiUrl}/auth/google/logout`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
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

  it('starts out not knowing the status', () => {
    expect(service.status).toBeNull();
    expect(service.isConnected).toBeFalse();
  });

  it('remembers a connected account', () => {
    service.loadStatus().subscribe();
    httpMock.expectOne(statusUrl)
      .flush({ configured: true, connected: true, email: 'student@example.com' });

    expect(service.isConnected).toBeTrue();
    expect(service.status?.email).toEqual('student@example.com');
  });

  it('treats an unreachable backend as disconnected rather than failing', () => {
    let emitted = false;
    service.loadStatus().subscribe(() => (emitted = true));
    httpMock.expectOne(statusUrl).error(new ProgressEvent('network error'));

    expect(emitted).toBeTrue();
    expect(service.isConnected).toBeFalse();
  });

  it('drops the connection on logout', () => {
    service.loadStatus().subscribe();
    httpMock.expectOne(statusUrl)
      .flush({ configured: true, connected: true, email: 'student@example.com' });

    service.logout().subscribe();
    httpMock.expectOne(logoutUrl).flush(null);

    expect(service.isConnected).toBeFalse();
    expect(service.status?.email).toBeNull();
  });

  it('drops the connection when the API reports the session has lapsed', () => {
    service.loadStatus().subscribe();
    httpMock.expectOne(statusUrl)
      .flush({ configured: true, connected: true, email: 'student@example.com' });

    service.markDisconnected();

    expect(service.isConnected).toBeFalse();
  });
});
