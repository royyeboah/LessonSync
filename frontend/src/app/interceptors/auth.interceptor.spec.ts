import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { credentialsInterceptor } from './auth.interceptor';
import { API_BASE_URL } from '../api';

describe('credentialsInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([credentialsInterceptor])),
        provideHttpClientTesting()
      ]
    });
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('sends the session cookie to the LessonSync API', () => {
    http.get(`${API_BASE_URL}/api/auth/status`).subscribe();

    const request = httpMock.expectOne(`${API_BASE_URL}/api/auth/status`);
    expect(request.request.withCredentials).toBeTrue();
    request.flush({ authenticated: false, name: null, email: null, picture: null });
  });
});
