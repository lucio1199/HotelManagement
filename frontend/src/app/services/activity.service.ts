import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import {map, Observable} from 'rxjs';
import {
  Activity,
  ActivityListDto,
  ActivityDetailDto,
  ActivitySearchDto,
  ActivitySlotDto,
  ActivitySlotSearchDto,
  ActivityBookingCreateDto
} from "../dtos/activity";
import {Globals} from '../global/globals';
import { formatDate } from '@angular/common';

@Injectable({
  providedIn: 'root'
})
export class ActivityService {

  private activityBaseUri: string = this.globals.backendUri + '/activity';

  constructor(private httpClient: HttpClient, private globals: Globals) {
  }

  /**
   * Persists activities to the backend
   *
   * @param activity to persist
   */
  createActivity(activity: Activity): Observable<Activity> {
    console.log("Creating activity:", activity);

    const formData = new FormData();

    // Append the basic fields
    if (activity.name) {
      formData.append('name', activity.name);
    }
    if (activity.description) {
      formData.append('description', activity.description);
    }
    if (activity.capacity) {
      formData.append('capacity', activity.capacity.toString());
    }
    if (activity.price) {
      formData.append('price', activity.price.toString());
    }

    // Append the main image (if present)
    if (activity.mainImage) {
      formData.append('mainImage', activity.mainImage, activity.mainImage.name);
    }

    // Append additional images (if any)
    if (activity.additionalImages && activity.additionalImages.length > 0) {
      activity.additionalImages.forEach((image, index) => {
        formData.append('additionalImages', image, image.name);
      });
    }

    // Serialize and append timeslots as a JSON string
    if (activity.timeslotsInfos && activity.timeslotsInfos.length > 0) {
      const timeslotsJson = JSON.stringify(activity.timeslotsInfos);
      formData.append('timeslots', timeslotsJson);
    }

    if (activity.categories) {
      formData.append('categories', activity.categories);
    }

    // Log the constructed FormData for debugging
    console.log("FormData contents:");
    formData.forEach((value, key) => console.log(`${key}:`, value));

    // Make the POST request
    return this.httpClient.post<Activity>(this.activityBaseUri, formData);
  }

  /**
   * Loads all activities from the backend
   */
  getAllPaginated(pageIndex: number, pageSize: number): Observable<{ totalElements: number, content: ActivityListDto[] }> {
    const params = new HttpParams()
      .set('pageIndex', pageIndex.toString())
      .set('pageSize', pageSize.toString());

    return this.httpClient.get<{ totalElements: number, content: ActivityListDto[] }>(
      `${this.activityBaseUri}/all`,
      {params}
    );
  }

  /**
   * Loads all activities from the backend
   */
  getRecommended(): Observable<ActivityListDto> {
    console.log('Load recommended activitiy');
    return this.httpClient.get<ActivityListDto>(`${this.activityBaseUri}/recommended`);
  }

  /**
   * Delete an activity by its id
   */
  delete(id: number): Observable<void> {
    return this.httpClient.delete<void>(`${this.activityBaseUri}/activities/${id}`);
  }

  /**
   * Find an activity by its id
   */
  findOne(id: number): Observable<ActivityDetailDto> {
    return this.httpClient.get<ActivityDetailDto>(`${this.activityBaseUri}/${id}`);
  }

  /**
   * Update an activity
   */
  updateActivity(id: number, activity: Activity): Observable<Activity> {
    const formData = new FormData();
    if (activity.name) {
      formData.append('name', activity.name);
    }
    if (activity.description) {
      formData.append('description', activity.description);
    }
    if (activity.capacity) {
      formData.append('capacity', activity.capacity.toString());
    }
    if (activity.price) {
      formData.append('price', activity.price.toString());
    }
    if (activity.mainImage) {
      formData.append('mainImage', activity.mainImage, activity.mainImage.name);
    }
    if (activity.additionalImages) {
      activity.additionalImages.forEach(image => {
        formData.append('additionalImages', image, image.name);
      });
    }

        // Serialize and append timeslots as a JSON string
        if (activity.timeslotsInfos && activity.timeslotsInfos.length > 0) {
          const timeslotsJson = JSON.stringify(activity.timeslotsInfos);
          formData.append('timeslots', timeslotsJson);
        }

        // Log the constructed FormData for debugging
        console.log("FormData contents:");
        formData.forEach((value, key) => console.log(`${key}:`, value));


    return this.httpClient.put<Activity>(`${this.activityBaseUri}/${id}`, formData);
  }

  /**
   * Search for activities with pagination
   */
  searchActivitiesPaginated(
    searchParams: ActivitySearchDto,
    pageIndex: number,
    pageSize: number
  ): Observable<{ totalElements: number, content: ActivityListDto[] }> {
    let params = new HttpParams()
      .set('pageIndex', pageIndex.toString())
      .set('pageSize', pageSize.toString());

    if (searchParams.name) {
      params = params.set('name', searchParams.name);
    }
    if (searchParams.date) {
      params = params.set('date', formatDate(searchParams.date, 'yyyy-MM-dd', 'en-US'));
    }
    if (searchParams.persons) {
      params = params.set('capacity', searchParams.persons.toString());
    }
    if (searchParams.minPrice) {
      params = params.set('minPrice', searchParams.minPrice.toString());
    }
    if (searchParams.maxPrice) {
      params = params.set('maxPrice', searchParams.maxPrice.toString());
    }

    return this.httpClient.get<{ totalElements: number, content: ActivityListDto[] }>(
      `${this.activityBaseUri}/search`,
      {params}
    );
  }

  getTimeSlots(activityId: number, pageIndex: number, pageSize: number): Observable<{ totalElements: number, content: ActivitySlotDto[] }> {
    const params = new HttpParams()
      .set('pageIndex', pageIndex.toString())
      .set('pageSize', pageSize.toString());
    const response =  this.httpClient.get<any>(`${this.activityBaseUri}/timeslots/${activityId}`, { params }
    );
    console.log('Loaded timeslots:', response);
    return response
  }

  searchTimeSlots(
    activityId: number,
    searchDto: ActivitySlotSearchDto,
    pageIndex: number,
    pageSize: number
  ): Observable<{ totalElements: number; content: ActivitySlotDto[] }> {
    let params = new HttpParams()
      .set('pageIndex', pageIndex.toString())
      .set('pageSize', pageSize.toString());

    if (searchDto.date) {
      params = params.set('date', formatDate(searchDto.date, 'yyyy-MM-dd', 'en-US'));
    }

    if (searchDto.participants) {
      params = params.set('participants', searchDto.participants.toString());
    }

    return this.httpClient.get<{ totalElements: number; content: ActivitySlotDto[] }>(
      `${this.activityBaseUri}/timeslots/search/${activityId}`,
      { params }
    );
  }

  bookActivitySlot(bookingData: ActivityBookingCreateDto): Observable<void> {
    return this.httpClient.post<void>(`${this.activityBaseUri}`, bookingData);
  }
}
