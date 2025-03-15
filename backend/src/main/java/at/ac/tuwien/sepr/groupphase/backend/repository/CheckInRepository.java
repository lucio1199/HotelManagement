package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Booking;
import at.ac.tuwien.sepr.groupphase.backend.entity.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, Long> {

    Optional<List<CheckIn>> findCheckInByGuest(ApplicationUser guest);

    List<CheckIn> findCheckInByBooking(Booking booking);

    List<CheckIn> findCheckInByBookingAndGuest(Booking booking, ApplicationUser guest);

    Optional<List<CheckIn>> findCheckInByGuestOrderByDate(ApplicationUser guest);

    @Query("SELECT c.booking.id FROM CheckIn c")
    List<Long> findAllBookingIds();

    Optional<List<CheckIn>> findCheckInByGuestOrderByDateDesc(ApplicationUser guest);
}
