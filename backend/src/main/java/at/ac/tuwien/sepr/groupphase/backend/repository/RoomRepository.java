package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    /**
     * Find a room by its id.
     *
     * @param id the id of the room
     * @return the room with the given id
     */
    Room findRoomById(Long id);

    /**
     * Find all rooms ordered by price ascending.
     *
     * @return ordered list of all rooms
     */
    Page<Room> findAllByOrderByPriceAsc(Pageable pageable);

    /**
     * Find all rooms ordered by price descending.
     *
     * @return list of all rooms that fit the criteria and do not have a booking at the given time
     */
    @Query("SELECT r FROM Room r WHERE r.id NOT IN (SELECT b.room.id FROM Booking b WHERE b.startDate <= :endDate AND b.endDate >= :startDate AND b.status IN ("
        + "at.ac.tuwien.sepr.groupphase.backend.enums.BookingStatus.PENDING,"
        + "at.ac.tuwien.sepr.groupphase.backend.enums.BookingStatus.ACTIVE,"
        + "at.ac.tuwien.sepr.groupphase.backend.enums.BookingStatus.COMPLETED)) "
        + "AND (:minPrice IS NULL OR r.price >= :minPrice) AND (:maxPrice IS NULL OR r.price <= :maxPrice) AND r.capacity >= :capacity ORDER BY r.price DESC")
    Page<Room> findRoomsByCriteria(@Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate,
                                   @Param("maxPrice") Double maxPrice,
                                   @Param("minPrice") Double minPrice,
                                   @Param("capacity") Integer capacity,
                                   Pageable pageable);

    /**
     * Find all rooms ordered by cleaningTimeTo ascending and then by lastCleanedAt ascending.
     *
     * @return ordered list of all rooms
     */
    @Query("SELECT r FROM Room r ORDER BY CASE WHEN r.cleaningTimeTo IS NOT NULL THEN 0 ELSE 1 END, r.cleaningTimeTo ASC, r.lastCleanedAt ASC")
    Page<Room> findAllRoomsCleaning(Pageable pageable);


    /**
     * Find rooms by admin criteria.
     *
     * @return list of rooms matching the criteria
     */
    @Query("SELECT r FROM Room r WHERE "
        + "(:name IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND "
        + "(:minPrice IS NULL OR r.price >= :minPrice) AND "
        + "(:maxPrice IS NULL OR r.price <= :maxPrice) AND "
        + "(:minCapacity IS NULL OR r.capacity >= :minCapacity) AND "
        + "(:maxCapacity IS NULL OR r.capacity <= :maxCapacity) AND "
        + "(:description IS NULL OR LOWER(r.description) LIKE LOWER(CONCAT('%', :description, '%')))")
    Page<Room> findRoomsByAdminCriteria(@Param("name") String name,
                                        @Param("minPrice") Double minPrice,
                                        @Param("maxPrice") Double maxPrice,
                                        @Param("minCapacity") Integer minCapacity,
                                        @Param("maxCapacity") Integer maxCapacity,
                                        @Param("description") String description,
                                        Pageable pageable);


    Page<Room> findRoomsByNameContainingIgnoreCase(String name, Pageable pageable);
}
