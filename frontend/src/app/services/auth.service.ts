import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of, tap } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export interface GoogleAuthStatus {
  /** False when the server itself has no OAuth client configured. */
  configured: boolean;
  connected: boolean;
  email: string | null;
}

const DISCONNECTED: GoogleAuthStatus = { configured: true, connected: false, email: null };

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly authUrl = `${environment.apiUrl}/auth/google`;

  private readonly statusSubject = new BehaviorSubject<GoogleAuthStatus | null>(null);

  /** Latest known status, or null until it has been loaded at least once. */
  readonly status$ = this.statusSubject.asObservable();

  constructor(private http: HttpClient) {}

  get status(): GoogleAuthStatus | null {
    return this.statusSubject.value;
  }

  get isConnected(): boolean {
    return this.statusSubject.value?.connected === true;
  }

  loadStatus(): Observable<GoogleAuthStatus> {
    return this.http.get<GoogleAuthStatus>(`${this.authUrl}/status`).pipe(
      catchError(() => of(DISCONNECTED)),
      tap(status => this.statusSubject.next(status))
    );
  }

  /**
   * Sends the browser to Google's consent screen. This has to be a top level navigation rather
   * than an XHR so that the session cookie is in place when Google redirects back.
   */
  login(): void {
    if (environment.production && !environment.apiUrl) {
      throw new Error('API_URL is not configured for this deployment.');
    }
    window.location.href = `${this.authUrl}/login`;
  }

  logout(): Observable<void> {
    return this.http.post<void>(`${this.authUrl}/logout`, {}).pipe(
      catchError(() => of(void 0)),
      map(() => {
        this.statusSubject.next(DISCONNECTED);
        return void 0;
      })
    );
  }

  /** Called by the interceptor when the API reports that the Google connection has lapsed. */
  markDisconnected(): void {
    this.statusSubject.next({
      configured: this.statusSubject.value?.configured ?? true,
      connected: false,
      email: null
    });
  }
}
