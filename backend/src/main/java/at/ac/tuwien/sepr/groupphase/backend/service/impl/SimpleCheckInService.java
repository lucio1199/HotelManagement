package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.AddToRoomDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CheckInDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CheckInStatusDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CheckOutDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DetailedBookingDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DetailedRoomDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.GuestListDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InviteToRoomDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.OccupancyDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.BookingMapper;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.CheckInMapper;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.GuestMapper;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.RoomMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Booking;
import at.ac.tuwien.sepr.groupphase.backend.entity.CheckIn;
import at.ac.tuwien.sepr.groupphase.backend.entity.CheckOut;
import at.ac.tuwien.sepr.groupphase.backend.entity.Guest;
import at.ac.tuwien.sepr.groupphase.backend.entity.InviteToRoom;
import at.ac.tuwien.sepr.groupphase.backend.enums.RoleType;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ApplicationUserRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.BookingRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.CheckInRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.CheckOutRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.GuestRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.InviteToRoomRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoomRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.BookingService;
import at.ac.tuwien.sepr.groupphase.backend.service.CheckInService;
import at.ac.tuwien.sepr.groupphase.backend.service.MailService;
import at.ac.tuwien.sepr.groupphase.backend.service.validator.CheckInValidator;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.hibernate.annotations.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SimpleCheckInService implements CheckInService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final CheckInRepository checkInRepository;
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;
    private final ApplicationUserRepository userRepository;
    private final GuestRepository guestRepository;
    private final CheckInValidator checkInValidator;
    private final CheckInMapper checkInMapper;
    private final BookingService bookingService;
    private final CheckOutRepository checkOutRepository;
    private final GuestMapper guestMapper;
    private final InviteToRoomRepository inviteToRoomRepository;
    private final MailService mailService;

    public SimpleCheckInService(CheckInRepository checkInRepository, BookingRepository bookingRepository,
                                BookingMapper bookingMapper, RoomRepository roomRepository, RoomMapper roomMapper,
                                ApplicationUserRepository userRepository, CheckInValidator checkInValidator,
                                CheckInMapper checkInMapper, GuestRepository guestRepository, BookingService bookingService,
                                CheckOutRepository checkOutRepository, GuestMapper guestMapper, InviteToRoomRepository inviteToRoomRepository,
                                MailService mailService) {
        this.checkInRepository = checkInRepository;
        this.bookingRepository = bookingRepository;
        this.bookingMapper = bookingMapper;
        this.roomRepository = roomRepository;
        this.roomMapper = roomMapper;
        this.userRepository = userRepository;
        this.checkInValidator = checkInValidator;
        this.checkInMapper = checkInMapper;
        this.guestRepository = guestRepository;
        this.bookingService = bookingService;
        this.checkOutRepository = checkOutRepository;
        this.guestMapper = guestMapper;
        this.inviteToRoomRepository = inviteToRoomRepository;
        this.mailService = mailService;
    }

    @Override
    @Transactional
    public void checkIn(CheckInDto checkInDto, MultipartFile passport, String email) throws NotFoundException, ValidationException, ConflictException, IOException {
        LOGGER.debug("Check in dto: {}, email: {}", checkInDto, email);

        checkInValidator.validateGuestInformation(checkInDto, passport);
        Guest guest = guestRepository.findByEmail(email).orElseThrow(() ->
            new NotFoundException("Guest with email " + email + " not found"));
        guest.setFirstName(checkInDto.firstName());
        guest.setLastName(checkInDto.lastName());
        guest.setDateOfBirth(LocalDate.parse(checkInDto.dateOfBirth().toString()));
        guest.setPlaceOfBirth(checkInDto.placeOfBirth());
        guest.setGender(checkInDto.gender());
        guest.setNationality(checkInDto.nationality());
        guest.setAddress(checkInDto.address());
        guest.setPassportNumber(checkInDto.passportNumber());
        guest.setPhoneNumber(checkInDto.phoneNumber());

        Booking booking = bookingRepository.findBookingById(checkInDto.bookingId()).orElseThrow(() ->
            new NotFoundException("Booking with id " + checkInDto.bookingId() + " not found"));
        DetailedBookingDto bookingDto = bookingMapper.bookingToDetailedBookingDto(booking);
        int countCheckedIn = checkInRepository.findCheckInByBooking(booking).size();
        int countCheckedOut = checkOutRepository.findCheckOutByBooking(booking).size();
        DetailedRoomDto room = roomMapper.roomToDetailedRoomDto(roomRepository.findRoomById(bookingDto.roomId()), null);
        ApplicationUser user = userRepository.findByEmail(email).orElseThrow(() ->
            new NotFoundException("User with email " + email + " not found"));
        List<InviteToRoom> invites = inviteToRoomRepository.findInviteToRoomByGuest(guest).orElse(null);
        InviteToRoom inviteToRoom = null;
        if (invites != null) {
            for (InviteToRoom invite : invites) {
                if (Objects.equals(invite.getBookingId(), checkInDto.bookingId())) {
                    inviteToRoom = invite;
                }
            }
            checkInValidator.validateForCheckIn(checkInDto, room, countCheckedIn - countCheckedOut, bookingDto, user, countCheckedOut > 0, inviteToRoom);
        } else {
            checkInValidator.validateForCheckIn(checkInDto, room, countCheckedIn - countCheckedOut, bookingDto, user, countCheckedOut > 0, null);
        }

        try {
            CheckIn checkIn = checkInMapper.checkInDtoToCheckIn(checkInDto, booking, user, passport);
            checkInRepository.save(checkIn);
            if (inviteToRoom != null) {
                inviteToRoomRepository.delete(inviteToRoom);
            }
        } catch (IOException e) {
            LOGGER.error("Error creating check in", e);
            throw new IOException("Failed to check in due to file processing error", e);
        }
    }

    @Override
    @Transactional
    public DetailedRoomDto[] getGuestRooms(String email) throws NotFoundException {
        LOGGER.debug("Get customer rooms for {}", email);

        // Find guest by email or throw an exception
        ApplicationUser guest = userRepository.findByEmail(email).orElseThrow(() ->
            new NotFoundException("User with email " + email + " not found"));

        // Find check-ins for the guest or throw an exception if not found
        List<CheckIn> checkIns = checkInRepository.findCheckInByGuestOrderByDateDesc(guest).orElseThrow(() ->
            new NotFoundException("No check-ins found for user with email " + email));

        // Find only active check-ins
        List<CheckIn> activeCheckIns = new ArrayList<>();
        for (CheckIn checkIn : checkIns) {
            Booking current = checkIn.getBooking();
            if (checkInRepository.findCheckInByBookingAndGuest(current, guest).size() > checkOutRepository.findCheckOutByBookingAndGuest(current, guest).size()) {
                boolean contains = false;
                for (CheckIn checker : activeCheckIns) {
                    if (Objects.equals(checker.getBookingId(), current.getId())) {
                        contains = true;
                    }
                }
                if (!contains) {
                    activeCheckIns.add(checkIn);
                }
            }
        }

        // Sort the active check-ins
        activeCheckIns.sort(Comparator.comparing(CheckIn::getDate));
        List<DetailedRoomDto> activeRooms = activeCheckIns.stream()
            .map(checkIn -> roomMapper.roomToDetailedRoomDto(checkIn.getRoom(), null))
            .distinct()
            .toList();

        if (activeRooms.isEmpty()) {
            throw new NotFoundException("No active rooms found for user with email " + email);
        }

        return activeRooms.toArray(new DetailedRoomDto[0]);
    }

    @Override
    @Transactional
    public List<Long> getAllBookingIds() {
        LOGGER.debug("Get booking ids from CheckIn");
        return checkInRepository.findAllBookingIds();
    }

    @Override
    @Transactional
    public DetailedBookingDto findBookingById(Long bookingId, Long id) throws NotFoundException {
        DetailedBookingDto bookingDto = this.bookingService.findBookingById(bookingId);
        Booking booking = this.bookingRepository.findBookingById(bookingId).orElse(null);
        ApplicationUser user = userRepository.findById(id).orElse(null);
        if ((user != null) && (user.hasAuthority(RoleType.ROLE_RECEPTIONIST) || user.hasAuthority(RoleType.ROLE_ADMIN))) {
            return bookingDto;
        }
        InviteToRoom invitation = inviteToRoomRepository.findInviteToRoomByGuestAndBooking(user, booking).orElse(null);
        if (!(Objects.equals(bookingDto.userId(), id) || invitation != null)) {
            throw new NotFoundException("Check in not found!");
        }
        return bookingDto;
    }

    @Override
    public CheckInStatusDto[] getCheckedInStatus(String email) {
        LOGGER.debug("Get checked-in status for {}", email);

        ApplicationUser guest = userRepository.findByEmail(email).orElseThrow(() ->
            new NotFoundException("User with email " + email + " not found"));

        List<CheckIn> checkIns = checkInRepository.findCheckInByGuest(guest).orElse(List.of());
        List<CheckOut> checkOuts = checkOutRepository.findCheckOutByGuest(guest).orElse(List.of());

        Set<Long> checkedOutBookingIds = checkOuts.stream()
            .map(CheckOut::getBookingId)
            .collect(Collectors.toSet());

        List<CheckIn> activeCheckIns = new ArrayList<>();
        for (CheckIn checkIn : checkIns) {
            Booking current = checkIn.getBooking();
            List<CheckIn> checkInsBooking = checkInRepository.findCheckInByBookingAndGuest(current, guest);
            List<CheckOut> checkOutsBooking = checkOutRepository.findCheckOutByBookingAndGuest(current, guest);
            if (!checkInsBooking.isEmpty() && checkOutsBooking.isEmpty()) {
                if (!activeCheckIns.contains(checkIn)) {
                    activeCheckIns.add(checkIn);
                }
            } else if (!checkInsBooking.isEmpty()) {
                if (!activeCheckIns.contains(checkIn)) {
                    CheckIn fakeCheckIn = CheckIn.CheckInBuilder.aCheckIn()  // for filtering out already checked-out of bookings
                        .withBooking(checkIn.getBooking())
                        .withDate(checkIn.getDate())
                        .withGuest(null)
                        .build();
                    activeCheckIns.add(fakeCheckIn);
                }
            }
        }

        return checkInMapper.checkInsToCheckInStatusDtos(activeCheckIns).toArray(new CheckInStatusDto[0]);
    }

    @Transactional
    @Override
    public void checkOut(CheckOutDto checkOutDto) throws NotFoundException, ValidationException {
        LOGGER.debug("Check out dto: {}", checkOutDto);
        Booking booking = bookingRepository.findBookingById(checkOutDto.bookingId()).orElseThrow(() ->
            new NotFoundException("Booking with id " + checkOutDto.bookingId() + " not found"));
        checkInValidator.validateForCheckOut(checkOutDto, booking);
        List<CheckIn> checkIns = checkInRepository.findCheckInByBooking(booking);
        for (CheckIn checkIn : checkIns) {
            CheckOut checkOut = new CheckOut(booking, LocalDateTime.now(), checkIn.getGuest());
            checkOutRepository.save(checkOut);
            LOGGER.info("User {} successfully checked out from booking {}", checkIn.getGuest().getEmail(), booking.getId());
        }
    }

    @Override
    public DetailedBookingDto getGuestBooking(Long roomId, String email) throws NotFoundException {
        LOGGER.debug("Get guest booking for email {} and room id {}", email, roomId);

        // Check if the guest has checked-in status
        if (getCheckedInStatus(email).length > 0) {
            ApplicationUser guest = userRepository.findByEmail(email).orElseThrow(() ->
                new NotFoundException("User with email " + email + " not found"));

            // Retrieve check-ins for the guest, ordered by date
            List<CheckIn> checkIns = checkInRepository.findCheckInByGuestOrderByDate(guest).orElseThrow(() ->
                new NotFoundException("Check-in for user with email " + email + " not found"));

            // Ensure the check-ins list is valid
            if (checkIns == null || checkIns.isEmpty()) {
                throw new NotFoundException("Room for user with email " + email + " not found");
            }

            // Filter check-ins by roomId
            CheckIn checkIn = checkIns.stream()
                .filter(ci -> ci.getBooking().getRoom().getId().equals(roomId)) // Ensure CheckIn corresponds to roomId
                .reduce((first, second) -> second)           // Get the most recent CheckIn
                .orElseThrow(() ->
                    new NotFoundException("No check-in found for room id " + roomId + " and email " + email));

            // Find the booking associated with the CheckIn
            Booking booking = bookingRepository.findBookingById(checkIn.getBookingId()).orElseThrow(() ->
                new NotFoundException("Booking with id " + checkIn.getBookingId() + " not found"));

            // Map the booking to a DetailedBookingDto and return
            return bookingMapper.bookingToDetailedBookingDto(booking);
        }

        // If no checked-in status, throw exception
        throw new NotFoundException("Booking for guest " + email + " not found.");
    }

    @Override
    public OccupancyDto getOccupancyStatus(Long id) throws NotFoundException {
        LOGGER.debug("Get occupancy status: {}", id);
        List<Booking> bookings = bookingRepository.findBookingsByRoomIdAndStartDateBetween(id, LocalDate.now(), LocalDate.now());
        for (Booking booking : bookings) {
            CheckInStatusDto[] checkedIn = getCheckedInStatus(booking.getUser().getEmail());
            for (CheckInStatusDto dto : checkedIn) {
                Booking b = bookingRepository.findBookingById(dto.bookingId()).orElse(null);
                if (b != null && Objects.equals(b.getRoom().getId(), id)) {
                    return new OccupancyDto(id, "occupied");
                }
            }
        }
        return new OccupancyDto(id, "not-occupied");
    }

    @Override
    @Transactional
    public void addToRoom(AddToRoomDto addToRoomDto, MultipartFile passport, String ownerEmail) throws NotFoundException, ValidationException, ConflictException, IOException {
        LOGGER.debug("Add to room dto: {}, email: {}", addToRoomDto, ownerEmail);

        CheckInDto checkInDto = new CheckInDto(addToRoomDto.bookingId(), addToRoomDto.firstName(), addToRoomDto.lastName(),
            addToRoomDto.dateOfBirth(), addToRoomDto.placeOfBirth(), addToRoomDto.gender(), addToRoomDto.nationality(),
            addToRoomDto.address(), addToRoomDto.passportNumber(), addToRoomDto.phoneNumber());
        checkInValidator.validateGuestInformationForAddToRoom(checkInDto, passport);
        Guest guest = guestRepository.findByEmail(addToRoomDto.email()).orElseThrow(() ->
            new NotFoundException("Guest with email " + addToRoomDto.email() + " not found"));
        guest.setFirstName(addToRoomDto.firstName());
        guest.setLastName(addToRoomDto.lastName());
        guest.setDateOfBirth(LocalDate.parse(addToRoomDto.dateOfBirth().toString()));
        guest.setPlaceOfBirth(addToRoomDto.placeOfBirth());
        guest.setGender(addToRoomDto.gender());
        guest.setNationality(addToRoomDto.nationality());
        guest.setAddress(addToRoomDto.address());
        guest.setPassportNumber(addToRoomDto.passportNumber());
        guest.setPhoneNumber(addToRoomDto.phoneNumber());

        Booking booking = bookingRepository.findBookingById(addToRoomDto.bookingId()).orElseThrow(() ->
            new NotFoundException("Booking with id " + addToRoomDto.bookingId() + " not found"));
        DetailedBookingDto bookingDto = bookingMapper.bookingToDetailedBookingDto(booking);
        List<CheckIn> checkIns = checkInRepository.findCheckInByBooking(booking);
        if (checkIns == null) {
            throw new NotFoundException("Cannot add guests to a room since you are not checked in.");
        }
        int countCheckedIn = checkIns.size();
        List<CheckOut> checkOuts = checkOutRepository.findCheckOutByBooking(booking);
        int countCheckedOut = 0;
        if (checkOuts != null) {
            countCheckedOut = checkOuts.size();
        }
        DetailedRoomDto room = roomMapper.roomToDetailedRoomDto(roomRepository.findRoomById(bookingDto.roomId()), null);
        ApplicationUser user = userRepository.findByEmail(addToRoomDto.email()).orElseThrow(() ->
            new NotFoundException("User with email " + addToRoomDto.email() + " not found"));
        ApplicationUser owner = userRepository.findByEmail(ownerEmail).orElseThrow(() ->
            new NotFoundException("User with email " + ownerEmail + " not found"));
        checkInValidator.validateForAddToRoom(checkInDto.bookingId(), room, countCheckedIn - countCheckedOut, bookingDto, owner);

        try {
            CheckIn checkIn = checkInMapper.checkInDtoToCheckIn(checkInDto, booking, user, passport);
            checkInRepository.save(checkIn);
        } catch (IOException e) {
            LOGGER.error("Error creating check in", e);
            throw new IOException("Failed to add guest to room due to file processing error", e);
        }
    }

    @Override
    public void inviteToRoom(InviteToRoomDto inviteToRoomDto, String ownerEmail) throws NotFoundException, ValidationException, ConflictException, MessagingException {
        LOGGER.debug("Invite to room dto: {}, email: {}", inviteToRoomDto, ownerEmail);

        if (!Objects.equals(inviteToRoomDto.ownerEmail(), ownerEmail)) {
            throw new ValidationException("You are not the owner of the room.", List.of("You cannot add guests to a room you don't own."));
        }

        Booking booking = bookingRepository.findBookingById(inviteToRoomDto.bookingId()).orElseThrow(() ->
            new NotFoundException("Booking with id " + inviteToRoomDto.bookingId() + " not found"));
        DetailedBookingDto bookingDto = bookingMapper.bookingToDetailedBookingDto(booking);
        List<CheckIn> checkIns = checkInRepository.findCheckInByBooking(booking);
        if (checkIns == null) {
            throw new NotFoundException("Cannot add guests to a room since you are not checked in.");
        }
        int countCheckedIn = checkIns.size();
        List<CheckOut> checkOuts = checkOutRepository.findCheckOutByBooking(booking);
        int countCheckedOut = 0;
        if (checkOuts != null) {
            countCheckedOut = checkOuts.size();
        }
        DetailedRoomDto room = roomMapper.roomToDetailedRoomDto(roomRepository.findRoomById(bookingDto.roomId()), null);
        ApplicationUser user = userRepository.findByEmail(inviteToRoomDto.email()).orElseThrow(() ->
            new NotFoundException("User with email " + inviteToRoomDto.email() + " not found"));
        ApplicationUser owner = userRepository.findByEmail(ownerEmail).orElseThrow(() ->
            new NotFoundException("User with email " + ownerEmail + " not found"));

        InviteToRoom pastInvite = inviteToRoomRepository.findInviteToRoomByGuestAndBooking(user, booking).orElse(null);
        if (pastInvite != null) {
            throw new ConflictException("Cannot invite guest to the room", List.of("The guest is already invited to the room"));
        }

        checkInValidator.validateForAddToRoom(inviteToRoomDto.bookingId(), room, countCheckedIn - countCheckedOut, bookingDto, owner);

        InviteToRoom invite = new InviteToRoom(booking, user);
        inviteToRoomRepository.save(invite);
        mailService.sendAddToRoomEmail(inviteToRoomDto);
    }

    @Override
    @Transactional
    public GuestListDto[] getGuests(Long roomId, String email) throws NotFoundException {
        LOGGER.debug("Get guests for room of email {} and room id {}", email, roomId);

        // Check if the guest has checked-in status
        if (getCheckedInStatus(email).length > 0) {
            ApplicationUser guest = userRepository.findByEmail(email).orElseThrow(() ->
                new NotFoundException("User with email " + email + " not found"));

            // Find the bookings associated with the logged in guest
            List<Booking> bookings = bookingRepository.findByUserId(guest.getId());
            if (bookings == null) {
                throw new NotFoundException("Bookings for guest with id " + guest.getId() + " not found");
            }

            List<CheckIn> checkIns = null;
            for (Booking booking : bookings) {
                if (Objects.equals(booking.getRoom().getId(), roomId)) {
                    // Retrieve check-ins of the invited guests
                    checkIns = checkInRepository.findCheckInByBooking(booking);
                }
            }
            if (checkIns == null || checkIns.size() <= 1) {
                throw new NotFoundException("No extra checked in guests for room found");
            }
            List<GuestListDto> guests = new ArrayList<>();
            for (CheckIn checkIn : checkIns) {
                if (checkIn.getGuest() != guest) {
                    Guest newGuest = guestRepository.findByEmail(checkIn.getGuest().getEmail()).orElse(null);
                    if (newGuest != null) {
                        guests.add(guestMapper.guestToGuestListDto(newGuest));
                    }
                }
            }

            if (!guests.isEmpty()) {
                return guests.toArray(new GuestListDto[0]);
            }
        }
        throw new NotFoundException("No extra checked in guests for room found");
    }

    @Override
    public boolean isOwner(Long roomId, String email) throws NotFoundException {
        LOGGER.debug("Get owner status for room: {}", roomId);
        List<Booking> bookings = bookingRepository.findBookingsByRoomIdAndStartDateBetween(roomId, LocalDate.now(), LocalDate.now());
        if (userRepository.findByEmail(email).isEmpty()) {
            throw new NotFoundException("User with email " + email + " not found.");
        }
        boolean result = false;
        for (Booking booking : bookings) {
            if (Objects.equals(booking.getUser().getEmail(), email)) {
                result = true;
            } else {
                result = false;
            }
        }
        return result;
    }

    @Override
    @Transactional
    public GuestListDto[] getAllGuests(Long bookingId) throws NotFoundException {
        LOGGER.debug("Get all guests for room of booking id {}", bookingId);
        Booking booking = bookingRepository.findBookingById(bookingId).orElse(null);
        if (booking == null) {
            throw new NotFoundException("Booking for id " + bookingId + " not found");
        }

        List<CheckIn> checkIns = checkInRepository.findCheckInByBooking(booking);

        if (checkIns == null || checkIns.isEmpty()) {
            throw new NotFoundException("No checked in guests for room found");
        }
        List<GuestListDto> guests = new ArrayList<>();
        for (CheckIn checkIn : checkIns) {
            guestRepository.findByEmail(checkIn.getGuest().getEmail()).ifPresent(newGuest -> guests.add(guestMapper.guestToGuestListDto(newGuest)));
        }

        if (!guests.isEmpty()) {
            return guests.toArray(new GuestListDto[0]);
        }
        throw new NotFoundException("No checked in guests for room found");
    }

    @Override
    public byte[] getPassportByBookingIdAndEmail(Long bookingId, String email) throws NotFoundException {
        LOGGER.debug("Get passport of guest {} and of booking id {}", email, bookingId);
        ApplicationUser guest = userRepository.findByEmail(email).orElseThrow(() ->
            new NotFoundException("Guest with email " + email + " not found"));
        Booking booking = bookingRepository.findBookingById(bookingId).orElseThrow(() ->
            new NotFoundException("Booking with id " + bookingId + " not found"));
        List<CheckIn> checkIns = checkInRepository.findCheckInByBookingAndGuest(booking, guest);

        if (checkIns == null || checkIns.isEmpty()) {
            throw new NotFoundException("Check-In for booking with id " + bookingId + " and guest with email " + email + " not found");
        }
        if (checkIns.getFirst().getPassport() == null) {
            throw new NotFoundException("Passport for guest with email " + email + " not found");
        }
        return checkIns.getFirst().getPassport();
    }

    @Scheduled(cron = "59 9 * * *")
    private void checkOutJob() {
        LOGGER.debug("CheckOutJob started");
        int countCheckOutsPerformed = performAutoCheckOut();
        LOGGER.debug("CheckOutJob finished, checked-out {} guests.", countCheckOutsPerformed);
    }

    @Override
    public int performAutoCheckOut() {
        int countCheckOutsPerformed = 0;
        List<Booking> bookingsThatRunOut = bookingRepository.findBookingsByEndDate(LocalDate.now()).orElse(null);
        if (bookingsThatRunOut == null) {
            return countCheckOutsPerformed;
        }

        for (Booking booking : bookingsThatRunOut) {
            List<CheckIn> activeCheckIns = checkInRepository.findCheckInByBooking(booking);
            if (activeCheckIns != null && !activeCheckIns.isEmpty()) {
                for (CheckIn checkIn : activeCheckIns) {
                    List<CheckOut> doneCheckOuts = checkOutRepository.findCheckOutByBookingAndGuest(booking, checkIn.getGuest());
                    if (doneCheckOuts == null || doneCheckOuts.isEmpty()) {
                        CheckOut newCheckOut = new CheckOut(booking, LocalDateTime.now(), checkIn.getGuest());
                        checkOutRepository.save(newCheckOut);
                        countCheckOutsPerformed++;
                    }
                }
            }
        }
        return countCheckOutsPerformed;
    }

    @Override
    public void remove(Long bookingId, String email) throws NotFoundException {
        LOGGER.debug("Remove {} from booking with id {}", email, bookingId);
        Booking booking = bookingRepository.findBookingById(bookingId).orElseThrow(() ->
            new NotFoundException("Booking with id " + bookingId + " not found"));
        ApplicationUser guest = userRepository.findByEmail(email).orElseThrow(() ->
            new NotFoundException("Guest with email " + email + " not found"));
        List<CheckIn> checkIns = checkInRepository.findCheckInByBookingAndGuest(booking, guest);
        if (checkIns == null || checkIns.isEmpty()) {
            throw new NotFoundException("No check-ins for guest with email " + email + " found");
        }

        for (CheckIn checkIn : checkIns) {
            checkInRepository.delete(checkIn);
        }
    }
}
