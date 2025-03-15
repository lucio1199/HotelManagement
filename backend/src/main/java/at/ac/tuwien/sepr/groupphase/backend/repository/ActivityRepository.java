package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.Activity;
import at.ac.tuwien.sepr.groupphase.backend.entity.ActivitySlot;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    /**
     * Finds an activity by its ID.
     *
     * @param id the ID of the activity
     * @return the activity with the given ID
     */
    Optional<Activity> findActivityById(Long id) throws NotFoundException;

    /**
     * Retrieves all activities ordered by price in ascending order.
     *
     * @return a list of all activities ordered by price ascending
     */
    Page<Activity> findAllByOrderByPriceAsc(Pageable pageable);


    /**
     * Retrieves all activities ordered by price in ascending order.
     *
     * @return a list of all activities ordered by price ascending
     */
    @Query("SELECT s FROM ActivitySlot s "
        + "WHERE s.activity.id = :activityId "
        + "AND (s.date > :currentDate OR (s.date = :currentDate AND s.startTime > :currentTime))")
    Page<ActivitySlot> findTimeslotsByActivityId(
        @Param("activityId") Long activityId,
        @Param("currentDate") LocalDate currentDate,
        @Param("currentTime") LocalTime currentTime,
        Pageable pageable);


    /**
     * Retrieves all activities ordered by price in ascending order.
     *
     * @param activityId the ID of the activity
     * @param date the date of the activity
     * @param participants the number of participants
     * @param pageable the pageable object
     * @return a list of all activities ordered by price ascending
     */
    @Query("SELECT s FROM ActivitySlot s WHERE s.activity.id = :activityId "
        + "AND (s.date > :currentDate OR (s.date = :currentDate AND s.startTime > :currentTime)) "
        + "AND (:date IS NULL OR s.date = :date) "
        + "AND (:participants IS NULL OR (s.capacity - s.occupied) >= :participants)")
    Page<ActivitySlot> findFilteredTimeslots(
        @Param("activityId") Long activityId,
        @Param("date") LocalDate date,
        @Param("participants") Integer participants,
        @Param("currentDate") LocalDate currentDate,
        @Param("currentTime") LocalTime currentTime,
        Pageable pageable);

    /**
     * Retrieves all activities ordered by price in ascending order.
     *
     * @param id the ID of the activity
     * @return a list of all activities ordered by price ascending
     */
    @Query("SELECT s FROM ActivitySlot s WHERE s.id = :id")
    Optional<ActivitySlot> findActivitySlotById(@Param("id") Long id);

    /**
     * Retrieves all activities ordered by price in ascending order.
     *
     * @param name the name of the activity
     * @param date the date of the activity
     * @param guestCount the number of participants
     * @param minPrice the minimum price
     * @param maxPrice the maximum price
     * @param pageable the pageable object
     * @return a list of all activities ordered by price ascending
     */
    @Query("SELECT DISTINCT a FROM Activity a "
        + "JOIN a.activityTimeslots ats "
        + "WHERE (:name IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%'))) "
        + "AND (:date IS NULL OR ats.date = :date) "
        + "AND (ats.date > :currentDate OR (ats.date = :currentDate AND ats.startTime > :currentTime)) "
        + "AND (:guestCount IS NULL OR (ats.capacity - ats.occupied) >= :guestCount) "
        + "AND (:minPrice IS NULL OR a.price >= :minPrice) "
        + "AND (:maxPrice IS NULL OR a.price <= :maxPrice)")
    Page<Activity> search(
        @Param("name") String name,
        @Param("date") LocalDate date,
        @Param("guestCount") Integer guestCount,
        @Param("minPrice") Double minPrice,
        @Param("maxPrice") Double maxPrice,
        @Param("currentDate") LocalDate currentDate,
        @Param("currentTime") LocalTime currentTime,
        Pageable pageable
    );

    Activity getActivityById(Long id);
}
