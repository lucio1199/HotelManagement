package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Booking;
import at.ac.tuwien.sepr.groupphase.backend.entity.CheckIn;
import at.ac.tuwien.sepr.groupphase.backend.entity.CheckOut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CheckOutRepository extends JpaRepository<CheckOut, Long> {

    Optional<List<CheckOut>> findCheckOutByGuest(ApplicationUser guest);

    List<CheckOut> findCheckOutByBooking(Booking booking);

    List<CheckOut> findCheckOutByBookingAndGuest(Booking booking, ApplicationUser guest);
}
