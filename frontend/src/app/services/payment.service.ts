import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {Globals} from "../global/globals";
import {ActivityPaymentRequestDto, PaymentRequestDto, StripeSessionDto} from "../dtos/payment-request";

@Injectable({
  providedIn: 'root'
})
export class PaymentService {


  private paymentBaseUri: string = this.globals.backendUri + '/payment';

  constructor(
    private http: HttpClient,
    private globals: Globals) { }

  checkout(paymentRequest: PaymentRequestDto) {
    console.log(paymentRequest);
    return this.http.post<StripeSessionDto>(this.paymentBaseUri + '/create-checkout-session', paymentRequest, { observe: 'body' })
  }

  redirectToCheckout(checkoutUrl: string): void {
    window.location.href = checkoutUrl;
  }

  activityBookingCheckout(paymentRequest: ActivityPaymentRequestDto) {
    console.log(paymentRequest);
    return this.http.post<StripeSessionDto>(this.paymentBaseUri + '/activity-booking-checkout', paymentRequest, { observe: 'body' })
  }


}
