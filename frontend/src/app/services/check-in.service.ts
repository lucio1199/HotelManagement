import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {myRoomDto, RoomDetailDto} from "../dtos/room";
import {BookingDetailDto} from "../dtos/booking";
import {CheckInDto, CheckOutDto, CheckInStatusDto, ManuallyAddToRoomDto} from "../dtos/check-in";
import {Globals} from '../global/globals';
import {GuestListDto} from "../dtos/guest";
import {InviteToRoomDto} from "../dtos/invite";


@Injectable({
  providedIn: 'root'
})
export class CheckInService {

  private apiUrl: string = this.globals.backendUri + '/checkin';
  private manualApiUrl: string = this.globals.backendUri + '/manual-checkin';
  private checkedIn: boolean = false;

  constructor(private http: HttpClient, private globals: Globals) { }

  getGuestRooms(): Observable<myRoomDto[]> {
    return this.http.get<myRoomDto[]>(`${this.apiUrl}/rooms`);
  }

  getBookingById(id: number): Observable<BookingDetailDto> {
    return this.http.get<BookingDetailDto>(`${this.apiUrl}/${id}`);
  }

  getBookingByIdAndEmail(id: number, email: string): Observable<BookingDetailDto> {
    return this.http.get<BookingDetailDto>(`${this.manualApiUrl}/${id}/${email}`);
  }

  appendFormData(checkIn: CheckInDto | ManuallyAddToRoomDto): FormData {
    const formData = new FormData();

    if (checkIn && checkIn.bookingId) {
      formData.append('bookingId', checkIn.bookingId);
    }

    if (checkIn && checkIn.firstName) {
      formData.append('firstName', checkIn.firstName);
    }

    if (checkIn && checkIn.lastName) {
      formData.append('lastName', checkIn.lastName);
    }

    if (checkIn && checkIn.dateOfBirth) {
      formData.append('dateOfBirth', checkIn.dateOfBirth);
    }

    if (checkIn && checkIn.placeOfBirth) {
      formData.append('placeOfBirth', checkIn.placeOfBirth);
    }

    if (checkIn && checkIn.gender) {
      formData.append('gender', checkIn.gender);
    }

    if (checkIn && checkIn.nationality) {
      formData.append('nationality', checkIn.nationality);
    }

    if (checkIn && checkIn.address) {
      formData.append('address', checkIn.address);
    }

    if (checkIn && checkIn.passportNumber) {
      formData.append('passportNumber', checkIn.passportNumber);
    }

    if (checkIn && checkIn.phoneNumber) {
      formData.append('phoneNumber', checkIn.phoneNumber);
    }

    if (checkIn && checkIn.passport) {
      formData.append('passport', checkIn.passport);
    }
    return formData;
  }

  checkIn(checkIn: CheckInDto): Observable<void> {
    const formData = this.appendFormData(checkIn);

    // Logging all the appended formData key-value pairs for verification
    formData.forEach((value, key) => {
      console.log(`${key}: ${value}`);
    });

    return this.http.post<void>(this.apiUrl, formData);
  }

  manualCheckIn(checkIn: CheckInDto, email: string): Observable<void> {
    const formData = this.appendFormData(checkIn);

    // Logging all the appended formData key-value pairs for verification
    formData.forEach((value, key) => {
      console.log(`${key}: ${value}`);
    });

    // Construct the URL correctly with email as a path parameter
    return this.http.post<void>(`${this.manualApiUrl}/${email}`, formData);
  }

  getCheckedInStatus(email: string): Observable<CheckInStatusDto[]> {
    return this.http.get<CheckInStatusDto[]>(`${this.manualApiUrl}/checkin-status/${email}`);
  }

  getDigitalCheckedInStatus(): Observable<CheckInStatusDto[]> {
    return this.http.get<CheckInStatusDto[]>(`${this.apiUrl}/checkin-status`);
  }

  checkOut(checkOut: CheckOutDto): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/checkout`, checkOut);
  }

  manualCheckOut(checkOut: CheckOutDto): Observable<void> {
    console.log('checkOutDto: ' + checkOut);
    return this.http.post<void>(`${this.manualApiUrl}/checkout`, checkOut);
  }

  getGuestBooking(roomId: number): Observable<BookingDetailDto> {
    return this.http.get<BookingDetailDto>(`${this.apiUrl}/booking/${roomId}`);
  }

  isCheckedIn(): boolean {
    return this.checkedIn;
  }

  resetCheckedIn(): void {
    this.getGuestRooms().subscribe({
      next: () => {
        this.checkedIn = true;
      },
      error: () => {
        this.checkedIn = false;
      }
    });
  }

  getBookingIds(): Observable<number[]> {
    console.log("load all booking Ids")
    return this.http.get<number[]>(`${this.apiUrl}/booking-ids`);
  }

  inviteToRoom(invite: InviteToRoomDto): Observable<void> {
    console.log("invite guest to room");
    return this.http.post<void>(this.apiUrl + '/to-room', invite);
  }

  manuallyAddGuestToRoom(addGuestDto: ManuallyAddToRoomDto): Observable<void> {
    console.log("manually add guest to room");

    const formData = this.appendFormData(addGuestDto);
    formData.append('email', addGuestDto.email);

    // Logging all the appended formData key-value pairs for verification
    formData.forEach((value, key) => {
      console.log(`${key}: ${value}`);
    });

    return this.http.post<void>(this.manualApiUrl + '/to-room', formData);
  }

  getGuests(id: number): Observable<GuestListDto[]> {
     return this.http.get<GuestListDto[]>(`${this.apiUrl}/guests/` + id);
  }

  getAllGuests(id: number): Observable<GuestListDto[]> {
     return this.http.get<GuestListDto[]>(`${this.manualApiUrl}/all-guests/` + id);
  }

  isOwner(id: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/owner/` + id);
  }

  remove(id: number, email: string): Observable<void> {
    return this.http.delete<void>(this.manualApiUrl + '/' + id + '/' + email);
  }
}
