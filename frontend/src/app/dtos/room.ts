export class Room {
  id: number | null;
  name: string;
  description: string;
  capacity: number;
  mainImage: File;
  price: number;
  additionalImages: File[];
  smartLockId: number;
}

export interface RoomListDto {
  id: number;
  name: string;
  description: string;
  capacity: number;
  mainImage: string;
  price: number;
}

export interface RoomListCleanDto {
  id: number;
  name: string;
  description: string;
  capacity: number;
  mainImage: string;
  price: number;
  lastCleanedAt: Date;
  cleaningTimeFrom: Date;
  cleaningTimeTo: Date;
}


export class RoomDetailDto {
  id:number;
  name: string;
  description: string;
  capacity: number;
  mainImage: string;
  price: number;
  additionalImages: string[];
  smartLockId: number;
}

export class RoomSearchDto {
  startDate: Date | null;
  endDate: Date | null;
  persons: number | null;
  minPrice: number | null;
  maxPrice: number | null;
}

export class RoomAdminSearchDto {
  name: string | null;
  description: string | null;
  minCapacity: number | null;
  maxCapacity: number | null;
  minPrice: number | null;
  maxPrice: number | null;
}

export class myRoomDto {
  id: number;
  name: string;
  description: string;
  capacity: number;
  mainImage: string;
  price: number;
  additionalImages: string[];
  cleaningFrom: string | null = null;
  cleaningTo: string | null = null;
  keyStatus: string | null = null;
}

export class OccupancyDto {
  roomId: number;
  status: string;
}
