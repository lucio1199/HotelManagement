package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ActivityBookingPaymentRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PaymentRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.StripeSessionDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Activity;
import at.ac.tuwien.sepr.groupphase.backend.entity.ActivityBooking;
import at.ac.tuwien.sepr.groupphase.backend.entity.Booking;
import at.ac.tuwien.sepr.groupphase.backend.entity.Room;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ActivityBookingRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ActivityRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.BookingRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoomRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.PaymentService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;

import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class SimplePaymentService implements PaymentService {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final ActivityRepository activityRepository;
    private final ActivityBookingRepository activityBookingRepository;

    @Value("${spring.stripe.secret_key}")
    private String stripeApiKey;

    public SimplePaymentService(RoomRepository roomRepository, BookingRepository bookingRepository, ActivityRepository activityRepository, ActivityBookingRepository activityBookingRepository) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
        this.activityRepository = activityRepository;
        this.activityBookingRepository = activityBookingRepository;
    }

    @Override
    public StripeSessionDto createCheckoutSession(PaymentRequestDto paymentRequest) throws ConflictException {
        log.debug("Create new payment checkout {}", paymentRequest);
        Room room = roomRepository.findById(paymentRequest.roomId())
            .orElseThrow(() -> new NotFoundException("Room not found"));
        Booking booking = bookingRepository.findById(paymentRequest.bookingId())
            .orElseThrow(() -> new NotFoundException("Booking not found"));

        if (booking.getStripeSessionId() != null) {
            try {
                Session session = Session.retrieve(booking.getStripeSessionId());
                PaymentIntent paymentIntent = PaymentIntent.retrieve(session.getPaymentIntent());
                if (paymentIntent.getStatus().equals("succeeded")) {
                    throw new ConflictException("Payment already succeeded", List.of(""));
                } else if (paymentIntent.getStatus().equals("processing")) {
                    throw new ConflictException("Payment is still processing", List.of(""));
                }
            } catch (StripeException e) {
                log.error("Failed to retrieve session", e);
            }
        }



        long nights = booking.calculateNumberOfNights();
        long totalPriceInCents = Math.round(booking.getTotalAmount() * 100);

        try {
            String domain = "http://localhost:4200/#/";
            Stripe.apiKey = stripeApiKey;
            SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(domain + "/bookings/my-bookings/success/" + paymentRequest.bookingId())
                .setCancelUrl(domain + "/bookings/my-bookings/cancel/" + paymentRequest.bookingId())
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.SEPA_DEBIT)
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.KLARNA)
                .addLineItem(
                    SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("eur")
                                .setUnitAmount(totalPriceInCents)
                                .setProductData(
                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName("Payment for Room " + room.getName())
                                        .setDescription("Room booking for " + nights + " nights")
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
                .putMetadata("bookingId", String.valueOf(paymentRequest.bookingId()))
                .build();

            Session session = Session.create(params);
            booking.setStripeSessionId(session.getId());
            bookingRepository.save(booking);
            log.debug("session id: {}", session.getId() + " url: " + session.getSuccessUrl());
            return new StripeSessionDto(session.getUrl(), session.getId());
        } catch (StripeException e) {
            log.error("Failed to create session", e);
            return null;
        }
    }

    /**
     * Process refund for a booking.
     *
     * @param booking the booking to refund
     */
    public void processRoomBookingRefund(Booking booking) throws ConflictException {
        try {
            if (booking.getStripePaymentIntentId() == null) {
                throw new ConflictException("No payment found for booking", List.of(""));
            }

            RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(booking.getStripePaymentIntentId())
                .build();

            Refund refund = Refund.create(params);
            log.info("Refund successful for booking ID {}: Refund ID {}", booking.getId(), refund.getId());
        } catch (StripeException e) {
            log.error("Error occurred while refunding booking ID {}: {}", booking.getId(), e.getMessage(), e);
        }
    }

    @Override
    public StripeSessionDto createActivityBookingCheckoutSession(ActivityBookingPaymentRequestDto paymentRequest) throws ConflictException {
        log.debug("Create new payment checkout {}", paymentRequest);

        ActivityBooking activityBooking = activityBookingRepository.findActivityBookingById(paymentRequest.activityBookingId())
            .orElseThrow(() -> new NotFoundException("Activity booking not found"));

        Activity activity = activityBooking.getActivity();

        if (activityBooking.getStripeSessionId() != null) {
            try {
                Session session = Session.retrieve(activityBooking.getStripeSessionId());
                PaymentIntent paymentIntent = PaymentIntent.retrieve(session.getPaymentIntent());
                if (paymentIntent.getStatus().equals("succeeded")) {
                    throw new ConflictException("Payment already succeeded", List.of(""));
                } else if (paymentIntent.getStatus().equals("processing")) {
                    throw new ConflictException("Payment is still processing", List.of(""));
                }
            } catch (StripeException e) {
                log.error("Failed to retrieve session", e);
            }
        }

        long totalPriceInCents = Math.round(activityBooking.getTotalAmount() * 100);

        try {
            String domain = "http://localhost:4200/#/";
            Stripe.apiKey = stripeApiKey;
            SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(domain + "/bookings/my-bookings/activity/success/" + paymentRequest.activityBookingId())
                .setCancelUrl(domain + "/bookings/my-bookings/activity/cancel/" + paymentRequest.activityBookingId())
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.SEPA_DEBIT)
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.KLARNA)
                .addLineItem(
                    SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("eur")
                                .setUnitAmount(totalPriceInCents)
                                .setProductData(
                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName("Payment for Activity " + activity.getName())
                                        .setDescription("Activity booking for " + activityBooking.getParticipants() + " participants")
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
                .putMetadata("bookingId", String.valueOf(paymentRequest.activityBookingId()))
                .build();

            Session session = Session.create(params);
            activityBooking.setStripeSessionId(session.getId());
            activityBookingRepository.save(activityBooking);
            log.debug("session id: {}", session.getId() + " url: " + session.getSuccessUrl());
            return new StripeSessionDto(session.getUrl(), session.getId());
        } catch (StripeException e) {
            log.error("Failed to create session", e);
            return null;
        }
    }
}
