package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.GuestCreateUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.GuestDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.GuestListDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.GuestSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.GuestSignupDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.SimpleGuestDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.entity.Guest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GuestService {
    /**
     * Creates a new guest account based on the provided signup information.
     *
     * <p>Maps the {@link GuestSignupDto} to a {@link Guest} entity, saves the entity to the repository,
     * and returns a {@link SimpleGuestDto} representing the newly created guest account. The password is
     * encrypted before storing.</p>
     *
     * @param guestSignupDto the {@link GuestSignupDto} containing the guest's signup information
     * @return a {@link SimpleGuestDto} object representing the created guest account
     * @throws ValidationException if validation fails for the guest data
     * @throws ConflictException if a guest with the same email already exists
     */
    SimpleGuestDto signup(GuestSignupDto guestSignupDto) throws ValidationException, ConflictException;

    /**
     * Retrieves all guests from the repository.
     *
     * @return a list of all {@link GuestListDto}
     */
    Page<GuestListDto> findAll(Pageable pageable);

    /**
     * Updates the guest account with the provided email address.
     *
     * @param email the email address of the guest account to update
     * @param guestDto the {@link GuestCreateUpdateDto} containing the updated guest information
     * @return a {@link GuestDetailDto} object representing the updated guest account
     * @throws ValidationException if validation fails for the guest data
     */
    GuestDetailDto update(String email, GuestCreateUpdateDto guestDto) throws ValidationException;

    /**
     * Creates a new guest account based on the provided information.
     *
     * @param guestDto the {@link GuestCreateUpdateDto} containing the guest information
     * @return a {@link GuestDetailDto} object representing the created guest account
     * @throws ValidationException if validation fails for the guest data
     */
    GuestDetailDto create(GuestCreateUpdateDto guestDto) throws ValidationException, ConflictException;

    /**
     * Retrieves the guest account with the provided email address.
     *
     * @param email the email address of the guest account to retrieve
     * @return a {@link GuestDetailDto} object representing the guest account
     * @throws ValidationException if the guest account with the provided email address does not exist
     */
    GuestDetailDto findByEmail(String email) throws ValidationException;

    /**
     * Deletes the guest account with the provided email address.
     *
     * @param email the email address of the guest account to delete
     * @throws ValidationException if the guest account with the provided email address does not exist
     */
    void deleteByEmail(String email) throws ValidationException;

    /**
     * Searches for guests based on the criteria specified in the {@link GuestSearchDto}.
     *
     * @param guestSearchDto the {@link GuestSearchDto} object containing the search criteria
     * @return a list of {@link GuestListDto} objects matching the search criteria
     */
    Page<GuestListDto> search(GuestSearchDto guestSearchDto, Pageable pageable);
}
