import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { WelcomeComponent } from './welcome.component';
import { GoogleAuthStatus } from '../../services/auth.service';
import { environment } from '../../../environments/environment';

describe('WelcomeComponent', () => {
  let component: WelcomeComponent;
  let fixture: ComponentFixture<WelcomeComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [WelcomeComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    })
    .compileComponents();

    fixture = TestBed.createComponent(WelcomeComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  function renderWith(status: GoogleAuthStatus): string {
    fixture.detectChanges();
    httpMock.expectOne(`${environment.apiUrl}/auth/google/status`).flush(status);
    fixture.detectChanges();
    return (fixture.nativeElement as HTMLElement).textContent ?? '';
  }

  it('should create', () => {
    renderWith({ configured: true, connected: false, email: null });

    expect(component).toBeTruthy();
  });

  it('asks the student to connect Google before anything else', () => {
    const text = renderWith({ configured: true, connected: false, email: null });

    expect(text).toContain('Connect Google Calendar');
    expect(text).not.toContain('Create TimeTable');
  });

  it('shows the connected account and lets the student carry on', () => {
    const text = renderWith({ configured: true, connected: true, email: 'student@example.com' });

    expect(text).toContain('student@example.com');
    expect(text).toContain('Create TimeTable');
    expect(text).toContain('Disconnect');
  });

  it('says so when the server has no OAuth client configured', () => {
    const text = renderWith({ configured: false, connected: false, email: null });

    expect(text).toContain('not set up on this server');
    expect(text).not.toContain('Connect Google Calendar');
  });
});
