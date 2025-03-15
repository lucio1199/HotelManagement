
export class UiConfig {
  id: number | null;
  hotelName: string;
  descriptionShort: string;
  description: string;
  address: string;
  roomCleaning: boolean;
  digitalCheckIn: boolean;
  activities: boolean;
  communication: boolean;
  nuki: boolean;
  halfBoard: boolean;
  priceHalfBoard: number;
  images: File[];
}

export interface UiConfigDetailDto {
  id: number;
  hotelName: string;
  descriptionShort: string;
  description: string;
  address: string;
  roomCleaning: boolean;
  digitalCheckIn: boolean;
  activities: boolean;
  communication: boolean;
  nuki: boolean;
  halfBoard: boolean;
  priceHalfBoard: number;
  images: string[];
}

export interface UiConfigHomepageDto {
  hotelName: string;
  descriptionShort: string;
  description: string;
  address: string;
  images: string[];
}

export interface activatedModulesDto {
  roomCleaning: boolean;
  digitalCheckIn: boolean;
  activities: boolean;
  communication: boolean;
  nuki: boolean;
  halfBoard: boolean;
  priceHalfBoard: number;
}
