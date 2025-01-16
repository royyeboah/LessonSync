import { TestBed } from '@angular/core/testing';

import { TimetableServiceService } from './timetable-service.service';

describe('TimetableServiceService', () => {
  let service: TimetableServiceService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TimetableServiceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
