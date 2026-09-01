import { Timetable } from './timetable.model';

describe('Timetable', () => {
  it('should describe a timetable record', () => {
    const timetable: Timetable = {
      name: 'First Year First Semester',
      startDate: '2026-01-12',
      endDate: '2026-05-01'
    };
    expect(timetable.name).toBe('First Year First Semester');
  });
});
