import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Timetable} from '../models/timetable.model';

@Injectable({
  providedIn: 'root'
})
export class TimetableServiceService {

  constructor(private http: HttpClient) { }

  apiUrl: string = 'http://localhost:8080';

  createCalendar(timetable: Timetable): Observable<any>{

    return this.http.post(this.apiUrl + '/createCalendar', timetable, {
      responseType: 'text' as 'json'
    });
  }

  getCurrentTimetable(): Observable<Timetable>{

    return this.http.get<Timetable>(`${this.apiUrl}/timetable`)
  }

}
