package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.GuestActivityCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GuestActivityCategoryRepository extends JpaRepository<GuestActivityCategory, Long> {

    /**
     * Finds a GuestActivityCategory by guests ID.
     *
     * @param id the ID of the guest
     * @return the GuestActivityCategory with the given ID
     */
    GuestActivityCategory findGuestActivityCategoryByGuestId(Long id);
}

