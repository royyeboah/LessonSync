import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

import { LectureServiceService } from './lecture-service.service';

describe('LectureServiceService', () => {
  let service: LectureServiceService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(LectureServiceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
