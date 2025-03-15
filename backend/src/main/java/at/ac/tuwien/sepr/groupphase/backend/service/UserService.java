package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserLoginDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UserService extends UserDetailsService {

    /**
     * Find a user in the context of Spring Security based on the email address.
     * <br>
     * For more information have a look at this tutorial:
     * https://www.baeldung.com/spring-security-authentication-with-a-database
     *
     * @param email the email address
     * @return a Spring Security user
     * @throws UsernameNotFoundException is thrown if the specified user does not exists
     */
    @Override
    UserDetails loadUserByUsername(String email) throws UsernameNotFoundException;

    /**
     * Find an application user based on the email address.
     *
     * @param email the email address
     * @return a application user
     */
    ApplicationUser findApplicationUserByEmail(String email);

    /**
     * Log in a user.
     *
     * @param userLoginDto login credentials
     * @return the JWT, if successful
     * @throws org.springframework.security.authentication.BadCredentialsException if credentials are bad
     */
    String login(UserLoginDto userLoginDto) throws BadCredentialsException;


    /**
     * Retrieves the email address of the currently logged-in user from the SecurityContext.
     *
     * <p>Extracts the {@code Authentication} object from the {@link SecurityContextHolder},
     * verifies its validity, and retrieves the principal object. Depending on the type of the principal,
     * it returns the email address as a {@code String}.
     *
     * <p>The principal must be an instance of {@link org.springframework.security.core.userdetails.User}.
     *
     * @return the email address of the currently logged-in user
     * @throws AuthenticationCredentialsNotFoundException if the authentication or principal is not found
     *         in the SecurityContext, or if the principal type is unexpected
     */
    String getLoggedInUserEmail();

    /**
     * Retrieves the {@link ApplicationUser} of the currently logged-in user from the SecurityContext.
     *
     * <p>Extracts the {@code Authentication} object from the {@link SecurityContextHolder},
     * verifies its validity, and retrieves the principal object. Depending on the type of the principal,
     * it fetches and returns the corresponding {@link ApplicationUser} entity from the database.
     *
     * <p>The principal must be an instance of {@link org.springframework.security.core.userdetails.User},
     * and the username returned by the principal is assumed to be the user's email address.
     *
     * @return the {@link ApplicationUser} of the currently logged-in user
     * @throws AuthenticationCredentialsNotFoundException if the authentication or principal is not found
     *         in the SecurityContext, or if the principal type is unexpected
     * @throws UsernameNotFoundException if no user is found in the database with the email address
     */
    ApplicationUser getLoggedInUser();

    boolean isAdminOrThisUser(String email);
}
