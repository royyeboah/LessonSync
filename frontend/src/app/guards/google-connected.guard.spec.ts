import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree, provideRouter } from '@angular/router';
import { Observable, isObservable } from 'rxjs';

import { googleConnectedGuard } from './google-connected.guard';
import { environment } from '../../environments/environment';

describe('googleConnectedGuard', () => {
  let httpMock: HttpTestingController;

  const statusUrl = `${environment.apiUrl}/auth/google/status`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    });
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  function runGuard(): Observable<boolean | UrlTree> {
    const result = TestBed.runInInjectionContext(() =>
      googleConnectedGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot));

    if (!isObservable(result)) {
      throw new Error('expected the guard to return an observable');
    }
    return result as Observable<boolean | UrlTree>;
  }

  it('lets a connected student through', () => {
    let outcome: boolean | UrlTree | undefined;
    runGuard().subscribe(value => (outcome = value));
    httpMock.expectOne(statusUrl)
      .flush({ configured: true, connected: true, email: 'student@example.com' });

    expect(outcome).toBeTrue();
  });

  it('sends an unconnected student back to the landing page', () => {
    let outcome: boolean | UrlTree | undefined;
    runGuard().subscribe(value => (outcome = value));
    httpMock.expectOne(statusUrl).flush({ configured: true, connected: false, email: null });

    expect(outcome instanceof UrlTree).toBeTrue();
    expect((outcome as UrlTree).toString()).toContain('google_error=not_connected');
  });

  it('blocks when the status cannot be read', () => {
    let outcome: boolean | UrlTree | undefined;
    runGuard().subscribe(value => (outcome = value));
    httpMock.expectOne(statusUrl).error(new ProgressEvent('network error'));

    expect(outcome instanceof UrlTree).toBeTrue();
  });
});
