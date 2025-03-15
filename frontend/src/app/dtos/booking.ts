export class BookingCreateDto {
  roomId: number;
  userId: number;
  startDate: string;
  endDate: string;
  roomName: string;
}

export class BookingDetailDto {
  id: number;
  roomId: number;
  userId: number;
  startDate: Date;
  endDate: Date;
  roomName: String;
  userFullName: String;
  price: number;
  status: string;
}

export class DetailedBookingDto {
  id: number;
  bookingNumber: string;
  roomName: string;
  startDate: string;
  endDate: string;
  price: number;
  email: string;
  firstName: string;
  lastName: string;
  address: string;
  nationality: string;
  phoneNumber: string;
  passportNumber: string;
  gender: string;
  dateOfBirth: string;
  placeOfBirth: string;
  isActive: boolean;
  capacity: number;
  lastCleanedAt: string;
  bookingDate: Date;
  isPaid: boolean;
  status: string;
  totalAmount: number;
  numberOfNights: number;
  transactionId: string;
}
