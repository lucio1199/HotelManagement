import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Globals } from '../global/globals';

@Injectable({
  providedIn: 'root'
})
export class DocumentService {

  private apiUrl: string = this.globals.backendUri + '/documents';

  constructor(private http: HttpClient, private globals: Globals) { }

  getPassportByBookingIdAndEmail(bookingId: number, email: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/passport/${bookingId}/${encodeURIComponent(email)}`, {
      responseType: 'blob',
      observe: 'response'
    }).pipe(
      map((response: HttpResponse<Blob>) => {
        if (response.body) {
          return response.body;
        } else {
          throw new Error('No passport data returned');
        }
      })
    );
  }
}
