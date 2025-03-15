import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import { Observable } from 'rxjs';
import {UiConfig, UiConfigDetailDto} from "../dtos/ui-config";
import {Globals} from "../global/globals";


@Injectable({
  providedIn: 'root'
})
export class UiConfigService {
  private uiConfigBaseUri: string = this.globals.backendUri + '/ui-config';

  constructor(private http: HttpClient, private globals: Globals) {}

  getUiConfig(): Observable<UiConfigDetailDto> {
    return new Observable<UiConfigDetailDto>((subscriber) => {
      this.http.get<UiConfigDetailDto>(`${this.uiConfigBaseUri}/1`).subscribe({
        next: (dto) => {
          console.log(this.uiConfigBaseUri);
          subscriber.next(dto);
          subscriber.complete();
        },
        error: (error) => subscriber.error(error)
      });
    });
  }

  updateUiConfig(config: UiConfig): Observable<UiConfig> {
    try {
      const formData = new FormData();

      // Append fields
      formData.append('id', config.id!.toString());
      formData.append('hotelName', config.hotelName);
      formData.append('descriptionShort', config.descriptionShort);
      formData.append('description', config.description);
      formData.append('address', config.address);
      formData.append('roomCleaning', config.roomCleaning.toString());
      formData.append('digitalCheckIn', config.digitalCheckIn.toString());
      formData.append('activities', config.activities.toString());
      formData.append('communication', config.communication.toString());
      formData.append('nuki', config.nuki.toString());
      formData.append('halfBoard', config.halfBoard.toString());
      if (config.priceHalfBoard) {
        formData.append('priceHalfBoard', config.priceHalfBoard.toString());
      }
      if (config.images) {
        config.images.forEach(image => {
          formData.append('images', image, image.name);
        });
      }
      formData.forEach((value, key) => {
        console.log(key, value);
      });
      return new Observable<UiConfig>((subscriber) => {
        this.http.put<UiConfig>(`${this.uiConfigBaseUri}/${config.id}`, formData).subscribe({
          next: (updatedConfig) => {
            console.log(this.uiConfigBaseUri);
            subscriber.next(updatedConfig);
            subscriber.complete();
          },
          error: (error) => subscriber.error(error)
        });
      });
    } catch (error) {
      console.error('Error creating formData:', error);
      throw error;
    }
  }

  getUiConfigHomepage(): Observable<UiConfigDetailDto> {
    return this.http.get<UiConfigDetailDto>(`${this.uiConfigBaseUri}/homepage`);
  }



  digitalCheckInIsEnabled(): Observable<boolean> {
    return this.http.get<boolean>(`${this.uiConfigBaseUri}/module-enabled/digitalCheckIn`);
  }

  roomCleaningIsEnabled(): Observable<boolean> {
    return this.http.get<boolean>(`${this.uiConfigBaseUri}/module-enabled/roomCleaning`);
  }

  activitiesIsEnabled(): Observable<boolean> {
    return this.http.get<boolean>(`${this.uiConfigBaseUri}/module-enabled/activities`);
  }

  nukiIsEnabled(): Observable<boolean> {
    return this.http.get<boolean>(`${this.uiConfigBaseUri}/module-enabled/nuki`);
  }



}
