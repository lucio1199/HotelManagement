import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {KeyStatusDto} from "../dtos/key";
import {Globals} from '../global/globals';


@Injectable({
  providedIn: 'root'
})
export class KeyService {

  private apiUrl: string = this.globals.backendUri + '/key';

  constructor(private http: HttpClient, private globals: Globals) { }

  getStatus(id: number): Observable<KeyStatusDto> {
    return this.http.get<KeyStatusDto>(`${this.apiUrl}/` + id, {});
  }

  unlock(id: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/unlock/` + id, {});
  }
}
