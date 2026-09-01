import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../api';
import { AuthStatus } from '../models/auth-status.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  constructor(private http: HttpClient) {}

  getStatus(): Observable<AuthStatus> {
    return this.http.get<AuthStatus>(`${API_BASE_URL}/api/auth/status`);
  }

  loginUrl(returnTo: string = '/'): string {
    const params = new URLSearchParams({ returnTo });
    return `${API_BASE_URL}/api/auth/login?${params.toString()}`;
  }

  login(returnTo: string = '/'): void {
    window.location.href = this.loginUrl(returnTo);
  }

  logout(): void {
    window.location.href = `${API_BASE_URL}/logout`;
  }
}
