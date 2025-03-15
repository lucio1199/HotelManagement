export class Activity {
  id: number | null;
  name: string;
  description: string;
  capacity: number;
  mainImage: File;
  price: number;
  additionalImages: File[];
  timeslotsInfos: ActivityTimeslotInfo[];
  categories: string;
}

export interface ActivityListDto {
  id: number | null;
  name: string;
  description: string;
  capacity: number;
  mainImage: string;
  price: number;
  timeslots: ActivityTimeslotInfo[];
}

export class ActivityDetailDto {
  id:number;
  name: string;
  description: string;
  capacity: number;
  mainImage: string;
  price: number;
  additionalImages: string[];
  categories: string;
  timeslots: ActivityTimeslotInfo[];
}

export interface ActivityTimeslotInfo {
  id: number;
  dayOfWeek?: DayOfWeek;
  specificDate?: Date;
  startTime: Date;
  endTime: Date;
}

export interface ActivitySlotDto {
  id: number;
  date: Date;
  startTime: Date;
  endTime: Date;
  capacity: number;
  occupied: number;
}

export enum DayOfWeek {
  monday = 'Monday',
  tuesday = 'Tuesday',
  wednesday = 'Wednesday',
  thursday = 'Thursday',
  friday = 'Friday',
  saturday = 'Saturday',
  sunday = 'Sunday'
}

export enum Category {
  Education = 'Education',
  Music = 'Music',
  Fitness = 'Fitness',
  Nature = 'Nature',
  Cooking = 'Cooking',
  Teamwork = 'Teamwork',
  Creativity = 'Creativity',
  Wellness = 'Wellness',
  Recreation = 'Recreation',
  Sports = 'Sports',
  Kids = 'Kids',
  Workshop = 'Workshop'
}

export class ActivitySearchDto {
  name: string | null;
  date: Date | null;
  persons: number | null;
  minPrice: number | null;
  maxPrice: number | null;
}

export class ActivitySlotSearchDto {
  date?: Date | null;
  participants?: number | null;
}

export class ActivityBookingCreateDto {
  activityId: number;
  activitySlotId: number;
  bookingDate: Date;
  participants: number;
}

export class ActivityBookingDto {
  id: number;
  activityId: number;
  activityName: string;
  bookingDate: Date;
  startTime: Date;
  endTime: Date;
  date: Date;
  participants: number;
  paid: boolean;
}
