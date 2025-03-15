package at.ac.tuwien.sepr.groupphase.backend.service.validator;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.BookingCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.enums.PaymentMethod;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class BookingValidator {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Validates the {@link BookingCreateDto} before creating a new booking.
     * Ensures that all required fields are valid, including dates and user information.
     *
     * @param bookingCreateDto the {@link BookingCreateDto} to be validated.
     * @param loggedInEmail the email of the logged-in user who is making the booking.
     * @throws ValidationException if the validation fails, with a list of errors.
     */
    public void validateForCreate(BookingCreateDto bookingCreateDto, String loggedInEmail) throws ValidationException {
        LOG.trace("validateForCreate({})", bookingCreateDto);
        List<String> validationErrors = new ArrayList<>();

        validateRoomId(bookingCreateDto.roomId(), validationErrors);
        validateEmail(loggedInEmail, validationErrors);
        validateDates(bookingCreateDto.startDate(), bookingCreateDto.endDate(), validationErrors);
        validatePaymentMethod(bookingCreateDto.paymentMethod(), validationErrors);

        if (!validationErrors.isEmpty()) {
            LOG.debug("Validation errors: {}", validationErrors);
            throw new ValidationException("Validation failed for one or more fields.", validationErrors);
        }
    }

    /**
     * Validates the room ID.
     * Ensures that the room ID is not null and is a positive number.
     *
     * @param roomId the room ID to validate.
     * @param errors the list of validation errors.
     */
    private void validateRoomId(Long roomId, List<String> errors) {
        if (roomId == null) {
            errors.add("Room ID must not be null.");
        } else if (roomId <= 0) {
            errors.add("Room ID must be a positive number.");
        }
    }

    /**
     * Validates the email format.
     * Ensures that the email is not null, is not blank, and has a valid format.
     *
     * @param email the email to validate.
     * @param errors the list of validation errors.
     */
    private void validateEmail(String email, List<String> errors) {
        if (email == null || email.isBlank()) {
            errors.add("Email must not be null or blank.");
        } else if (!email.matches("^[\\w-_.+]*[\\w-_.]@[\\w]+[.][a-z]+$")) {
            errors.add("Email format is invalid.");
        } else if (email.length() > 255) {
            errors.add("Email must not exceed 255 characters.");
        }
    }


    /**
     * Validates the start and end dates for the booking.
     * Ensures that the dates are valid, the start date is in the future, and the start date is before or equal to the end date.
     *
     * @param startDate the start date of the booking.
     * @param endDate the end date of the booking.
     * @param errors the list of validation errors.
     */
    private void validateDates(LocalDate startDate, LocalDate endDate, List<String> errors) {
        if (startDate == null) {
            errors.add("Start date must not be null.");
        }
        if (endDate == null) {
            errors.add("End date must not be null.");
        }

        if (startDate != null && endDate != null) {
            if (startDate.isAfter(endDate)) {
                errors.add("Start date must be before or equal to the end date.");
            }
            if (startDate.isBefore(LocalDate.now().minusDays(1L))) {
                errors.add("Start date must be today or in the future.");
            }
        }

        if (startDate != null && startDate.isEqual(endDate)) {
            errors.add("Start date and end date must be at least 1 day apart.");
        }
    }

    /**
     * Validates the payment method for the booking.
     * Ensures that the provided payment method is a valid value from the {@link PaymentMethod} enum.
     *
     * @param paymentMethod the payment method to validate.
     * @param errors the list of validation errors.
     */
    private void validatePaymentMethod(String paymentMethod, List<String> errors) {
        try {
            PaymentMethod.valueOf(paymentMethod);
        } catch (IllegalArgumentException | NullPointerException e) {
            errors.add("Invalid payment method: " + paymentMethod);
        }
    }
}
