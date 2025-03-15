package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.GuestCreateUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.GuestDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.GuestListDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.GuestSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.GuestSignupDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.SimpleGuestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.GuestMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.Booking;
import at.ac.tuwien.sepr.groupphase.backend.entity.Guest;
import at.ac.tuwien.sepr.groupphase.backend.entity.GuestActivityCategory;
import at.ac.tuwien.sepr.groupphase.backend.enums.Gender;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.BookingRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.GuestActivityCategoryRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.GuestRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.GuestService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.validator.GuestValidator;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class SimpleGuestService implements GuestService {

    private final GuestRepository guestRepository;
    private final GuestActivityCategoryRepository guestActivityCategoryRepository;
    private final GuestMapper guestMapper;
    private final GuestValidator guestValidator;
    private final PasswordEncoder passwordEncoder;
    private final BookingRepository bookingRepository;

    public SimpleGuestService(GuestRepository guestRepository,
                              GuestActivityCategoryRepository guestActivityCategoryRepository,
                              GuestMapper guestMapper,
                              GuestValidator guestValidator,
                              PasswordEncoder passwordEncoder,
                              BookingRepository bookingRepository) {
        this.guestRepository = guestRepository;
        this.guestActivityCategoryRepository = guestActivityCategoryRepository;
        this.guestMapper = guestMapper;
        this.guestValidator = guestValidator;
        this.passwordEncoder = passwordEncoder;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    @Override
    public SimpleGuestDto signup(GuestSignupDto guestSignupDto) throws ValidationException, ConflictException {
        log.debug("Create new guest account {}", guestSignupDto);
        guestValidator.validateForSignup(guestSignupDto);

        if (guestRepository.existsByEmail(guestSignupDto.email())) {
            throw new ConflictException("Email conflict", List.of("Email already exists."));
        }

        Guest guest = guestMapper.guestSignupDtoToGuest(guestSignupDto);
        guest.setPassword(passwordEncoder.encode(guestSignupDto.password()));
        GuestActivityCategory guestActivityCategory = new GuestActivityCategory();
        guestActivityCategory.setGuest(guest);
        guestActivityCategory.setEducation(0.0);
        guestActivityCategory.setMusic(0.0);
        guestActivityCategory.setFitness(0.0);
        guestActivityCategory.setNature(0.0);
        guestActivityCategory.setCooking(0.0);
        guestActivityCategory.setTeamwork(0.0);
        guestActivityCategory.setCreativity(0.0);
        guestActivityCategory.setWellness(0.0);
        guestActivityCategory.setRecreation(0.0);
        guestActivityCategory.setSports(0.0);
        guestActivityCategory.setKids(0.0);
        guestActivityCategory.setWorkshop(0.0);
        guest.setGuestActivityCategory(guestActivityCategory);
        Guest savedGuest = guestRepository.save(guest);
        log.info("Created new guest account with ID {}", savedGuest.getId());


        //guestActivityCategoryRepository.save(guestActivityCategory);

        return guestMapper.guestToSimpleGuestDto(savedGuest);
    }


    @Override
    public Page<GuestListDto> findAll(Pageable pageable) {
        log.debug("Find all guests");

        Page<Guest> guests = guestRepository.findAll(pageable);

        return guests.map(guestMapper::guestToGuestListDto);
    }

    @Override
    public GuestDetailDto update(String email, GuestCreateUpdateDto guestDto) throws ValidationException, NotFoundException, IllegalArgumentException {
        log.debug("Update guest account with email {}", email);

        if (email == null) {
            throw new IllegalArgumentException("Email cannot be null");
        }

        Guest existingGuest = guestRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException("Guest with email " + email + " not found"));

        if (guestDto.firstName() != null) {
            existingGuest.setFirstName(guestDto.firstName());
        }
        if (guestDto.lastName() != null) {
            existingGuest.setLastName(guestDto.lastName());
        }
        if (guestDto.email() != null) {
            existingGuest.setEmail(guestDto.email());
        }
        if (guestDto.dateOfBirth() != null) {
            existingGuest.setDateOfBirth(guestDto.dateOfBirth());
        }
        if (guestDto.placeOfBirth() != null) {
            existingGuest.setPlaceOfBirth(guestDto.placeOfBirth());
        }
        if (guestDto.gender() != null) {
            existingGuest.setGender(Gender.valueOf(guestDto.gender()));
        }
        if (guestDto.passportNumber() != null) {
            existingGuest.setPassportNumber(guestDto.passportNumber());
        }
        if (guestDto.phoneNumber() != null) {
            existingGuest.setPhoneNumber(guestDto.phoneNumber());
        }
        if (guestDto.address() != null) {
            existingGuest.setAddress(guestDto.address());
        }
        if (guestDto.password() != null) {
            existingGuest.setPassword(passwordEncoder.encode(guestDto.password()));
        }

        Guest updatedGuest = guestRepository.save(existingGuest);
        log.info("Guest with email {} updated successfully", email);

        return guestMapper.guestToGuestDetailDto(updatedGuest);
    }

    @Override
    public GuestDetailDto create(GuestCreateUpdateDto guestDto) throws ValidationException, ConflictException {
        log.debug("Create guest: {}", guestDto);
        if (guestRepository.existsByEmail(guestDto.email())) {
            throw new ConflictException("Email conflict", List.of("Email already exists."));
        }

        guestValidator.validateForCreateOrUpdate(guestDto);

        Guest guest = guestMapper.guestCreateUpdateDtoToGuest(guestDto);

        guest.setPassword(passwordEncoder.encode(guestDto.password()));

        Guest savedGuest = guestRepository.save(guest);
        return guestMapper.guestToGuestDetailDto(savedGuest);
    }


    @Override
    public GuestDetailDto findByEmail(String email) throws ValidationException {
        Guest guest = guestRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException("Guest with email " + email + " not found"));

        return guestMapper.guestToGuestDetailDto(guest);
    }

    @Override
    public void deleteByEmail(String email) throws ValidationException {
        log.debug("Delete guest with email {}", email);

        Guest guest = guestRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException("Guest with email " + email + " not found"));
        List<Booking> guestBookings = bookingRepository.findByUserId(guest.getId());
        guestValidator.validateForDelete(guest, guestBookings);

        guestRepository.deleteByEmail(email);

    }

    @Override
    public Page<GuestListDto> search(GuestSearchDto guestSearchDto, Pageable pageable) {
        log.debug("Search for guests with criteria: {}", guestSearchDto);

        Page<Guest> guests = guestRepository.findGuestsByCriteria(
            guestSearchDto.firstName(),
            guestSearchDto.lastName(),
            guestSearchDto.email(),
            pageable);

        return guests.map(guestMapper::guestToGuestListDto);
    }
}
