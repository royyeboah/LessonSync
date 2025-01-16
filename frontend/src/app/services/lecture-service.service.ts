import { Injectable } from '@angular/core';
import {Observable} from 'rxjs';
import {Lecture} from '../models/lecture.model';
import {Timetable} from '../models/timetable.model';
import {HttpClient} from '@angular/common/http';


@Injectable({
  providedIn: 'root'
})
export class LectureServiceService {

  constructor(private http: HttpClient) {}

  private apiUrl = 'http://localhost:8080';

  createLecture(file: File):Observable<Timetable>{

    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<Timetable>(`${this.apiUrl}/lecture`, formData);
  }
}
