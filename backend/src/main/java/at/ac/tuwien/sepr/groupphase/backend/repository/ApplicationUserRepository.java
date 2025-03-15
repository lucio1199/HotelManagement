package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for {@link ApplicationUser} entities.
 */
@Repository
public interface ApplicationUserRepository extends JpaRepository<ApplicationUser, Long> {

    /**
     * Finds an application user by their email.
     *
     * @param email the email of the user
     * @return an {@link Optional} containing the found {@link ApplicationUser}, or empty if not found
     */
    Optional<ApplicationUser> findByEmail(String email);

    /**
     * Checks if an application user exists with the given email.
     *
     * @param email the email to check
     * @return {@code true} if an application user with the email exists, {@code false} otherwise
     */
    boolean existsByEmail(String email);
}