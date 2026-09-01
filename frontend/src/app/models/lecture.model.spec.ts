import { Lecture } from './lecture.model';

describe('Lecture', () => {
  it('should describe a lecture record', () => {
    const lecture: Lecture = {
      showStartTimeInput: false,
      showEndTimeInput: false,
      id: '1',
      day: 'Monday',
      course: 'CSM 387',
      location: 'SCB-SF8',
      lecturerName: 'D. ASAMOAH',
      groupName: '1',
      start_time: '08:00',
      end_time: '08:55',
      colorId: 1
    };
    expect(lecture.course).toBe('CSM 387');
  });
});
