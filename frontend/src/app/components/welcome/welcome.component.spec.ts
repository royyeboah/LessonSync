import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { WelcomeComponent } from './welcome.component';
import { AuthService } from '../../services/auth.service';
import { AuthStatus } from '../../models/auth-status.model';

describe('WelcomeComponent', () => {
  let component: WelcomeComponent;
  let fixture: ComponentFixture<WelcomeComponent>;
  let authService: jasmine.SpyObj<AuthService>;

  const anonymous: AuthStatus = {
    authenticated: false,
    name: null,
    email: null,
    picture: null
  };

  beforeEach(async () => {
    authService = jasmine.createSpyObj('AuthService', ['getStatus', 'login', 'logout']);
    authService.getStatus.and.returnValue(of(anonymous));

    await TestBed.configureTestingModule({
      imports: [WelcomeComponent],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authService },
        { provide: ActivatedRoute, useValue: { snapshot: { queryParamMap: { get: () => null } } } }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(WelcomeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('shows the Google sign-in button when the student is not authenticated', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Sign in with Google');
    expect(compiled.textContent).not.toContain('+ Create TimeTable');
  });

  it('shows the create timetable action after Google sign-in', () => {
    authService.getStatus.and.returnValue(of({
      authenticated: true,
      name: 'Student',
      email: 'student@example.com',
      picture: null
    }));

    fixture = TestBed.createComponent(WelcomeComponent);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Signed in as student@example.com');
    expect(compiled.textContent).toContain('+ Create TimeTable');
  });
});
