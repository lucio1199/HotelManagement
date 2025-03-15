export interface GuestSignupDto {
  email: string;
  password: string;
}

export interface SimpleGuestDto {
  email: string;
}

export interface GuestListDto {
  firstName?: string;
  lastName?: string;
  email: string;
}

export interface GuestCreateDto {
  firstName: string;
  lastName: string;
  email: string;
  dateOfBirth?: string;
  placeOfBirth?: string;
  gender?: string;
  nationality?: string;
  address?: string;
  passportNumber?: string;
  phoneNumber?: string;
  password: string;
}

export interface GuestUpdateDto {
  firstName?: string;
  lastName?: string;
  email?: string;
  dateOfBirth?: string;
  placeOfBirth?: string;
  gender?: string;
  nationality?: string;
  address?: string;
  passportNumber?: string;
  phoneNumber?: string;
  password?: string;
}

export interface GuestDetailDto {
  firstName: string;
  lastName: string;
  email: string;
  dateOfBirth?: string;
  placeOfBirth?: string;
  gender?: string;
  nationality?: string;
  address?: string;
  passportNumber?: string;
  phoneNumber?: string;
  password: string;
}

export interface GuestSearchDto {
  firstName?: string;
  lastName?: string;
  email?: string;
}
