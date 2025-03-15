package at.ac.tuwien.sepr.groupphase.backend.service.validator;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CheckInDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CheckOutDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DetailedBookingDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DetailedRoomDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Booking;
import at.ac.tuwien.sepr.groupphase.backend.entity.InviteToRoom;
import at.ac.tuwien.sepr.groupphase.backend.enums.Gender;
import at.ac.tuwien.sepr.groupphase.backend.enums.Nationality;
import at.ac.tuwien.sepr.groupphase.backend.enums.RoleType;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class CheckInValidator {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final List<String> ALLOWED_FILE_TYPES = List.of("application/pdf");
    private static final long MAX_FILE_SIZE = 1024 * 1024 * 10;


    /**
     * Validates the {@link CheckInDto} and {@link MultipartFile} before checking in a customer.
     * Ensures that all required fields are valid, including the passport.
     *
     * @param checkInDto            the {@link CheckInDto} to be validated.
     * @param room                  a {@link DetailedRoomDto} to be validated against.
     * @param countCheckedIn        the number of guests already checked into the room.
     * @param booking               the booking of the check-in.
     * @param loggedIn              the logged in guest.
     * @throws ValidationException if the validation fails.
     */
    public void validateForCheckIn(CheckInDto checkInDto, DetailedRoomDto room, int countCheckedIn, DetailedBookingDto booking, ApplicationUser loggedIn, boolean checkedOut, InviteToRoom invite) throws ValidationException {
        LOG.trace("validateForCheckIn({})", checkInDto);
        List<String> validationErrors = new ArrayList<>();

        // Null Checks
        if (checkInDto.bookingId() == null) {
            validationErrors.add("Booking ID must not be null.");
        }

        // Booking Checks
        if (countCheckedIn + 1 > room.capacity()) {
            validationErrors.add("Room doesn't have enough capacity.");
        }
        if (!(Objects.equals(booking.userId(), loggedIn.getId()) || invite != null)) {
            validationErrors.add("Room can only be checked in by user who did the booking or an invited user.");
        }
        if (!(LocalDate.now().isAfter(booking.startDate().minusDays(1L)) && LocalDate.now().isBefore(booking.endDate()))) {
            validationErrors.add("Cannot check into a booking before the booked date.");
        }

        // Check-In check (checks if guest had already checked into the booking before)
        if (checkedOut) {
            validationErrors.add("Cannot check into a booking multiple times.");
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation failed for one or more reasons.", validationErrors);
        }
    }

    /**
     * Validates the {@link CheckInDto} and {@link MultipartFile} before adding a customer to a room.
     * Ensures that all required fields are valid, including the passport.
     *
     * @param room           a {@link DetailedRoomDto} to be validated against.
     * @param countCheckedIn the number of guests already checked into the room.
     * @param booking        the booking of the check-in.
     * @param loggedIn       the logged in guest.
     * @throws ValidationException if the validation fails.
     */
    public void validateForAddToRoom(Long bookingId, DetailedRoomDto room, int countCheckedIn, DetailedBookingDto booking, ApplicationUser loggedIn) throws ValidationException {
        LOG.trace("validateForAddToRoom({})", bookingId);
        List<String> validationErrors = new ArrayList<>();

        // Null Checks
        if (bookingId == null) {
            validationErrors.add("Booking ID must not be null.");
        }

        // Booking Checks
        if (countCheckedIn + 1 > room.capacity()) {
            validationErrors.add("Room doesn't have enough capacity.");
        }
        if (!(LocalDate.now().isAfter(booking.startDate().minusDays(1L)) && LocalDate.now().isBefore(booking.endDate()))) {
            validationErrors.add("Cannot check into a room before the booked date.");
        }
        if (!(Objects.equals(booking.userId(), loggedIn.getId()))) {
            if (!loggedIn.hasAuthority(RoleType.ROLE_ADMIN) || loggedIn.hasAuthority(RoleType.ROLE_RECEPTIONIST)) {
                validationErrors.add("Cannot add guest to room as guest who doesn't own the room.");
            }
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation failed for one or more reasons.", validationErrors);
        }
    }

    /**
     * Validates the guest information before saving it into the database.
     * Ensures that all required fields are valid, including the passport.
     *
     * @param checkInDto the guest information to be validated.
     * @param passport a {@link MultipartFile} representing the passport of the customer, which needs to be validated against the given information.
     * @throws ValidationException if the validation fails.
     */
    public void validateGuestInformation(CheckInDto checkInDto, MultipartFile passport) throws ValidationException {
        LOG.trace("validateGuestInformation({})", checkInDto);
        List<String> validationErrors = new ArrayList<>();

        // Null Checks
        if (checkInDto.firstName() == null) {
            validationErrors.add("First Name must not be null.");
        }
        if (checkInDto.lastName() == null) {
            validationErrors.add("Last Name must not be null.");
        }
        if (checkInDto.dateOfBirth() == null) {
            validationErrors.add("Date of Birth must not be null.");
        }
        if (checkInDto.placeOfBirth() == null) {
            validationErrors.add("Place of Birth must not be null.");
        }
        if (checkInDto.gender() == null) {
            validationErrors.add("Gender must not be null.");
        }
        if (checkInDto.nationality() == null) {
            validationErrors.add("Nationality must not be null.");
        }
        if (checkInDto.address() == null) {
            validationErrors.add("Address must not be null.");
        }
        if (checkInDto.passportNumber() == null) {
            validationErrors.add("Passport Number must not be null.");
        }
        if (checkInDto.phoneNumber() == null) {
            validationErrors.add("Phone Number must not be null.");
        }

        // String Validation
        if (checkInDto.firstName() != null && (checkInDto.firstName().length() < 3 || checkInDto.firstName().length() > 64)) {
            validationErrors.add("First Name must be between 3 and 64 characters.");
        }
        if (checkInDto.lastName() != null && (checkInDto.lastName().length() < 3 || checkInDto.lastName().length() > 64)) {
            validationErrors.add("Last Name must be between 3 and 64 characters.");
        }
        int dateOfBirthLength = String.valueOf(checkInDto.dateOfBirth()).length();
        if (checkInDto.dateOfBirth() != null && (dateOfBirthLength < 3 || dateOfBirthLength > 128)) {
            validationErrors.add("Date of Birth must be between 3 and 128 characters.");
        }
        if (checkInDto.placeOfBirth() != null && (checkInDto.placeOfBirth().length() < 3 || checkInDto.placeOfBirth().length() > 128)) {
            validationErrors.add("Place of Birth must be between 3 and 128 characters.");
        }
        if (checkInDto.address() != null && (checkInDto.address().length() < 3 || checkInDto.address().length() > 256)) {
            validationErrors.add("Address must be between 3 and 256 characters.");
        }

        // Gender Validation
        if (checkInDto.gender() != null && !EnumUtils.isValidEnum(Gender.class, checkInDto.gender().name())) {
            validationErrors.add("Gender is malformed, it should be MALE, FEMALE or DIVERSE.");
        }

        // Nationality Validation
        if (checkInDto.nationality() != null && !EnumUtils.isValidEnum(Nationality.class, checkInDto.nationality().name())) {
            validationErrors.add("Nationality is malformed.");
        }

        // Date of Birth Validation
        if (checkInDto.dateOfBirth() != null && checkInDto.dateOfBirth().isAfter(LocalDate.now().minusYears(18))) {
            validationErrors.add("Must be at least 18 years old.");
        }

        // Phone Number Validation
        if (checkInDto.phoneNumber() != null && !checkInDto.phoneNumber().matches("^(\\+?[0-9]{1,19}|[0-9]{20})$")) {
            validationErrors.add("Phone Number must contain at most 1 '+' followed by up to 19 numbers, or up to 20 numbers with no leading '+'.");
        }

        // Passport Number Validation
        if (checkInDto.passportNumber() != null && !checkInDto.passportNumber().matches("^[A-Za-z0-9]{6,9}$")) {
            validationErrors.add("Passport must be 6 to 9 characters long and contain only letters and digits.");
        }


        // Passport Validation TODO: Verify Passport Detail Information
        if (passport != null) {
            if (!ALLOWED_FILE_TYPES.contains(passport.getContentType())) {
                validationErrors.add("Invalid file type. Allowed types are: PDF.");
            }
            if (passport.getSize() > MAX_FILE_SIZE) {
                validationErrors.add("Passport file size exceeds the maximum limit of 10 MB.");
            }
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation failed for one or more fields.", validationErrors);
        }
    }

    /**
     * Validates the {@link CheckOutDto} before checking out a customer.
     * Ensures that all required fields are valid.
     *
     * @param checkOutDto           the {@link CheckOutDto} to be validated.
     * @throws ValidationException  if the validation fails.
     */
    public void validateForCheckOut(CheckOutDto checkOutDto, Booking booking) throws ValidationException {
        LOG.trace("validateForCheckOut({})", checkOutDto);
        List<String> validationErrors = new ArrayList<>();

        // Null Checks
        if (checkOutDto.bookingId() == null) {
            validationErrors.add("Booking ID must not be null.");
        }
        if (checkOutDto.email() == null) {
            validationErrors.add("Email must not be null.");
        }

        if (booking != null && !booking.isPaid()) {
            validationErrors.add("Checkout is not possible because the booking has not been paid yet.");
        }

        if (booking != null && booking.getUser() != null && !Objects.equals(booking.getUser().getEmail(), checkOutDto.email())) {
            validationErrors.add("Checkout can only be completed by the owner of the booking.");
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation failed for one or more fields.", validationErrors);
        }
    }

    /**
     * Validates the guest information before saving it into the database.
     * Ensures that all required fields are valid, including the passport.
     *
     * @param checkInDto the guest information to be validated.
     * @param passport a {@link MultipartFile} representing the passport of the customer, which needs to be validated against the given information.
     * @throws ValidationException if the validation fails.
     */
    public void validateGuestInformationForAddToRoom(CheckInDto checkInDto, MultipartFile passport) throws ValidationException {
        LOG.trace("validateGuestInformationForAddToRoom({})", checkInDto);
        List<String> validationErrors = new ArrayList<>();

        // Null Checks
        if (checkInDto.firstName() == null) {
            validationErrors.add("First Name must not be null.");
        }
        if (checkInDto.lastName() == null) {
            validationErrors.add("Last Name must not be null.");
        }
        if (checkInDto.dateOfBirth() == null) {
            validationErrors.add("Date of Birth must not be null.");
        }
        if (checkInDto.placeOfBirth() == null) {
            validationErrors.add("Place of Birth must not be null.");
        }
        if (checkInDto.gender() == null) {
            validationErrors.add("Gender must not be null.");
        }
        if (checkInDto.nationality() == null) {
            validationErrors.add("Nationality must not be null.");
        }
        if (checkInDto.address() == null) {
            validationErrors.add("Address must not be null.");
        }
        if (checkInDto.passportNumber() == null) {
            validationErrors.add("Passport Number must not be null.");
        }
        if (checkInDto.phoneNumber() == null) {
            validationErrors.add("Phone Number must not be null.");
        }

        // String Validation
        if (checkInDto.firstName() != null && (checkInDto.firstName().length() < 3 || checkInDto.firstName().length() > 64)) {
            validationErrors.add("First Name must be between 3 and 64 characters.");
        }
        if (checkInDto.lastName() != null && (checkInDto.lastName().length() < 3 || checkInDto.lastName().length() > 64)) {
            validationErrors.add("Last Name must be between 3 and 64 characters.");
        }
        int dateOfBirthLength = String.valueOf(checkInDto.dateOfBirth()).length();
        if (checkInDto.dateOfBirth() != null && (dateOfBirthLength < 3 || dateOfBirthLength > 128)) {
            validationErrors.add("Date of Birth must be between 3 and 128 characters.");
        }
        if (checkInDto.placeOfBirth() != null && (checkInDto.placeOfBirth().length() < 3 || checkInDto.placeOfBirth().length() > 128)) {
            validationErrors.add("Place of Birth must be between 3 and 128 characters.");
        }
        if (checkInDto.address() != null && (checkInDto.address().length() < 3 || checkInDto.address().length() > 256)) {
            validationErrors.add("Address must be between 3 and 256 characters.");
        }

        // Gender Validation
        if (checkInDto.gender() != null && !EnumUtils.isValidEnum(Gender.class, checkInDto.gender().name())) {
            validationErrors.add("Gender is malformed, it should be MALE, FEMALE or DIVERSE.");
        }

        // Nationality Validation
        if (checkInDto.nationality() != null && !EnumUtils.isValidEnum(Nationality.class, checkInDto.nationality().name())) {
            validationErrors.add("Nationality is malformed.");
        }

        // Phone Number Validation
        if (checkInDto.phoneNumber() != null && !checkInDto.phoneNumber().matches("^(\\+?[0-9]{1,19}|[0-9]{20})$")) {
            validationErrors.add("Phone Number must contain at most 1 '+' followed by up to 19 numbers, or up to 20 numbers with no leading '+'.");
        }

        // Passport Number Validation
        if (checkInDto.passportNumber() != null && !checkInDto.passportNumber().matches("^[A-Za-z0-9]{6,9}$")) {
            validationErrors.add("Passport must be 6 to 9 characters long and contain only letters and digits.");
        }


        // Passport Validation TODO: Verify Passport Detail Information
        if (passport != null) {
            if (!ALLOWED_FILE_TYPES.contains(passport.getContentType())) {
                validationErrors.add("Invalid file type. Allowed types are: PDF.");
            }
            if (passport.getSize() > MAX_FILE_SIZE) {
                validationErrors.add("Passport file size exceeds the maximum limit of 10 MB.");
            }
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation failed for one or more fields.", validationErrors);
        }
    }
}
