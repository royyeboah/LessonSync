import { Routes } from '@angular/router';
import {WelcomeComponent} from './components/welcome/welcome.component';
import {UploadComponent} from './components/upload/upload.component';

export const routes: Routes = [

  {path: '', component: WelcomeComponent},
  {path:'upload', component: UploadComponent}
];
