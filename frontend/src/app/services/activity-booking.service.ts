import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Globals} from "../global/globals";
import {ActivityBookingCreateDto, ActivityBookingDto} from "../dtos/activity";
import {Observable} from "rxjs";
import {DetailedBookingDto} from "../dtos/booking";

@Injectable({
  providedIn: 'root'
})
export class ActivityBookingService {

  private baseUri: string = this.globals.backendUri + '/activity-booking';

  constructor(private httpClient: HttpClient, private globals: Globals) {}

  bookActivitySlot(bookingData: ActivityBookingCreateDto): Observable<ActivityBookingDto> {
    return this.httpClient.post<ActivityBookingDto>(`${this.baseUri}`, bookingData);
  }

  markAsPaid(bookingId: number): Observable<void> {
    return this.httpClient.put<void>(`${this.baseUri}/${bookingId}`, {});
  }

  getBookingByUser(): Observable<ActivityBookingDto[]> {
    return this.httpClient.get<ActivityBookingDto[]>(`${this.baseUri}/my-bookings`);

  }
}
