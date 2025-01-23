import { Injectable } from '@angular/core';
import {Observable} from 'rxjs';
import {Lecture} from '../models/lecture.model';
import {Timetable} from '../models/timetable.model';
import {HttpClient, HttpParams} from '@angular/common/http';


@Injectable({
  providedIn: 'root'
})
export class LectureServiceService {

  constructor(private http: HttpClient) {}

  private apiUrl = 'http://localhost:8080';

  createLecture(file: File):Observable<any>{

    const formData = new FormData();
    formData.append('file', file);

    return this.http.post(`${this.apiUrl}/lecture`, formData);
  }

  getAllLectures():Observable<Lecture[]>{

    return this.http.get<Lecture[]>(`${this.apiUrl}/lectures`);
  }


  editLectures(id: string, updatedLecture: Partial<Lecture>): Observable<Lecture>{

    return this.http.put<Lecture>(`${this.apiUrl}/lecture/${id}`, updatedLecture);
  }

  submitLectures(reminderTime: number): Observable<string[]>{

    const params = new HttpParams()
      .append('reminderTime', reminderTime.toString());

    return this.http.get<string[]>(`${this.apiUrl}/createEvents`, { params })
  }

}
