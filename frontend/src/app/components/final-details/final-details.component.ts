import {Component, OnInit} from '@angular/core';
import {LectureServiceService} from '../../services/lecture-service.service';
import {Lecture} from '../../models/lecture.model';
import {DatePipe, KeyValue, KeyValuePipe, NgForOf, NgIf} from '@angular/common';
import {FormsModule} from '@angular/forms';

@Component({
  selector: 'app-final-details',
  imports: [
    NgForOf,
    DatePipe,
    KeyValuePipe,
    FormsModule,
    NgIf
  ],
  templateUrl: './final-details.component.html',
  standalone: true,
  styleUrl: './final-details.component.css'
})
export class FinalDetailsComponent implements OnInit{

  constructor(private lectureService: LectureServiceService) {
  }

  lectures: Lecture[] = []

  lecturesMap: Map<string, Lecture[]> = new Map();

  collapsedSections: Set<string> = new Set();

  trackByFn(index: number, item: KeyValue<string, Lecture[]>): string {
    return item.key;
  }

  isCollapsed(key: string): boolean {

    return this.collapsedSections.has(key);
  }

  toggleCollapse(key: string): void{

    if(this.collapsedSections.has(key)) {
      this.collapsedSections.delete(key)
    } else{
    this.collapsedSections.add(key)}
  }


  ngOnInit(): void {
    this.getAvailableLectures();
  }

  getAvailableLectures(): void {
    this.lectureService.getAllLectures().subscribe(
      {
        next: (data) => {
          this.lectures = data;
          this.groupLecturesByName();
        },
        error: (error) => {
          console.error('Error fetching lectures:', error);
        }
      }
    );
  }

  formatTime(time: string): string {

    if(!time) return '';

    const timeRegex = /^([01]\d|2[0-3]):([0-5]\d)$/;
    if (timeRegex.test(time)){
      return time;
    }

    const date = new Date(`1970-01-01T${time}`);
    if(!isNaN(date.getTime())){

      const hours = date.getHours().toString().padStart(2, '0');
      const minutes = date.getMinutes().toString().padStart(2, '0');

      return `${hours}:${minutes}`
    }

    return '';
  }

  groupLecturesByName(): void {
    this.lectures.forEach(
      (lecture) => {
        if(!this.lecturesMap.has(lecture.course)){
          this.lecturesMap.set(lecture.course, [])
        }
        this.lecturesMap.get(lecture.course)?.push(lecture);

      }
    )
  }

  updateLecturerName(event: Event, lectures: Lecture[]): void {

    const inputElement = event.target as HTMLInputElement;

    lectures.forEach(
      lecture => {
        lecture.lecturerName = inputElement.value;
      }
    );
  }

  validateTimes(lecture: Lecture): void {
    const start = new Date(`1970-01-01T${lecture.start_time}`)
    const end = new Date(`1970-01-01T${lecture.end_time}`)

    if(end <= start){
      const newEnd = new Date(start);
      newEnd.setHours(newEnd.getHours()+1);
      lecture.end_time = newEnd.toTimeString().slice(0,5);
    }
  }

  updateCourseKey(oldKey: string, newKey: string, lectures: Lecture[]): void {
    if (oldKey === newKey || !newKey.trim()) return;

    // Update collapsed state if necessary
    if (this.collapsedSections.has(oldKey)) {
      this.collapsedSections.delete(oldKey);
      this.collapsedSections.add(newKey);
    }

    this.lecturesMap.delete(oldKey);
    this.lecturesMap.set(newKey, lectures);

    lectures.forEach(lecture => {
      lecture.course = newKey;
    });

    this.lecturesMap = new Map(this.lecturesMap)
  }

  // Toggle visibility for start time input
  toggleStartTimeInput(lecture: Lecture): void {
    lecture.showStartTimeInput = true;
  }

// Hide start time input on blur
  hideStartTimeInput(lecture: Lecture): void {
    lecture.showStartTimeInput = false;
  }

// Toggle visibility for end time input
  toggleEndTimeInput(lecture: Lecture): void {
    lecture.showEndTimeInput = true;
  }

// Hide end time input on blur
  hideEndTimeInput(lecture: Lecture): void {
    lecture.showEndTimeInput = false;
  }

// Format time from 24-hour to 12-hour format
  formatTimeTo12Hour(time: string): string {
    if (!time) return '';
    const [hours, minutes] = time.split(':').map(Number);
    const period = hours >= 12 ? 'PM' : 'AM';
    const hour12 = hours % 12 || 12; // Convert 0 or 12 to 12 for AM/PM
    return `${hour12.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')} ${period}`;
  }


}
