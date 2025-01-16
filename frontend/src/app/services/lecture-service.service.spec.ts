import { TestBed } from '@angular/core/testing';

import { LectureServiceService } from './lecture-service.service';

describe('LectureServiceService', () => {
  let service: LectureServiceService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(LectureServiceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
