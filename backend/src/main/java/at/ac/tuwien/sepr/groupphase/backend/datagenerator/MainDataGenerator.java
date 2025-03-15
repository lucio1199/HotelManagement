package at.ac.tuwien.sepr.groupphase.backend.datagenerator;

import at.ac.tuwien.sepr.groupphase.backend.repository.CheckInRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.CheckOutRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.InviteToRoomRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.LockRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.PdfRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import java.lang.invoke.MethodHandles;

@Profile("generateData")
@Component
public class MainDataGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final GuestDataGenerator guestDataGenerator;
    private final EmployeeDataGenerator employeeDataGenerator;
    private final RoomDataGenerator roomDataGenerator;
    private final BookingDataGenerator bookingDataGenerator;
    private final CheckInRepository checkInRepository;
    private final CheckOutRepository checkOutRepository;
    private final UiConfigDataGenerator uiConfigDataGenerator;
    private final ActivityDataGenerator activityDataGenerator;
    private final LockRepository lockRepository;
    private final InviteToRoomRepository inviteToRoomRepository;
    private final PdfRepository pdfRepository;

    public MainDataGenerator(
        EmployeeDataGenerator employeeDataGenerator,
        GuestDataGenerator guestDataGenerator,
        RoomDataGenerator roomDataGenerator,
        BookingDataGenerator bookingDataGenerator,
        CheckInRepository checkInRepository,
        CheckOutRepository checkOutRepository,
        UiConfigDataGenerator uiConfigDataGenerator,
        ActivityDataGenerator activityDataGenerator,
        LockRepository lockRepository,
        InviteToRoomRepository inviteToRoomRepository,
        PdfRepository pdfRepository
    ) {
        this.guestDataGenerator = guestDataGenerator;
        this.employeeDataGenerator = employeeDataGenerator;
        this.roomDataGenerator = roomDataGenerator;
        this.bookingDataGenerator = bookingDataGenerator;
        this.checkInRepository = checkInRepository;
        this.checkOutRepository = checkOutRepository;
        this.uiConfigDataGenerator = uiConfigDataGenerator;
        this.activityDataGenerator = activityDataGenerator;
        this.lockRepository = lockRepository;
        this.inviteToRoomRepository = inviteToRoomRepository;
        this.pdfRepository = pdfRepository;
    }

    /**
     * Generates UI configurations, guests, employees, rooms and bookings in an orderly fashion.
     */
    @PostConstruct
    private void generateData() {
        deleteData();
        LOGGER.debug("generating data...");
        uiConfigDataGenerator.generateUiConfig();
        guestDataGenerator.generateGuests();
        employeeDataGenerator.generateEmployees();
        roomDataGenerator.generateRooms();
        bookingDataGenerator.generatePastBookings();
        bookingDataGenerator.generatePresentBookings();
        bookingDataGenerator.generateFutureBookings();
        activityDataGenerator.generateActivities();
    }

    /**
     * Clears all existing check-ins, bookings, rooms, employees, guests and UI configurations in an orderly fashion.
     */
    private void deleteData() {
        pdfRepository.deleteAll();
        inviteToRoomRepository.deleteAll();
        lockRepository.deleteAll();
        checkInRepository.deleteAll();
        checkOutRepository.deleteAll();
        bookingDataGenerator.clearExistingBookings();
        roomDataGenerator.clearExistingRooms();
        employeeDataGenerator.clearExistingEmployees();
        guestDataGenerator.clearExistingGuests();
        uiConfigDataGenerator.clearExistingUiConfig();
        activityDataGenerator.clearExistingActivities();
    }
}
