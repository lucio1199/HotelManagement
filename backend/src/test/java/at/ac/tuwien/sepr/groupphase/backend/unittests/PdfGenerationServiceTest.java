package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.PdfMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.*;
import at.ac.tuwien.sepr.groupphase.backend.repository.PdfRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UiConfigRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.SimplePdfGenerationService;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.SimplePdfStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PdfGenerationServiceTest {

    @Mock
    private UiConfigRepository uiConfigRepository;

    @Mock
    private PdfRepository pdfRepository;

    @Mock
    private PdfMapper pdfMapper;

    @InjectMocks
    private SimplePdfGenerationService pdfGenerationService;


    /**
     * Test case to verify that a booking confirmation PDF is generated correctly for a valid booking.
     */
    @Test
    public void testGenerateBookingConfirmation_ValidBooking() throws IOException {
        ApplicationUser user = new ApplicationUser();
        user.setEmail("test@example.com");
        user.setId(1L);

        Room room = new Room();
        room.setId(1L);
        room.setName("Room Name");
        room.setDescription("Single Room with a great view");
        room.setCapacity(2);
        room.setHalfBoard(true);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(user);
        booking.setRoom(room);
        booking.setStartDate(LocalDate.of(2025, 1, 20));
        booking.setEndDate(LocalDate.of(2025, 1, 25));
        booking.setBookingNumber("BOOK-8FD8E9C0");


        Guest guest = new Guest();
        guest.setFirstName("John");
        guest.setLastName("Doe");
        guest.setEmail("guest@example.com");
        guest.setPhoneNumber("+123456789");
        guest.setAddress("123 Guest Street");
        booking.setUser(guest);

        UiConfig uiConfig = new UiConfig();
        uiConfig.setHotelName("InnControl Hotel");
        when(uiConfigRepository.findById(1L)).thenReturn(Optional.of(uiConfig));

        SimplePdfGenerationService pdfGenerationService = new SimplePdfGenerationService(uiConfigRepository);


        byte[] result = pdfGenerationService.generateBookingConfirmation(booking);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    /**
     * Test case to verify that an invoice PDF is generated correctly for a valid booking with all necessary details.
     */
    @Test
    public void testGenerateInvoice_ValidBooking() throws IOException {
        ApplicationUser user = new ApplicationUser();
        user.setEmail("test@example.com");
        user.setId(1L);

        Room room = new Room();
        room.setId(1L);
        room.setName("Room Name");
        room.setDescription("Single Room with a great view");
        room.setCapacity(2);
        room.setPrice(100.0);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(user);
        booking.setRoom(room);
        booking.setStartDate(LocalDate.of(2025, 1, 20));
        booking.setEndDate(LocalDate.of(2025, 1, 25));
        booking.setBookingNumber("BOOK-8FD8E9C0");
        booking.setInvoiceNumber("INV-12345");
        booking.setInvoiceDate(LocalDate.now());

        Guest guest = new Guest();
        guest.setFirstName("John");
        guest.setLastName("Doe");
        guest.setEmail("guest@example.com");
        guest.setPhoneNumber("+123456789");
        guest.setAddress("123 Guest Street");
        booking.setUser(guest);

        booking.setTaxAmount(50.0);

        UiConfig uiConfig = new UiConfig();
        uiConfig.setHotelName("InnControl Hotel");
        when(uiConfigRepository.findById(1L)).thenReturn(Optional.of(uiConfig));

        SimplePdfGenerationService pdfGenerationService = new SimplePdfGenerationService(uiConfigRepository);

        byte[] result = pdfGenerationService.generateInvoice(booking);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    public void testGenerateCancellation_ValidBooking() throws IOException {
        ApplicationUser user = new ApplicationUser();
        user.setEmail("test@example.com");
        user.setId(1L);

        Room room = new Room();
        room.setId(1L);
        room.setName("Room Name");

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(user);
        booking.setRoom(room);
        booking.setStartDate(LocalDate.of(2025, 1, 20));
        booking.setEndDate(LocalDate.of(2025, 1, 25));
        booking.setBookingNumber("BOOK-8FD8E9C0");
        booking.setCancellationDate(LocalDate.now());

        Guest guest = new Guest();
        guest.setFirstName("John");
        guest.setLastName("Doe");
        guest.setPhoneNumber("+123456789");
        booking.setUser(guest);

        UiConfig uiConfig = new UiConfig();
        uiConfig.setHotelName("InnControl Hotel");
        when(uiConfigRepository.findById(1L)).thenReturn(Optional.of(uiConfig));

        SimplePdfGenerationService pdfGenerationService = new SimplePdfGenerationService(uiConfigRepository);

        byte[] result = pdfGenerationService.generateCancellation(booking);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }


    /**
     * Test case to verify that an exception is thrown when trying to generate a PDF for a booking without a valid user.
     */
    @Test
    public void testGenerateBookingConfirmation_NoUser() throws IOException {
        ApplicationUser user = null;
        Room room = new Room();
        room.setId(1L);
        room.setName("Room Name");
        room.setDescription("Single Room with a great view");
        room.setCapacity(2);
        room.setHalfBoard(true);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(user);
        booking.setRoom(room);
        booking.setStartDate(LocalDate.of(2025, 1, 20));
        booking.setEndDate(LocalDate.of(2025, 1, 25));
        booking.setBookingNumber("BOOK-8FD8E9C0");

        UiConfig uiConfig = new UiConfig();
        uiConfig.setHotelName("InnControl Hotel");

        assertThrows(IllegalArgumentException.class, () -> {
            pdfGenerationService.generateBookingConfirmation(booking);
        });
    }
}
