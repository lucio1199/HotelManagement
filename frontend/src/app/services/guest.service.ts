import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {Globals} from "../global/globals";
import {Observable} from "rxjs";
import {
  SimpleGuestDto,
  GuestSignupDto,
  GuestListDto,
  GuestCreateDto,
  GuestDetailDto,
  GuestUpdateDto, GuestSearchDto
} from "../dtos/guest";
import {AuthService} from "./auth.service";
import {RoomListCleanDto} from "../dtos/room";

@Injectable({
  providedIn: 'root'
})
export class GuestService {
  private guestBaseUri: string = this.globals.backendUri + '/guest';

  constructor(private httpClient: HttpClient, private globals: Globals, private authService: AuthService) { }

  /**
   * Registers a new guest user in the system.
   *
   * @param guestSignupDto the guest signup data
   * @returns observable of the created guest
   */
  signup(guestSignupDto: GuestSignupDto): Observable<SimpleGuestDto> {
    return this.httpClient.post<SimpleGuestDto>(`${this.guestBaseUri}/signup`, guestSignupDto);
  }

  /**
   * Fetches the guest data of the currently logged-in user.
   *
   * @returns observable of the guest data
   */
  getOwnData(): Observable<GuestDetailDto> {
    return this.httpClient.get<GuestDetailDto>(`${this.guestBaseUri}/${this.authService.getUserEmail()}`);
  }


  getAllGuests(page: number, size: number): Observable<{totalElements: number, content: GuestListDto[]}> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.httpClient.get<any>(`${this.guestBaseUri}`, {params});
  }

  /**
   * Deletes the guest from the system.
   *
   * @param guest the guest to delete
   */
  deleteGuest(guest: GuestListDto): Observable<void> {
    return this.httpClient.delete<void>(`${this.guestBaseUri}/${guest.email}`);
  }

  /**
   * Creates the guest in the system.
   *
   * @param guest the guest to create
   */
  createGuest(guest: GuestCreateDto): Observable<GuestDetailDto> {
    return this.httpClient.post<GuestDetailDto>(this.guestBaseUri, guest);
  }

  /**
   * Updates the guest in the system.
   *
   * @param email the email of the guest to update
   * @param guest the guest data to update
   */
  updateGuest(email: string, guest: GuestUpdateDto): Observable<GuestDetailDto> {
    return this.httpClient.put<GuestDetailDto>(`${this.guestBaseUri}/${email}`, guest);
  }

  /**
   * Fetches the guest data of the given email.
   *
   * @param email the email of the guest
   */
  getGuest(email: string): Observable<GuestDetailDto> {
    return this.httpClient.get<GuestDetailDto>(`${this.guestBaseUri}/${email}`);
  }

  /**
   * Searches for guests with the given search parameters.
   *
   * @param searchParams the search parameters
   */

  searchGuests(searchParams: GuestSearchDto, page: number, size: number): Observable<{ totalElements: number, content: GuestListDto[] }> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (searchParams.firstName) {
      params = params.append('firstName', searchParams.firstName);
    }
    if (searchParams.lastName) {
      params = params.append('lastName', searchParams.lastName);
    }
    if (searchParams.email) {
      params = params.append('email', searchParams.email);
    }

    return this.httpClient.get<any>(`${this.guestBaseUri}/search`, {params});
  }
}
