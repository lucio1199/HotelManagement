import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {HeaderComponent} from './components/header/header.component';
import {FooterComponent} from './components/footer/footer.component';
import {HomeComponent} from './components/home/home.component';
import {LoginComponent} from './components/login/login.component';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {httpInterceptorProviders} from './interceptors';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import {MatCardModule} from "@angular/material/card";
import {MatDividerModule} from "@angular/material/divider";
import {MatIconModule} from "@angular/material/icon";
import {MatToolbarModule} from "@angular/material/toolbar";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {MatButtonModule} from "@angular/material/button";
import {MatSnackBarModule} from "@angular/material/snack-bar";
import {MatMenu, MatMenuItem, MatMenuTrigger} from "@angular/material/menu";
import {MatDialogModule} from '@angular/material/dialog';
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {ToastrModule} from "ngx-toastr";
import {CheckInComponent} from './components/check-in/check-in.component';
import {MyRoomComponent} from './components/my-room/my-room.component';
import {MatSelectModule} from '@angular/material/select';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {DatePipe} from '@angular/common';
import {GuestListComponent} from './components/guest-list/guest-list.component';

import {MatListItem, MatNavList} from "@angular/material/list";
import {
    MatDatepickerToggle,
    MatDateRangeInput,
    MatDateRangePicker,
    MatEndDate,
    MatStartDate
} from "@angular/material/datepicker";
import {MatTab, MatTabGroup} from "@angular/material/tabs";
import {
  ActivityTimeslotListComponent
} from "./components/activity/activity-timeslot-list/activity-timeslot-list.component";
import {MatPaginator} from "@angular/material/paginator";

@NgModule({ declarations: [
        AppComponent,
        HeaderComponent,
        FooterComponent,
        LoginComponent,
        CheckInComponent,
        MyRoomComponent,
    ],
    bootstrap: [AppComponent],
  imports: [
    BrowserModule,
    AppRoutingModule,
    ReactiveFormsModule,
    NgbModule,
    FormsModule,
    MatCardModule,
    MatDividerModule,
    MatIconModule,
    MatToolbarModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSnackBarModule,
    MatMenuTrigger,
    MatMenu,
    MatMenuItem,
    MatDialogModule,
    MatNavList,
    MatListItem,
    MatSelectModule,
    BrowserAnimationsModule,
    ToastrModule.forRoot({
      timeOut: 5000,
      positionClass: 'toast-top-right',
      preventDuplicates: true,
      closeButton: true,
    }),
    MatDateRangeInput,
    MatDateRangePicker,
    MatDatepickerToggle,
    MatEndDate,
    MatStartDate,
    HomeComponent,
    MatDatepickerModule,
    MatTabGroup,
    MatTab,
    GuestListComponent,
    ActivityTimeslotListComponent,
    MatPaginator
  ], providers: [DatePipe, httpInterceptorProviders, provideHttpClient(withInterceptorsFromDi()), provideAnimationsAsync('noop')] })
export class AppModule {
}
