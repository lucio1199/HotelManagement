export class PaymentRequestDto {
  roomId: number;
  bookingId: number;
}

export class StripeSessionDto {
  url: string;
  sessionId: string;
}

export class ActivityPaymentRequestDto {
  activityId: number;
  activityBookingId: number;
}
