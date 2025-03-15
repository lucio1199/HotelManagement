package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.Lock;
import at.ac.tuwien.sepr.groupphase.backend.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LockRepository extends JpaRepository<Lock, Long> {

    /**
     * Find a lock by its id.
     *
     * @param id the id of the lock
     * @return the lock with the given id
     */
    Lock findLockById(Long id);

    /**
     * Find a lock by its room, excluding null results.
     *
     * @param room the room of the lock
     * @return the lock with the given room, or an empty Optional if not found
     */
    @Query("SELECT l FROM Lock l WHERE l.room = :room AND l IS NOT NULL")
    Optional<Lock> findLockByRoom(@Param("room") Room room);

    /**
     * Find a lock by its smart lock id, excluding null results.
     *
     * @param smartLockId the id of the smart lock
     * @return the lock with the given smart lock id, or an empty Optional if not found
     */
    @Query("SELECT l FROM Lock l WHERE l.smartLockId = :smartLockId AND l IS NOT NULL")
    Optional<Lock> findLockBySmartLockId(@Param("smartLockId") Long smartLockId);
}
