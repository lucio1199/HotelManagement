import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {HomeComponent} from './components/home/home.component';
import {LoginComponent} from './components/login/login.component';
import {RoomListComponent} from "./components/room/room-list/room-list.component";
import {ActivityListComponent} from "./components/activity/activity-list/activity-list.component";
import {ActivityDetailComponent} from "./components/activity/activity-detail/activity-detail.component";
import {
  ActivityCreateEditComponent, ActivityCreateEditMode
} from "./components/activity/activity-create-edit/activity-create-edit.component";
import {RoomCleaningListComponent} from "./components/room/room-cleaning-list/room-cleaning-list.component";
import {RoomDetailComponent} from "./components/room/room-detail/room-detail.component";
import {UiConfigComponent} from "./components/ui-config/ui-config.component";
import {CheckInComponent} from "./components/check-in/check-in.component";
import {MyRoomComponent} from "./components/my-room/my-room.component";
import {
  RoomCreateEditComponent,
  RoomCreateEditMode
} from "./components/room/room-create-edit/room-create-edit.component";
import { BookingCreateComponent } from "./components/booking/booking-create/booking-create.component";
import { AuthGuard } from './guards/auth.guard';
import { BookingDetailComponent } from './components/booking/booking-detail/booking-detail.component';
import {BookingListComponent} from "./components/booking/booking-list/booking-list.component";
import {EmployeeListComponent} from "./components/employee/employee-list/employee-list.component";
import {SignUpComponent} from "./components/sign-up/sign-up.component";
import {GuestListComponent} from "./components/guest/guest-list/guest-list.component";
import {
  GuestCreateEditComponent,
  GuestCreateEditMode
} from "./components/guest/guest-list/guest-create-edit/guest-create-edit.component";
import {
  EmployeeCreateEditComponent,
  EmployeeCreateEditMode
} from "./components/employee/employee-create-edit/employee-create-edit.component";
import {
  ActivityTimeslotListComponent
} from "./components/activity/activity-timeslot-list/activity-timeslot-list.component";

const routes: Routes = [
  {path: '', component: HomeComponent},
  {path: 'login', component: LoginComponent},
  {path: 'signup', component: SignUpComponent},
  {
    path: 'rooms',
    children: [
      {path: '', component: RoomListComponent },
      {path: 'detail/:id', component: RoomDetailComponent},
      {path: 'create', component: RoomCreateEditComponent, data: {mode: RoomCreateEditMode.create, adminCheck: true}, canActivate: [AuthGuard]},
      {path: 'edit/:id', component: RoomCreateEditComponent, data: {mode: RoomCreateEditMode.edit, adminCheck: true}, canActivate: [AuthGuard]},
    ],
  },
  {
    path: 'activities',
        children: [
          {path: '', component: ActivityListComponent },
          {path: 'timeslots/:id', component: ActivityTimeslotListComponent},
          {path: 'detail/:id', component: ActivityDetailComponent},
          {path: 'create', component: ActivityCreateEditComponent, data: {mode: ActivityCreateEditMode.create, adminCheck: true}, canActivate: [AuthGuard]},
          {path: 'edit/:id', component: ActivityCreateEditComponent, data: {mode: ActivityCreateEditMode.edit, adminCheck: true}, canActivate: [AuthGuard]},
        ],
  },
  {
    path: 'bookings',
    children: [
      {path: 'create/:id', component: BookingCreateComponent, canActivate: [AuthGuard],},
      {path: 'my-bookings', children: [
          {path: '', component: BookingDetailComponent, canActivate: [AuthGuard]},
          {path: 'success/:bookingId', component: BookingDetailComponent, canActivate: [AuthGuard]},
          {path: 'cancel/:bookingId', component: BookingDetailComponent, canActivate: [AuthGuard]},
          {path: 'activity/success/:bookingId', component: BookingDetailComponent, canActivate: [AuthGuard]},
          {path: 'activity/cancel/:bookingId', component: BookingDetailComponent, canActivate: [AuthGuard]},

        ]
      },
      {path: 'managerbookings', component: BookingListComponent, canActivate: [AuthGuard]},
      {path: '', redirectTo: '/login', pathMatch: 'full'},
    ],
  },
  {path: 'ui-config', component: UiConfigComponent, canActivate: [AuthGuard], data: {adminCheck: true}},
  {path: 'room-cleaning', component: RoomCleaningListComponent, canActivate: [AuthGuard], data: {cleanerCheck: true}},
  {path: 'check-in/:id', component: CheckInComponent, canActivate: [AuthGuard]},
  {
    path: 'manual-check-in/:email/:id',
    component: CheckInComponent,
    canActivate: [AuthGuard],
    data: {adminCheck: true}
  },
  {path: 'add-to-room/:email/:ownerEmail/:isManual/:id', component: CheckInComponent, canActivate: [AuthGuard]},
  {path: 'my-room', component: MyRoomComponent, canActivate: [AuthGuard]},
  {path: 'employees', children: [
      {path: '', component: EmployeeListComponent, canActivate: [AuthGuard]},
      {path: 'create', component: EmployeeCreateEditComponent, data: {mode: EmployeeCreateEditMode.create}, canActivate: [AuthGuard],},
      {path: 'edit/:id', component: EmployeeCreateEditComponent, data: {mode: EmployeeCreateEditMode.edit}, canActivate: [AuthGuard],}
    ],
  },
  {path: 'guests', children: [
      {path: '', component: GuestListComponent, canActivate: [AuthGuard]},
      {path: 'create', component: GuestCreateEditComponent, data: {mode: GuestCreateEditMode.create}, canActivate: [AuthGuard],},
      {path: 'edit/:email', component: GuestCreateEditComponent, data: {mode: GuestCreateEditMode.edit}, canActivate: [AuthGuard],},
    ],
  },
];
@NgModule({
  imports: [RouterModule.forRoot(routes, {useHash: true})],
  exports: [RouterModule]
})
export class AppRoutingModule { }
