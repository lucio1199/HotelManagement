package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ActivityBookingPaymentRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PaymentRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.StripeSessionDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.repository.BookingRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoomRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.PaymentService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/payment")
@Slf4j
public class PaymentStripeEndpoint {

    private PaymentService paymentService;

    @Autowired
    public PaymentStripeEndpoint(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Endpoint to create a checkout session for room booking a payment.
     *
     * @param paymentRequest the payment request
     * @return the stripe session
     * @throws ConflictException if the payment request is invalid
     */
    @Secured("ROLE_GUEST")
    @PostMapping("/create-checkout-session")
    @ResponseStatus(HttpStatus.OK)
    public StripeSessionDto createCheckoutSession(@RequestBody PaymentRequestDto paymentRequest) throws ConflictException {
        log.info("POST /api/v1/payment/create-checkout-session");
        log.info("Received PaymentRequestDto: {}", paymentRequest);
        return paymentService.createCheckoutSession(paymentRequest);
    }

    /**
     * Endpoint to create a checkout session for an activity booking payment.
     *
     * @param paymentRequest the activity booking payment request
     * @return the stripe session
     * @throws ConflictException if the payment request is invalid
     */
    @Secured("ROLE_GUEST")
    @PostMapping("/activity-booking-checkout")
    @ResponseStatus(HttpStatus.OK)
    public StripeSessionDto createActivityBookingCheckoutSession(@RequestBody ActivityBookingPaymentRequestDto paymentRequest) throws ConflictException {
        log.info("POST /api/v1/payment/activity-booking-checkout");
        log.info("Received PaymentRequestDto: {}", paymentRequest);
        return paymentService.createActivityBookingCheckoutSession(paymentRequest);
    }


}

