import { Routes } from '@angular/router';
import {WelcomeComponent} from './components/welcome/welcome.component';
import {UploadComponent} from './components/upload/upload.component';
import {FinalDetailsComponent} from './components/final-details/final-details.component';

export const routes: Routes = [

  {path: '', component: WelcomeComponent},
  {path:'upload', component: UploadComponent},
  {path: 'final', component: FinalDetailsComponent}
];
