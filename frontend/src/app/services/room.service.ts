import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Globals} from '../global/globals';
import {Room, RoomListDto, RoomDetailDto, RoomSearchDto, RoomAdminSearchDto, RoomListCleanDto, OccupancyDto} from "../dtos/room";
import { formatDate } from '@angular/common';

@Injectable({
  providedIn: 'root'
})
export class RoomService {

  private roomBaseUri: string = this.globals.backendUri + '/room';

  constructor(private httpClient: HttpClient, private globals: Globals) {
  }

  /**
   * Persists rooms to the backend
   *
   * @param room to persist
   */
  createRoom(room: Room): Observable<Room> {
    const formData = new FormData();
    if (room.name) {
      formData.append('name', room.name);
    }
    if (room.description) {
      formData.append('description', room.description);
    }
    if (room.capacity) {
      formData.append('capacity', room.capacity.toString());
    }
    if (room.smartLockId) {
      formData.append('smartLockId', room.smartLockId.toString());
    }
    if (room.price) {
      formData.append('price', room.price.toString());
    }
    if (room.mainImage) {
      formData.append('mainImage', room.mainImage, room.mainImage.name);
    }
    if (room.additionalImages) {
      room.additionalImages.forEach(image => {
        formData.append('additionalImages', image, image.name);
      });
    }


    formData.forEach((value, key) => {
      console.log(`${key}: ${value}`);
    });
    return this.httpClient.post<Room>(this.roomBaseUri, formData);
  }

  /**
   * Loads all rooms from the backend
   */
  getAll(page: number, size: number): Observable<RoomListDto[]> {
    console.log('Load all rooms');
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.httpClient.get<RoomListDto[]>(`${this.roomBaseUri}/all`, {params});
  }

  /**
   * Loads all rooms from the backend
   */
  getAllForClean(page: number, size: number): Observable<{totalElements: number, content: RoomListCleanDto[]}> {
    console.log('Load all rooms');
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.httpClient.get<any>(`${this.roomBaseUri}/clean`, {params});
  }

  /**
   * Delete a room by its id
   */
  delete(id: number): Observable<void> {
    return this.httpClient.delete<void>(`${this.roomBaseUri}/rooms/${id}`);
  }

  /**
   * Deletes the cleaning time for a room
   */
  deleteRoomCleaningTime(id: number): Observable<RoomListDto> {
    console.log(`Deleting cleaning time for room with ID: ${id}`);

    return this.httpClient.delete<RoomListDto>(`${this.roomBaseUri}/${id}`);
  }

  /**
   * Find a room by its id
   */
  findOne(id: number): Observable<RoomDetailDto> {
    return this.httpClient.get<RoomDetailDto>(`${this.roomBaseUri}/${id}`);
  }

  /**
   * Update a room
   */
  updateRoom(id: number, room: Room): Observable<Room> {
    const formData = new FormData();
    if (room.name) {
      formData.append('name', room.name);
    }
    if (room.description) {
      formData.append('description', room.description);
    }
    if (room.capacity) {
      formData.append('capacity', room.capacity.toString());
    }
    if (room.smartLockId) {
      formData.append('smartLockId', room.smartLockId.toString());
    }
    if (room.price) {
      formData.append('price', room.price.toString());
    }
    if (room.mainImage) {
      formData.append('mainImage', room.mainImage, room.mainImage.name);
    }
    if (room.additionalImages) {
      room.additionalImages.forEach(image => {
        formData.append('additionalImages', image, image.name);
      });
    }

    return this.httpClient.put<Room>(`${this.roomBaseUri}/${id}`, formData);
  }

  /**
   * Updates the last cleaned time of a room to now
   */
  updateRoomLastCleanedAt(id: number): Observable<RoomListDto> {
    console.log(`Updating last cleaned time for room with ID: ${id}`);
    return this.httpClient.put<RoomListDto>(`${this.roomBaseUri}/${id}/clean`, null);
  }

  /**
   * Updates the preferred cleaning time for a room
   */
  updateRoomCleaningTime(id: number, cleaningTimeFrom: string, cleaningTimeTo: string): Observable<RoomListDto> {
    console.log(`Updating preferred cleaning time for room with ID: ${id}`);

    // Create payload as JSON object
    const payload = {
      cleaningTimeFrom: cleaningTimeFrom,
      cleaningTimeTo: cleaningTimeTo,
    };

    // Send the payload to the backend
    return this.httpClient.put<RoomListDto>(`${this.roomBaseUri}/${id}/clean-time`, payload);
  }



  /**
   * Search for a room, with search parameters
   */
  searchRooms(searchParams: RoomSearchDto, page: number, size: number): Observable<{totalElements: number, content: RoomListDto[]}> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (searchParams.startDate) {
      params = params.append('startDate', formatDate(searchParams.startDate, 'yyyy-MM-dd', 'en-US'));
    }
    if (searchParams.endDate) {
      params = params.append('endDate', formatDate(searchParams.endDate, 'yyyy-MM-dd', 'en-US'));
    }
    if (searchParams.persons) {
      params = params.append('capacity', searchParams.persons.toString());
    }
    if (searchParams.minPrice) {
      params = params.append('minPrice', searchParams.minPrice.toString());
    }
    if (searchParams.maxPrice) {
      params = params.append('maxPrice', searchParams.maxPrice.toString());
    }
    return this.httpClient.get<any>(`${this.roomBaseUri}`, {params});
  }

  /**
   * Search for rooms as a manager
   */
  managerSearch(searchParams: RoomAdminSearchDto, pageIndex: number, pageSize: number): Observable<{ totalElements: number, content: RoomListDto[] }>{
    let params = new HttpParams()
      .set('page', pageIndex.toString())
      .set('size', pageSize.toString());
    if (searchParams.name) {
      params = params.append('name', searchParams.name);
    }
    if (searchParams.description) {
      params = params.append('description', searchParams.description);
    }
    if (searchParams.minCapacity) {
      params = params.append('minCapacity', searchParams.minCapacity.toString());
    }
    if (searchParams.maxCapacity) {
      params = params.append('maxCapacity', searchParams.maxCapacity.toString());
    }
    if (searchParams.minPrice) {
      params = params.append('minPrice', searchParams.minPrice.toString());
    }
    if (searchParams.maxPrice) {
      params = params.append('maxPrice', searchParams.maxPrice.toString());
    }
    return this.httpClient.get<any>(`${this.roomBaseUri}/admin`, {params});
  }

  getOccupancyStatus(roomId: number): Observable<any> {
    return this.httpClient.get<OccupancyDto>(`${this.globals.backendUri}/manual-checkin/occupancy/${roomId}`);
  }
}
