package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Booking;
import at.ac.tuwien.sepr.groupphase.backend.entity.InviteToRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InviteToRoomRepository extends JpaRepository<InviteToRoom, Long> {

    Optional<List<InviteToRoom>> findInviteToRoomByGuest(ApplicationUser guest);

    Optional<InviteToRoom> findInviteToRoomByGuestAndBooking(ApplicationUser guest, Booking booking);
}
