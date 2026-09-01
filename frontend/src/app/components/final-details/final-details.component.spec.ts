import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { provideRouter } from '@angular/router';
import { FinalDetailsComponent } from './final-details.component';
import { LectureServiceService } from '../../services/lecture-service.service';
import { TimetableServiceService } from '../../services/timetable-service.service';

describe('FinalDetailsComponent', () => {
  let component: FinalDetailsComponent;
  let fixture: ComponentFixture<FinalDetailsComponent>;

  beforeEach(async () => {
    const lectureService = jasmine.createSpyObj('LectureServiceService', [
      'getAllLectures',
      'editLectures',
      'submitLectures'
    ]);
    lectureService.getAllLectures.and.returnValue(of([]));
    const timetableService = jasmine.createSpyObj('TimetableServiceService', [
      'getCurrentTimetable',
      'createCalendar'
    ]);
    timetableService.getCurrentTimetable.and.returnValue(of({
      name: '',
      startDate: '',
      endDate: ''
    }));

    await TestBed.configureTestingModule({
      imports: [FinalDetailsComponent],
      providers: [
        provideRouter([]),
        { provide: LectureServiceService, useValue: lectureService },
        { provide: TimetableServiceService, useValue: timetableService }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FinalDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
