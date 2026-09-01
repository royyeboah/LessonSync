import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TimetableServiceService } from './timetable-service.service';

describe('TimetableServiceService', () => {
  let service: TimetableServiceService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(TimetableServiceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
