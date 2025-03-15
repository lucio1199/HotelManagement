package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ActivityBookingPaymentRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PaymentRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.StripeSessionDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Booking;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;

public interface PaymentService {

    /**
     * Creates a new checkout session for the given payment request.
     *
     * @param paymentRequest The payment request.
     * @return The created checkout session.
     * @throws ConflictException If the payment request is invalid.
     */
    StripeSessionDto createCheckoutSession(PaymentRequestDto paymentRequest) throws ConflictException;

    /**
     * Processes paymentRefund for a booking.
     *
     * @param booking The booking that was paid for.
     * @throws ConflictException If the refund processing fails.
     */
    void processRoomBookingRefund(Booking booking) throws ConflictException;

    /**
     * Creates a new checkout session for the given activity booking payment request.
     *
     * @param paymentRequest The activity booking payment request.
     * @return The created checkout session.
     * @throws ConflictException If the payment request is invalid.
     */
    StripeSessionDto createActivityBookingCheckoutSession(ActivityBookingPaymentRequestDto paymentRequest) throws ConflictException;
}
