package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.BookingCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DetailedBookingDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EmployeeBookingDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import com.stripe.exception.StripeException;
import jakarta.mail.MessagingException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public interface BookingService {

    /**
     * Creates a new booking.
     *
     * @param bookingCreateDto Details of the booking (room ID, user ID, dates).
     * @return Detailed information about the created booking.
     * @throws ValidationException If the room is not available or data is invalid.
     * @throws ConflictException If there is a conflict during booking creation.
     * @throws MessagingException If there is an error with sending emails.
     * @throws IOException If there is an error generating PDF or other resources.
     */
    DetailedBookingDto createBooking(BookingCreateDto bookingCreateDto) throws ValidationException, ConflictException, MessagingException, IOException;

    /**
     * Retrieves all bookings for a specific user identified by their email.
     *
     * @param email The email of the user.
     * @return A list of detailed booking DTOs for the user.
     */
    List<DetailedBookingDto> findByUserId(String email);


    /**
     * Finds a booking by its ID.
     *
     * @param bookingId The ID of the booking.
     * @return Detailed booking DTO of the specified booking.
     */
    DetailedBookingDto findBookingById(Long bookingId);


    /**
     * Checks if a room is available for a specific date range.
     *
     * @param roomId    The ID of the room to check.
     * @param startDate Start date of the desired booking period.
     * @param endDate   End date of the desired booking period.
     * @return True if the room is available; false otherwise.
     */
    boolean isRoomAvailable(Long roomId, LocalDate startDate, LocalDate endDate);

    /**
     * Retrieves all bookings.
     *
     * @return A list of all bookings.
     */
    List<EmployeeBookingDto> getAllBookings();

    /**
     * Cancels a booking.
     *
     * @param bookingId The ID of the booking to cancel.
     * @throws NotFoundException If the booking is not found.
     * @throws IOException If there is an error generating cancellation documents.
     * @throws MessagingException If there is an error with sending cancellation emails.
     * @throws ConflictException If there is a conflict when canceling the booking.
     */
    void cancelBooking(Long bookingId) throws NotFoundException, IOException, MessagingException, ConflictException;

    /**
     * Updates the payment status of a booking.
     *
     * @param bookingId The ID of the booking to update.
     * @throws NotFoundException If the booking is not found.
     */
    boolean updatePaymentStatus(Long bookingId) throws NotFoundException;

    /**
     * Retrieves a paginated list of all bookings.
     *
     * @param pageable The pageable object containing pagination details.
     * @return A page of all bookings.
     */
    Page<EmployeeBookingDto> getPagedBookings(Pageable pageable);

    /**
     * Marks a booking as paid manually.
     *
     * @param bookingId The ID of the booking to mark as paid.
     * @throws NotFoundException If the booking is not found.
     * @throws ConflictException If there is a conflict when marking the booking as paid.
     */
    void markAsPaidManually(Long bookingId) throws NotFoundException, ConflictException;
}
