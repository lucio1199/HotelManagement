package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A detailed data transfer object for bookings made by employees.
 * Represents booking details, including room and user information, as well as employee-related details.
 *
 * <p>This DTO contains comprehensive information about a booking, including details of the booking, room, user, and employee. It also includes booking status, payment status, and other relevant data.</p>
 *
 * @param id the unique identifier for the booking
 * @param roomId the ID of the room booked
 * @param userId the ID of the user who made the booking
 * @param startDate the start date of the booking
 * @param endDate the end date of the booking
 * @param roomName the name of the booked room
 * @param email the email address of the user making the booking
 * @param price the total price for the booking
 * @param isActive indicates whether the booking is currently active
 * @param firstName the first name of the user making the booking
 * @param lastName the last name of the user making the booking
 * @param address the address of the user making the booking
 * @param dateOfBirth the date of birth of the user making the booking
 * @param nationality the nationality of the user making the booking
 * @param passportNumber the passport number of the user making the booking
 * @param phoneNumber the phone number of the user making the booking
 * @param gender the gender of the user making the booking
 * @param placeOfBirth the place of birth of the user making the booking
 * @param capacity the maximum capacity of the booked room
 * @param lastCleanedAt the last time the room was cleaned
 * @param isPaid indicates whether the booking has been paid for
 * @param status the current status of the booking (e.g., "active", "cancelled")
 * @param bookingNumber a unique reference number for the booking
 * @param bookingDate the date when the booking was made
 * @param totalAmount the total amount of the booking, including any applicable fees
 * @param numberOfNights the total number of nights booked
 */
public record EmployeeBookingDto(
        Long id,
        Long roomId,
        Long userId,
        LocalDate startDate,
        LocalDate endDate,
        String roomName,
        String email,
        Double price,
        Boolean isActive,
        String firstName,
        String lastName,
        String address,
        String dateOfBirth,
        String nationality,
        String passportNumber,
        String phoneNumber,
        String gender,
        String placeOfBirth,
        Integer capacity,
        LocalDateTime lastCleanedAt,
        Boolean isPaid,
        String status,
        String bookingNumber,
        LocalDate bookingDate,
        Double totalAmount,
        Integer numberOfNights,
        String transactionId
) {
}
