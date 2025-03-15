package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserLoginDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import org.springframework.security.core.userdetails.User;
import at.ac.tuwien.sepr.groupphase.backend.enums.RoleType;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ApplicationUserRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtTokenizer;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.List;

@Service
public class CustomUserDetailService implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ApplicationUserRepository applicationUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenizer jwtTokenizer;

    @Autowired
    public CustomUserDetailService(ApplicationUserRepository applicationUserRepository, PasswordEncoder passwordEncoder, JwtTokenizer jwtTokenizer) {
        this.applicationUserRepository = applicationUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenizer = jwtTokenizer;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        LOGGER.debug("Load all user by email");
        try {
            ApplicationUser applicationUser = findApplicationUserByEmail(email);

            List<GrantedAuthority> grantedAuthorities;
            if (applicationUser.hasAuthority(RoleType.ROLE_ADMIN)) {
                grantedAuthorities = AuthorityUtils.createAuthorityList("ROLE_ADMIN", "ROLE_GUEST", "ROLE_CLEANING_STAFF", "ROLE_RECEPTIONIST");
            } else if (applicationUser.hasAuthority(RoleType.ROLE_CLEANING_STAFF)) {
                grantedAuthorities = AuthorityUtils.createAuthorityList("ROLE_CLEANING_STAFF");
            } else if (applicationUser.hasAuthority(RoleType.ROLE_RECEPTIONIST)) {
                grantedAuthorities = AuthorityUtils.createAuthorityList("ROLE_RECEPTIONIST");
            } else {
                grantedAuthorities = AuthorityUtils.createAuthorityList("ROLE_GUEST");
            }

            return new User(applicationUser.getEmail(), applicationUser.getPassword(), grantedAuthorities);
        } catch (NotFoundException e) {
            throw new UsernameNotFoundException(e.getMessage(), e);
        }
    }

    @Override
    public ApplicationUser findApplicationUserByEmail(String email) {
        LOGGER.debug("Find application user by email");
        return applicationUserRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException(String.format("Could not find the user with the email address %s", email)));
    }

    @Override
    public String login(UserLoginDto userLoginDto) throws BadCredentialsException {
        UserDetails userDetails = loadUserByUsername(userLoginDto.email());
        if (userDetails != null
            && userDetails.isAccountNonExpired()
            && userDetails.isAccountNonLocked()
            && userDetails.isCredentialsNonExpired()
            && passwordEncoder.matches(userLoginDto.password(), userDetails.getPassword())) {
            List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
            return jwtTokenizer.getAuthToken(userDetails.getUsername(), roles);
        }
        throw new BadCredentialsException("Invalid password");
    }


    @Override
    public String getLoggedInUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            throw new AuthenticationCredentialsNotFoundException("No authentication or principal found in SecurityContext.");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof User userDetails) {
            return userDetails.getUsername();
        } else {
            throw new AuthenticationCredentialsNotFoundException(
                "Unexpected principal type: " + principal.getClass().getName()
            );
        }
    }

    @Override
    public ApplicationUser getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            throw new AuthenticationCredentialsNotFoundException("No authentication or principal found in SecurityContext.");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof User userDetails) {
            String email = userDetails.getUsername();
            return applicationUserRepository.findByEmail(email).orElseThrow(() ->
                new UsernameNotFoundException("User not found with email: " + email)
            );
        } else {
            throw new AuthenticationCredentialsNotFoundException(
                "Unexpected principal type: " + principal.getClass().getName()
            );
        }
    }

    @Override
    public boolean isAdminOrThisUser(String email) {
        ApplicationUser loggedInUser = getLoggedInUser();
        return loggedInUser.getEmail().equals(email) || loggedInUser.hasAuthority(RoleType.ROLE_ADMIN);
    }
}
