package at.ac.tuwien.sepr.groupphase.backend.service.validator;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.GuestCreateUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.GuestSignupDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Booking;
import at.ac.tuwien.sepr.groupphase.backend.entity.Guest;
import at.ac.tuwien.sepr.groupphase.backend.enums.Gender;
import at.ac.tuwien.sepr.groupphase.backend.enums.Nationality;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class GuestValidator {

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 255;

    /**
     * Validates the {@link GuestSignupDto} before creating a new guest account.
     * Ensures that all required fields are valid and the password meets security requirements.
     *
     * @param guestSignupDto the {@link GuestSignupDto} to be validated
     * @throws ValidationException if the validation fails
     */
    public void validateForSignup(GuestSignupDto guestSignupDto) throws ValidationException {
        log.trace("validateForSignup({})", guestSignupDto);
        List<String> validationErrors = new ArrayList<>();

        if (guestSignupDto.email() == null) {
            validationErrors.add("Email must not be null.");
        }
        if (guestSignupDto.password() == null) {
            validationErrors.add("Password must not be null.");
        }

        if (guestSignupDto.email() != null) {
            if (!guestSignupDto.email().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                validationErrors.add("Invalid email format.");
            }
            if (guestSignupDto.email().length() > 255) {
                validationErrors.add("Email must not exceed 255 characters.");
            }
        }

        if (guestSignupDto.password() != null) {
            validatePassword(guestSignupDto.password(), validationErrors);
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation failed for one or more fields.", validationErrors);
        }
    }

    /**
     * Validates the {@link GuestCreateUpdateDto} before creating or updating a guest.
     *
     * @param guestDto the {@link GuestCreateUpdateDto} to be validated
     * @throws ValidationException if the validation fails
     */
    public void validateForCreateOrUpdate(GuestCreateUpdateDto guestDto) throws ValidationException {
        log.trace("validateForCreateOrUpdate({})", guestDto);
        List<String> validationErrors = new ArrayList<>();

        if (guestDto.firstName() == null) {
            validationErrors.add("First Name must not be empty.");
        }
        if (guestDto.lastName() == null) {
            validationErrors.add("Last Name must not be empty.");
        }
        if (guestDto.password() == null) {
            validationErrors.add("Password must not be empty.");
        }

        if (guestDto.firstName() != null && (guestDto.firstName().length() < 3 || guestDto.firstName().length() > 64)) {
            validationErrors.add("First Name must be between 3 and 64 characters.");
        }
        if (guestDto.lastName() != null && (guestDto.lastName().length() < 3 || guestDto.lastName().length() > 64)) {
            validationErrors.add("Last Name must be between 3 and 64 characters.");
        }
        int dateOfBirthLength = String.valueOf(guestDto.dateOfBirth()).length();
        if (guestDto.dateOfBirth() != null && (dateOfBirthLength < 3 || dateOfBirthLength > 128)) {
            validationErrors.add("Date of Birth must be between 3 and 128 characters.");
        }
        if (guestDto.placeOfBirth() != null && (guestDto.placeOfBirth().length() < 3 || guestDto.placeOfBirth().length() > 128)) {
            validationErrors.add("Place of Birth must be between 3 and 128 characters.");
        }
        if (guestDto.address() != null && (guestDto.address().length() < 3 || guestDto.address().length() > 256)) {
            validationErrors.add("Address must be between 3 and 256 characters.");
        }

        // Gender Validation
        if (guestDto.gender() != null && !EnumUtils.isValidEnum(Gender.class, guestDto.gender())) {
            validationErrors.add("Gender is malformed, it should be MALE, FEMALE or DIVERSE.");
        }

        // Nationality Validation
        if (guestDto.nationality() != null && !EnumUtils.isValidEnum(Nationality.class, guestDto.nationality())) {
            validationErrors.add("Nationality is malformed.");
        }

        // Date of Birth Validation
        if (guestDto.dateOfBirth() != null && guestDto.dateOfBirth().isAfter(LocalDate.now().minusYears(18))) {
            validationErrors.add("Must be at least 18 years old.");
        }

        // Phone Number Validation
        if (guestDto.phoneNumber() != null && !guestDto.phoneNumber().matches("^(\\+?[0-9]{1,19}|[0-9]{20})$")) {
            validationErrors.add("Phone Number must contain at most 1 '+' followed by up to 19 numbers, or up to 20 numbers with no leading '+'.");
        }

        // Passport Number Validation
        if (guestDto.passportNumber() != null && !guestDto.passportNumber().matches("^[A-Za-z0-9]{6,9}$")) {
            validationErrors.add("Passport must be 6 to 9 characters long and contain only letters and digits.");
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation failed for one or more fields.", validationErrors);
        }
    }


    /**
     * Validates the password based on defined rules.
     *
     * @param password the password to validate
     * @param validationErrors the list to add validation errors
     */
    private void validatePassword(String password, List<String> validationErrors) {
        if (password.length() < MIN_PASSWORD_LENGTH) {
            validationErrors.add("Password must be at least 8 characters long.");
        }
        if (password.length() > MAX_PASSWORD_LENGTH) {
            validationErrors.add("Password must not exceed 255 characters.");
        }
        if (!password.matches(".*[A-Z].*")) {
            validationErrors.add("Password must contain at least one uppercase letter.");
        }
        if (!password.matches(".*[a-z].*")) {
            validationErrors.add("Password must contain at least one lowercase letter.");
        }
        if (!password.matches(".*\\d.*")) {
            validationErrors.add("Password must contain at least one number.");
        }
        if (!password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            validationErrors.add("Password must contain at least one special character (!@#$%^&*(),.?\":{}|<>).");
        }
    }

    /**
     * Validates the guest before deleting a guest account.
     *
     * @param guest the guest to validate
     * @throws ValidationException if the validation fails
     */
    public void validateForDelete(Guest guest, List<Booking> guestBookings) throws ValidationException {
        log.trace("validateForDelete({})", guest);
        List<String> validationErrors = new ArrayList<>();

        if (guest == null) {
            validationErrors.add("Guest does not exist.");
        }

        guestBookings.forEach(booking -> {
            if (booking.getEndDate().isAfter(LocalDate.now())) {
                validationErrors.add("Cannot delete guest with bookings.");
            }
        });


        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation failed for one or more fields.", validationErrors);
        }
    }
}
