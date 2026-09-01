import {Component, OnInit} from '@angular/core';
import {NgIf} from '@angular/common';
import {ActivatedRoute, RouterLink} from '@angular/router';
import {AuthService} from '../../services/auth.service';
import {AuthStatus} from '../../models/auth-status.model';

@Component({
  selector: 'app-welcome',
  imports: [NgIf, RouterLink],
  templateUrl: './welcome.component.html',
  standalone: true,
  styleUrl: './welcome.component.css'
})
export class WelcomeComponent implements OnInit {

  status: AuthStatus | null = null;
  loading = true;
  oauthError = false;

  constructor(
    private authService: AuthService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.oauthError = this.route.snapshot.queryParamMap.get('error') === 'oauth';
    this.authService.getStatus().subscribe({
      next: (status) => {
        this.status = status;
        this.loading = false;
      },
      error: () => {
        this.status = {
          authenticated: false,
          name: null,
          email: null,
          picture: null
        };
        this.loading = false;
      }
    });
  }

  signIn(): void {
    this.authService.login('/upload');
  }

  signOut(): void {
    this.authService.logout();
  }

}
