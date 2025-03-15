package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.AddToRoomDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CheckInDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CheckInStatusDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CheckOutDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DetailedBookingDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DetailedRoomDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.GuestListDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InviteToRoomDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.OccupancyDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Message;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import jakarta.mail.MessagingException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.io.IOException;

public interface CheckInService {

    /**
     * Checks In a Guest.
     *
     * @param checkInDto Details of the check-in (booking ID).
     * @param passport The uploaded passport.
     * @param email The guest email.
     * @throws NotFoundException If the guest or booking is not found.
     * @throws ValidationException If the check-in data is invalid.
     * @throws ConflictException If the guest is already checked in.
     * @throws IOException If the passport file can't be handled properly.
     */
    void checkIn(CheckInDto checkInDto, MultipartFile passport, String email) throws NotFoundException, ValidationException, ConflictException, IOException;

    /**
     * Returns the Check In Information of a Guest.
     *
     * @param email The guest email.
     * @return The information of the Rooms of the corresponding Guest.
     * @throws NotFoundException If the given guest is not found or if they are not checked into any rooms.
     */
    DetailedRoomDto[] getGuestRooms(String email) throws NotFoundException;

    List<Long> getAllBookingIds();


    /**
     * Finds a booking by its ID.
     *
     * @param bookingId The ID of the booking.
     * @param id The ID of the logged in guest.
     * @return Detailed booking DTO of the specified booking.
     * @throws NotFoundException If the check in doesn't exist or doesn't belong to the logged in guest.
     */
    DetailedBookingDto findBookingById(Long bookingId, Long id) throws NotFoundException;

    /**
     * Checks if a guest (identified by email) is already checked in.
     *
     * @param email The email of the guest.
     * @return True if the checked in; false otherwise.
     */
    CheckInStatusDto[] getCheckedInStatus(String email);

    /**
     * Checks Out a Guest.
     *
     * @param checkOutDto Details of the check-out (booking ID, guest email).
     * @throws NotFoundException If the guest or booking is not found.
     * @throws ValidationException If the check-out data is invalid.
     */
    void checkOut(CheckOutDto checkOutDto) throws NotFoundException, ValidationException;

    /**
     * Returns the Booking Information of a Checked-In Guest.
     *
     * @param roomId The id of the room which corresponds to the requested booking.
     * @param email The guest email.
     * @return The information of the Booking.
     * @throws NotFoundException If the given guest is not found or if they are not checked into any rooms.
     */
    DetailedBookingDto getGuestBooking(Long roomId, String email) throws NotFoundException;

    /**
     * Checks if a room (identified by id) is occupied.
     *
     * @param id The id of the room.
     * @return The occupancy status of the room.
     * @throws NotFoundException If the given room is not found.
     */
    OccupancyDto getOccupancyStatus(Long id) throws NotFoundException;

    /**
     * Adds a Guest to a room.
     *
     * @param addToRoomDto Details of the added guest (booking ID).
     * @param passport The uploaded passport.
     * @param ownerEmail The email of the guest that owns the room.
     * @throws NotFoundException If the guest or booking is not found.
     * @throws ValidationException If the check-in data is invalid.
     * @throws ConflictException If the guest is already checked in.
     * @throws IOException If the passport file can't be handled properly.
     */
    void addToRoom(AddToRoomDto addToRoomDto, MultipartFile passport, String ownerEmail) throws NotFoundException, ValidationException, ConflictException, IOException;

    /**
     * Invites a Guest to a room.
     *
     * @param inviteToRoomDto The DTO that represents the necessary information for marking the guest so they can check in.
     * @param ownerEmail The email of the guest that owns the room.
     * @throws NotFoundException If the guest or booking is not found.
     * @throws ValidationException If the invite data is invalid.
     * @throws ConflictException If the guest is already checked in.
     * @throws MessagingException If there was an error sending the invitation email.
     */
    void inviteToRoom(InviteToRoomDto inviteToRoomDto, String ownerEmail) throws NotFoundException, ValidationException, ConflictException, MessagingException;


    /**
     * Returns the Information the Guests that were added to the logged in user's MyRoom.
     *
     * @param roomId The id of the room.
     * @param email The guest email.
     * @return The information of the Guests.
     * @throws NotFoundException If there are no guests found or if the logged in user is not checked into any rooms.
     */
    GuestListDto[] getGuests(Long roomId, String email) throws NotFoundException;

    /**
     * Returns true if the Guest identified by email is owner of the Room identified by id, otherwise false.
     *
     * @param roomId The id of the room.
     * @param email The guest email.
     * @return True if the guest is the owner, otherwise false.
     * @throws NotFoundException If there are no guests found or if the logged in user is not checked into any rooms.
     */
    boolean isOwner(Long roomId, String email) throws NotFoundException;

    /**
     * Returns the Information the Guests that are staying in the Room that is connected to the Booking.
     *
     * @param bookingId The id of the booking.
     * @return The information of the Guests.
     * @throws NotFoundException If there are no guests found.
     */
    GuestListDto[] getAllGuests(Long bookingId) throws NotFoundException;

    /**
     * Returns the Passport of the Guest identified by email that is checked into to the Booking.
     *
     * @param bookingId The id of the booking.
     * @param email the email of the guest.
     * @return The bytes of the passport.
     * @throws NotFoundException If there are no guests found with that email or if they are not checked in.
     */
    byte[] getPassportByBookingIdAndEmail(Long bookingId, String email) throws NotFoundException;

    /**
     * Checks out all guests whose bookings have run out today.
     *
     * @return The number of guests that were checked-out.
     */
    int performAutoCheckOut();

    /**
     * Remove a guest from a room.
     *
     * @param email The guest's email.
     * @param bookingId The ID of the booking.
     * @throws NotFoundException If the guest or booking is not found.
     */
    void remove(Long bookingId, String email) throws NotFoundException;
}
