import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {BookingCreateDto, BookingDetailDto, DetailedBookingDto} from '../dtos/booking';
import { Globals } from '../global/globals';
import { Page } from '../models/page.model';

@Injectable({
  providedIn: 'root'
})
export class BookingService {
  private bookingBaseUri: string = `${this.globals.backendUri}/bookings`;

  constructor(private httpClient: HttpClient, private globals: Globals) {}

  /**
   * Sends a booking request to the backend.
   *
   * @param booking The booking information
   */
  createBooking(booking: BookingCreateDto): Observable<BookingDetailDto> {
    return this.httpClient.post<BookingDetailDto>(this.bookingBaseUri, booking);
  }

  getBookingsByUser(): Observable<BookingDetailDto[]> {
    return this.httpClient.get<BookingDetailDto[]>(`${this.bookingBaseUri}/my-bookings`);
  }

  getBookingsByGuests(): Observable<DetailedBookingDto[]> {
    return this.httpClient.get<DetailedBookingDto[]>(`${this.bookingBaseUri}/managerbookings`);
  }

  cancelBooking(bookingId: number): Observable<void> {
    return this.httpClient.delete<void>(`${this.bookingBaseUri}/my-bookings/${bookingId}/cancel`);
  }

  markAsPaid(bookingId: number): Observable<boolean> {
    return this.httpClient.put<boolean>(`${this.bookingBaseUri}/${bookingId}`, {});
  }

  markAsPaidManually(bookingId: number): Observable<void> {
    return this.httpClient.put<void>(`${this.bookingBaseUri}/${bookingId}/mark-as-paid`, {});
  }

  getPagedBookings(page: number, size: number): Observable<Page<DetailedBookingDto>> {
    return this.httpClient.get<Page<DetailedBookingDto>>(`${this.bookingBaseUri}/managerbookings/paged?page=${page}&size=${size}`);
  }

  downloadPdf(bookingId: number, type: string): Observable<Blob> {
    const url = `${this.bookingBaseUri}/my-bookings/${bookingId}/pdf/${type}`;
    return this.httpClient.get(url, { responseType: 'blob' });
  }
}
