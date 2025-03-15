package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import at.ac.tuwien.sepr.groupphase.backend.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Finds all bookings associated with a specific ApplicationUser ID.
     *
     * @param userId the ID of the ApplicationUser
     * @return a list of bookings associated with the specified ApplicationUser ID
     */
    List<Booking> findByUserId(Long userId);

    /**
     * Finds all bookings for a specific room ID within a specified date range.
     *
     * @param roomId the ID of the room
     * @param startDate the start of the date range
     * @param endDate the end of the date range
     * @return a list of bookings for the specified room and date range
     */
    List<Booking> findBookingsByRoomIdAndStartDateBetween(Long roomId, LocalDate startDate, LocalDate endDate);

    /**
     * Finds a booking by its ID.
     *
     * @param id the ID of the booking
     * @return an {@link Optional} containing the booking if found, or empty if not
     */
    Optional<Booking> findBookingById(Long id);

    /**
     * Counts the number of bookings for a specific room ID within a given date range and status.
     *
     * @param roomId the ID of the room
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @param statuses a list of {@link BookingStatus} values to filter by
     * @return the number of bookings matching the given criteria
     */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.room.id = :roomId AND b.startDate <= :endDate AND b.endDate >= :startDate AND b.status IN :statuses")
    long countByRoomIdAndDateRangeAndStatus(@Param("roomId") Long roomId,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate,
                                            @Param("statuses") List<BookingStatus> statuses);

    /**
     * Finds all bookings with a specific end date.
     *
     * @param endDate the end date of the booking
     * @return an {@link Optional} containing a list of bookings with the specified end date
     */
    Optional<List<Booking>> findBookingsByEndDate(LocalDate endDate);

    /**
     * Finds all bookings with pagination support.
     *
     * @param pageable the pagination details
     * @return a {@link Page} containing the bookings
     */
    @Query("SELECT b FROM Booking b ORDER BY "
        + "CASE WHEN b.status = 'ACTIVE' THEN 1 ELSE 2 END, "
        + "b.startDate ASC")
    Page<Booking> findAllWithCustomSorting(Pageable pageable);
}
