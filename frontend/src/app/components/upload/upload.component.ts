import {Component, OnInit} from '@angular/core';
import {Lecture} from '../../models/lecture.model';
import {Timetable} from '../../models/timetable.model';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {NgClass, NgIf, NgOptimizedImage} from '@angular/common';
import {LectureServiceService} from '../../services/lecture-service.service';
import {DomSanitizer, SafeUrl} from '@angular/platform-browser';
import {provideHttpClient} from '@angular/common/http';
import {Router} from '@angular/router';
import {TimetableServiceService} from '../../services/timetable-service.service';
import {switchMap} from 'rxjs';

@Component({
  selector: 'app-upload',
  imports: [
    ReactiveFormsModule,
    NgIf,
    FormsModule,
    NgOptimizedImage,
    NgClass,

  ],

  templateUrl: './upload.component.html',
  standalone: true,
  styleUrl: './upload.component.css'
})
export class UploadComponent{

  constructor(private sanitizer: DomSanitizer,
              private lectureService: LectureServiceService,
              private router: Router,
              private timetableService: TimetableServiceService) {
  }

  timetable: Timetable = {

    name: '',
    startDate: '',
    endDate: ''
  }

  loading: boolean = false;

  timetableFile: File | null = null;
  previewUrl: SafeUrl | null = null;


  onFileSelected(event: any) {
    this.timetableFile = event.target.files[0];

    if(this.timetableFile) {

      const unsafeUrl = URL.createObjectURL(this.timetableFile);
      this.previewUrl = this.sanitizer.bypassSecurityTrustUrl(unsafeUrl);
    }
    console.log(this.previewUrl);
  }

  removeFile(event: Event) {

    event.stopPropagation();
    event.preventDefault();
    this.previewUrl = null;
    this.timetableFile = null;
  }

  ngOnDestroy() {
    if(this.previewUrl){
      URL.revokeObjectURL(this.previewUrl as string)

    }
  }

  error: string = ""

  onClickSubmit() {

    if(!this.timetableFile){
      this.error = "Please upload a file";
      return;
    }


    this.loading = true;
    this.error = '';

    this.lectureService.createLecture(this.timetableFile).pipe(
      switchMap(()=> {

        this.timetable.startDate = new Date(this.timetable.startDate).toISOString().slice(0,19);
        this.timetable.endDate = new Date(this.timetable.endDate).toISOString().slice(0,19);
        console.log('Lecture created, creating calendar')
        return this.timetableService.createCalendar(this.timetable);
      })

      ).subscribe({
      next: ()=>{
        console.log("Calendar created, attempting navigation...")
        this.loading = false;
        this.router.navigateByUrl('/final').then(
          (navigated) => console.log('Navigation result:', navigated)
        )
      }
    })
  }

}
