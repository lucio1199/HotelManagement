package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.ActivityBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityBookingRepository extends JpaRepository<ActivityBooking, Long> {

    /**
     * Finds all activity bookings associated with a specific ApplicationUser ID.
     *
     * @param id the ID of the ApplicationUser
     * @return a list of activity bookings associated with the specified ApplicationUser ID
     */
    Optional<ActivityBooking> findActivityBookingById(Long id);

    /**
     * Finds all active activity bookings associated with a specific ApplicationUser ID.
     *
     * @param userId the ID of the ApplicationUser
     * @return a list of active activity bookings associated with the specified ApplicationUser ID
     */
    @Query("SELECT b FROM ActivityBooking b WHERE b.user.id = :userId AND b.status = 'ACTIVE'")
    List<ActivityBooking> findActiveBookingsByUserId(@Param("userId") Long userId);

    /**
     * Finds all activity bookings associated with a specific ApplicationUser ID.
     *
     * @param activityId the ID of the ApplicationUser
     * @return a list of activity bookings associated with the specified ApplicationUser ID
     */
    List<ActivityBooking> findByActivityId(Long activityId);
}
