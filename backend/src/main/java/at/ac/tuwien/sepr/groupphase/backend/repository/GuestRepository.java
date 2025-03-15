package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.Guest;
import at.ac.tuwien.sepr.groupphase.backend.enums.Nationality;
import jakarta.validation.constraints.Email;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link Guest} entities.
 */
@Repository
public interface GuestRepository extends JpaRepository<Guest, Long> {

    /**
     * Finds a guest by their passport number and nationality.
     *
     * @param passportNumber the passport number of the guest
     * @param nationality    the nationality of the guest
     * @return an {@link Optional} containing the found {@link Guest}, or empty if not found
     */
    Optional<Guest> findByPassportNumberAndNationality(String passportNumber, Nationality nationality);

    /**
     * Checks if a guest exists with the given passport number and nationality.
     *
     * @param passportNumber the passport number to check
     * @param nationality    the nationality to check
     * @return {@code true} if a guest with the passport number and nationality exists, {@code false} otherwise
     */
    boolean existsByPassportNumberAndNationality(String passportNumber, Nationality nationality);

    /**
     * Finds all guests with the specified last name.
     *
     * @param lastName the last name of the guests
     * @return a list of {@link Guest} entities with the specified last name
     */
    List<Guest> findByLastName(String lastName);

    /**
     * Checks if a guest exists by its email.
     *
     * @param email of the guest
     * @return true or false, based on finding the user
     */
    boolean existsByEmail(@Email String email);

    /**
     * Finds a guest by their email.
     *
     * @param email the email of the guest
     * @return an {@link Optional} containing the found {@link Guest}, or empty if not found
     */
    Optional<Guest> findByEmail(@Email String email);

    /**
     * Deletes a guest by their email.
     *
     * @param email the email of the guest
     */
    void deleteByEmail(@Email String email);

    @Query("SELECT g FROM Guest g WHERE "
        + "(:firstName IS NULL OR LOWER(g.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))) AND "
        + "(:lastName IS NULL OR LOWER(g.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))) AND "
        + "(:email IS NULL OR LOWER(g.email) LIKE LOWER(CONCAT('%', :email, '%')))")
    Page<Guest> findGuestsByCriteria(@Param("firstName") String firstName,
                                     @Param("lastName") String lastName,
                                     @Param("email") String email,
                                     Pageable pageable);
}
