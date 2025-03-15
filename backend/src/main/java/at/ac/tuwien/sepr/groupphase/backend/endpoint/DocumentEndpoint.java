package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.entity.Pdf;
import at.ac.tuwien.sepr.groupphase.backend.repository.PdfRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.CheckInService;
import at.ac.tuwien.sepr.groupphase.backend.service.PdfStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentEndpoint.class);

    private final PdfStorageService pdfStorageService;
    private final CheckInService checkInService;

    public DocumentEndpoint(PdfStorageService pdfStorageService,
                            PdfRepository pdfRepository, CheckInService checkInService) {
        this.pdfStorageService = pdfStorageService;
        this.checkInService = checkInService;
    }

    @GetMapping("/{bookingId}/pdfs/{type}")
    public ResponseEntity<byte[]> getDocument(@PathVariable Long bookingId, @PathVariable String type) {
        Pdf document = pdfStorageService.getPdf(bookingId, type);
        byte[] content = document.getContent();

        LOGGER.info("Fetched PDF for bookingId: {}, type: {}", bookingId, type);

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .body(content);
    }

    @GetMapping(value = "/passport/{bookingId}/{email}", produces = "application/pdf")
    public ResponseEntity<byte[]> getPassport(@PathVariable("bookingId") Long bookingId, @PathVariable("email") String email) {
        // Get the passport as a byte array
        byte[] passport = checkInService.getPassportByBookingIdAndEmail(bookingId, email);

        // Set headers to indicate this is a PDF response
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.builder("inline")
            .filename("passport.pdf")
            .build());

        // Return response with passport and headers
        return new ResponseEntity<>(passport, headers, HttpStatus.OK);
    }
}
