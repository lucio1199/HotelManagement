package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.BookingConfirmationDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Booking;
import at.ac.tuwien.sepr.groupphase.backend.entity.UiConfig;
import at.ac.tuwien.sepr.groupphase.backend.repository.UiConfigRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.PdfGenerationService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class SimplePdfGenerationService implements PdfGenerationService {

    @Value("${application.booking.tax-id}")
    private String taxId;

    private final UiConfigRepository uiConfigRepository;

    @Autowired
    public SimplePdfGenerationService(UiConfigRepository uiConfigRepository) {
        this.uiConfigRepository = uiConfigRepository;
    }

    @Override
    public byte[] generateBookingConfirmation(Booking booking) throws IOException {
        if (booking == null) {
            throw new IllegalArgumentException("Booking cannot be null");
        }

        if (booking.getGuest() == null || booking.getGuest().getFirstName() == null || booking.getGuest().getLastName() == null) {
            throw new IllegalArgumentException("Guest information is incomplete.");
        }
        if (booking.getGuest().getPhoneNumber() == null || booking.getGuest().getEmail() == null) {
            throw new IllegalArgumentException("Guest phone number or email cannot be null.");
        }

        if (booking.getStartDate() == null || booking.getEndDate() == null) {
            throw new IllegalArgumentException("Booking start and end dates cannot be null.");
        }

        if (booking.getRoom() == null || booking.getRoom().getName() == null || booking.getRoom().getDescription() == null) {
            throw new IllegalArgumentException("Room information is incomplete.");
        }

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             PDDocument document = new PDDocument()) {

            PDPage page = new PDPage();
            document.addPage(page);

            UiConfig uiConfig = uiConfigRepository.findById(1L)
                .orElseThrow(() -> new IllegalStateException("UiConfig not found"));

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                final float startX = 50;
                final float startYinitial = 750;
                float startY = startYinitial;
                final float lineSpacing = 14.5f;
                final float paragraphSpacing = 30f;

                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.showText("Booking Confirmation for Your Stay at " + uiConfig.getHotelName());
                contentStream.endText();

                startY -= paragraphSpacing;

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(startX, startY);
                contentStream.showText("Dear " + booking.getGuest().getLastName() + ",");
                contentStream.endText();

                startY -= lineSpacing;

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(startX, startY);
                contentStream.showText("Thank you for choosing the " + uiConfig.getHotelName() +  ". Here are your booking details:");
                contentStream.endText();

                startY -= paragraphSpacing;

                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.showText("Hotel Details:");
                contentStream.endText();

                startY -= lineSpacing;

                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.showText("Hotel Name: " + uiConfig.getHotelName());
                contentStream.endText();

                startY -= lineSpacing;

                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.showText("Hotel Address: " + uiConfig.getAddress());
                contentStream.endText();

                contentStream.setFont(PDType1Font.HELVETICA, 12);

                startY -= paragraphSpacing;

                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.showText("Guest Details:");
                contentStream.endText();

                startY -= lineSpacing;

                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.showText("Name: " + booking.getGuest().getFirstName() + " " + booking.getGuest().getLastName());
                contentStream.endText();

                startY -= lineSpacing;

                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.showText("Phone: " + booking.getGuest().getPhoneNumber());
                contentStream.endText();

                startY -= lineSpacing;

                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.showText("Email: " + booking.getGuest().getEmail());
                contentStream.endText();

                startY -= lineSpacing;

                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.showText("Address: " + booking.getGuest().getAddress());
                contentStream.endText();


                startY -= paragraphSpacing;

                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.showText("Booking Details:");
                contentStream.endText();

                startY -= lineSpacing;

                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.showText("Booking Number: " + booking.getBookingNumber());
                contentStream.endText();

                startY -= lineSpacing;


                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.showText("Check-in: " + booking.getStartDate() + " (after 14:00)");
                contentStream.endText();

                startY -= lineSpacing;

                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.showText("Check-out: " + booking.getEndDate() + " (before 10:00)");
                contentStream.endText();

                startY -= paragraphSpacing;

                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.showText("Room Details:");
                contentStream.endText();

                startY -= lineSpacing;

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(startX, startY);
                contentStream.showText("Room Name: " + booking.getRoom().getName());
                contentStream.endText();

                startY -= lineSpacing;

                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.showText("Room Description: " + booking.getRoom().getDescription());
                contentStream.endText();

                startY -= lineSpacing;

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(startX, startY);
                contentStream.showText("Number of persons : " + booking.getRoom().getCapacity());
                contentStream.endText();

                startY -= paragraphSpacing;

                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.showText("Cancellation:");
                contentStream.endText();

                startY -= lineSpacing;

                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.showText("To cancel your booking, visit the \"My Bookings\" page in your account.");
                contentStream.endText();

                startY -= lineSpacing;

                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.showText("After cancellation, a refund will be processed.");
                contentStream.endText();

                startY -= paragraphSpacing;

                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.showText("Questions:");
                contentStream.endText();

                startY -= lineSpacing;

                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.showText("If you have any questions, contact us at inncontrolhotel@gmail.com.");
                contentStream.endText();

                startY -= lineSpacing;

                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.showText("Include your booking number (" + booking.getBookingNumber() + ") and room name.");
                contentStream.endText();

                startY -= paragraphSpacing;

                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.showText("We look forward to your stay!");
                contentStream.endText();
            }

            document.save(byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        }
    }




    @Override
    public byte[] generateInvoice(Booking booking) throws IOException {

        UiConfig uiConfig = uiConfigRepository.findById(1L)
            .orElseThrow(() -> new IllegalStateException("UiConfig not found"));

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            PDDocument document = new PDDocument();
            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            final float startX = 50;
            final float startYinitial = 750;
            float startY = startYinitial;

            final float lineSpacing = 14.5f;
            final float paragraphSpacing = 30f;
            final float rightX = 300;

            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);

            contentStream.beginText();
            contentStream.newLineAtOffset(startX, startY);
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
            contentStream.showText("Invoice Confirmation for Your Stay at " + uiConfig.getHotelName());
            contentStream.endText();

            startY -= paragraphSpacing;

            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(startX, startY);
            contentStream.showText("Hotel Name: " + uiConfig.getHotelName());
            contentStream.endText();

            startY -= lineSpacing;

            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(startX, startY);
            contentStream.showText("Hotel Address: " + uiConfig.getAddress());
            contentStream.endText();

            startY -= lineSpacing;

            contentStream.beginText();
            contentStream.newLineAtOffset(startX, startY);
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.showText("Email: inncontrolhotel@gmail.com");
            contentStream.endText();
            startY -= lineSpacing;

            contentStream.beginText();
            contentStream.newLineAtOffset(startX, startY);
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.showText("Tax ID: " + taxId);
            contentStream.endText();

            startY -= paragraphSpacing;
            startY -= paragraphSpacing;

            contentStream.beginText();
            contentStream.newLineAtOffset(rightX, startY);
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.showText("Bill to: " + booking.getGuest().getFirstName() + " " + booking.getGuest().getLastName());
            contentStream.endText();
            startY -= lineSpacing;

            contentStream.beginText();
            contentStream.newLineAtOffset(rightX, startY);
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.showText("Address: " + booking.getGuest().getAddress());
            contentStream.endText();
            startY -= lineSpacing;

            contentStream.beginText();
            contentStream.newLineAtOffset(rightX, startY);
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.showText("Booking Number: " + booking.getBookingNumber());
            contentStream.endText();

            startY -= paragraphSpacing;
            startY -= paragraphSpacing;

            contentStream.beginText();
            contentStream.newLineAtOffset(startX, startY);
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.showText("Booking Details:");
            contentStream.endText();
            startY -= lineSpacing;

            contentStream.beginText();
            contentStream.newLineAtOffset(startX, startY);
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.showText("Booking Period: " + booking.getStartDate() + " to " + booking.getEndDate());
            contentStream.endText();
            startY -= lineSpacing;

            contentStream.beginText();
            contentStream.newLineAtOffset(startX, startY);
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.showText("Booking Date: " + booking.getBookingDate());
            contentStream.endText();
            startY -= lineSpacing;

            contentStream.beginText();
            contentStream.newLineAtOffset(startX, startY);
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.showText("Room Name: " + booking.getRoom().getName());
            contentStream.endText();
            startY -= lineSpacing;

            contentStream.beginText();
            contentStream.newLineAtOffset(startX, startY);
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.showText("Number of Guests: " + booking.getRoom().getCapacity());
            contentStream.endText();
            startY -= lineSpacing;

            contentStream.beginText();
            contentStream.newLineAtOffset(startX, startY);
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.showText("Total Nights: " + booking.getNumberOfNights());
            contentStream.endText();
            startY -= lineSpacing;

            startY -= paragraphSpacing;

            contentStream.beginText();
            contentStream.newLineAtOffset(startX, startY);
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.showText("Invoice Details");
            contentStream.endText();
            startY -= lineSpacing;

            contentStream.beginText();
            contentStream.newLineAtOffset(startX, startY);
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.showText("Invoice Number: " + booking.getInvoiceNumber());
            contentStream.endText();
            startY -= lineSpacing;

            contentStream.beginText();
            contentStream.newLineAtOffset(startX, startY);
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.showText("Price per Night (€): " + String.format("%.2f", booking.getRoom().getPrice()));
            contentStream.endText();
            startY -= lineSpacing;

            startY -= paragraphSpacing;

            double pricePerNight = booking.getRoom().getPrice();
            int numberOfNights = booking.calculateNumberOfNights();
            double netAmount = pricePerNight * numberOfNights;
            double taxAmount = booking.getTaxAmount();
            final double totalAmount = netAmount + taxAmount;

            contentStream.beginText();
            contentStream.newLineAtOffset(startX, startY);
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.showText("Subtotal (Net):");
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(startX + 200, startY);
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.showText("€  " + String.format("%.2f", netAmount));
            contentStream.endText();
            startY -= lineSpacing;

            contentStream.beginText();
            contentStream.newLineAtOffset(startX, startY);
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.showText("Tax (10%):");
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(startX + 200, startY);
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.showText("€  " + String.format("%.2f", taxAmount));
            contentStream.endText();
            startY -= lineSpacing;

            contentStream.setLineWidth(1f);
            contentStream.moveTo(startX, startY);
            contentStream.lineTo(startX + 300, startY);
            contentStream.stroke();

            startY -= 15f;

            contentStream.beginText();
            contentStream.newLineAtOffset(startX, startY);
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.showText("Total (Gross):");
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(startX + 200, startY);
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.showText("€  " + String.format("%.2f", totalAmount));
            contentStream.endText();
            startY -= paragraphSpacing;

            contentStream.beginText();
            contentStream.newLineAtOffset(startX, startY);
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.showText("Note: For inquiries regarding this invoice, please contact us at inncontrolhotel@gmail.com.");
            contentStream.endText();

            contentStream.close();

            document.save(byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        }
    }


    @Override
    public byte[] generateCancellation(Booking booking) throws IOException {

        UiConfig uiConfig = uiConfigRepository.findById(1L)
            .orElseThrow(() -> new IllegalStateException("UiConfig not found"));

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             PDDocument document = new PDDocument()) {

            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                final float startX = 50;
                final float startYinitial = 750;
                float startY = startYinitial;

                final float lineSpacing = 14.5f;
                final float paragraphSpacing = 30f;

                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);

                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                contentStream.showText("Cancellation Confirmation for Your Stay at " + uiConfig.getHotelName());
                contentStream.endText();

                startY -= paragraphSpacing;

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(startX, startY);
                contentStream.showText("Dear " + booking.getGuest().getLastName() + ",");
                contentStream.endText();

                startY -= lineSpacing;

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(startX, startY);
                contentStream.showText("We would like to confirm the cancellation of your booking with the following details:");
                contentStream.endText();

                startY -= paragraphSpacing;

                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.showText("Hotel Details:");
                contentStream.endText();

                startY -= lineSpacing;

                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.showText("Hotel Name: " + uiConfig.getHotelName());
                contentStream.endText();

                startY -= lineSpacing;

                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.showText("Hotel Address: " + uiConfig.getAddress());
                contentStream.endText();

                startY -= lineSpacing;

                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.showText("Email: inncontrolhotel@gmail.com");
                contentStream.endText();

                startY -= paragraphSpacing;

                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.showText("Guest Details:");
                contentStream.endText();

                startY -= lineSpacing;

                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.showText("Name: " + booking.getGuest().getFirstName() + " " + booking.getGuest().getLastName());
                contentStream.endText();

                startY -= lineSpacing;

                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.showText("Booking Number: " + booking.getBookingNumber());
                contentStream.endText();

                startY -= lineSpacing;

                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.showText("Room Name: " + booking.getRoom().getName());
                contentStream.endText();

                startY -= paragraphSpacing;

                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.showText("Cancellation Details:");
                contentStream.endText();

                startY -= lineSpacing;

                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.showText("Cancellation Date: " + booking.getCancellationDate());
                contentStream.endText();
                startY -= lineSpacing;

                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.showText("Check-in Date: " + booking.getStartDate());
                contentStream.endText();

                startY -= lineSpacing;

                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.showText("Check-out Date: " + booking.getEndDate());
                contentStream.endText();

                startY -= paragraphSpacing;

                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.showText("Please be informed that your booking has been successfully cancelled.");
                contentStream.endText();

                startY -= paragraphSpacing;

                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.showText("Important Note:");
                contentStream.endText();

                startY -= lineSpacing;

                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.showText("If you have paid in advance, you will receive a refund of your payment shortly. ");
                contentStream.endText();

                startY -= paragraphSpacing;

                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.showText("Refund amount: " + booking.getTotalAmount() + "€");
                contentStream.endText();

                startY -= paragraphSpacing;

                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.showText("For any further inquiries or assistance, please do not hesitate to contact us at ");
                contentStream.endText();

                startY -= lineSpacing;

                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.showText("inncontrolhotel@gmail.com");
                contentStream.endText();

                startY -= paragraphSpacing;

                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.showText("Thank you for choosing " + uiConfig.getHotelName() + ". We hope to welcome you again in the future.");
                contentStream.endText();

                startY -= paragraphSpacing;

                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.showText("Best regards,");
                contentStream.endText();

                startY -= lineSpacing;

                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.showText(uiConfig.getHotelName());
                contentStream.endText();
            }

            document.save(byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        }
    }
}
