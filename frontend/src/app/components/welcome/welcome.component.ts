import {Component, OnInit} from '@angular/core';
import {NgIf} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {AuthService, GoogleAuthStatus} from '../../services/auth.service';

@Component({
  selector: 'app-welcome',
  imports: [NgIf],
  templateUrl: './welcome.component.html',
  standalone: true,
  styleUrl: './welcome.component.css'
})
export class WelcomeComponent implements OnInit {

  status: GoogleAuthStatus | null = null;
  loading = true;
  errorMessage = '';

  constructor(private authService: AuthService,
              private route: ActivatedRoute,
              private router: Router) {
  }

  ngOnInit(): void {
    this.errorMessage = this.describeError(this.route.snapshot.queryParamMap.get('google_error'));
    this.authService.loadStatus().subscribe(status => {
      this.status = status;
      this.loading = false;
    });
  }

  connect(): void {
    this.authService.login();
  }

  disconnect(): void {
    this.authService.logout().subscribe(() => {
      this.status = { configured: true, connected: false, email: null };
    });
  }

  start(): void {
    this.router.navigateByUrl('/upload');
  }

  private describeError(code: string | null): string {
    switch (code) {
      case null:
        return '';
      case 'not_connected':
        return 'Connect your Google account first so we know where to put your timetable.';
      case 'session_expired':
        return 'Your Google session expired. Please connect again.';
      case 'access_denied':
        return 'Google access was declined. We need calendar permission to add your classes.';
      case 'invalid_state':
        return 'That sign-in link could not be verified. Please try connecting again.';
      case 'token_exchange_failed':
        return 'We could not complete the sign-in with Google. Please try again.';
      default:
        return 'Something went wrong while connecting to Google. Please try again.';
    }
  }
}
