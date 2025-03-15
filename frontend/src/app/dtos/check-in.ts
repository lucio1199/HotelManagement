export class CheckInDto {
  bookingId: string;
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  placeOfBirth: string;
  gender: string;
  nationality: string;
  address: string;
  passportNumber: string;
  phoneNumber: string;
  passport: File;
}

export class CheckOutDto {
  bookingId: string;
  email: string;
}

export class CheckInStatusDto {
  bookingId: string;
  email: string;
}

export class ManuallyAddToRoomDto {
  bookingId: string;
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  placeOfBirth: string;
  gender: string;
  nationality: string;
  address: string;
  passportNumber: string;
  phoneNumber: string;
  passport: File;
  email: string;
}
