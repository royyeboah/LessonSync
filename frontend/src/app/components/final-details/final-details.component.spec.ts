import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { FinalDetailsComponent } from './final-details.component';
import { environment } from '../../../environments/environment';

describe('FinalDetailsComponent', () => {
  let component: FinalDetailsComponent;
  let fixture: ComponentFixture<FinalDetailsComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FinalDetailsComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FinalDetailsComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
    // Loading the lectures triggers a follow-up request for the timetable they belong to.
    httpMock.expectOne(`${environment.apiUrl}/lectures`).flush([]);
    httpMock.expectOne(`${environment.apiUrl}/timetable`)
      .flush({ name: '', startDate: '', endDate: '' });
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
