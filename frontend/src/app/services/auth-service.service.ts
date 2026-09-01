import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AuthStatus {
  authenticated: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class AuthServiceService {

  constructor(private http: HttpClient) {}

  private apiUrl = 'http://localhost:8080';

  getStatus(): Observable<AuthStatus> {
    return this.http.get<AuthStatus>(`${this.apiUrl}/auth/status`);
  }

  /**
   * Sends the browser through the backend's Google OAuth flow. After consent,
   * the backend redirects back to the frontend at the given path.
   */
  login(returnTo: string = '/upload'): void {
    window.location.href = `${this.apiUrl}/auth/google/login?returnTo=${encodeURIComponent(returnTo)}`;
  }

  logout(): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/auth/logout`, null);
  }
}
