package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.PdfMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.Pdf;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.PdfRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.PdfStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SimplePdfStorageService implements PdfStorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleBookingService.class);

    private final PdfRepository pdfRepository;
    private final PdfMapper pdfMapper;

    public SimplePdfStorageService(PdfRepository pdfRepository, PdfMapper pdfMapper) {
        this.pdfRepository = pdfRepository;
        this.pdfMapper = pdfMapper;
    }

    @Override
    public void storePdf(Long bookingId, String type, byte[] content) {
        if (bookingId == null) {
            throw new IllegalArgumentException("Booking ID cannot be null");
        }

        LOGGER.debug("Storing PDF for bookingId: " + bookingId + " with content size: " + content.length);
        Pdf document = pdfMapper.mapToEntity(bookingId, type, content);
        pdfRepository.save(document);
    }

    public Pdf getPdf(Long bookingId, String type) {
        return pdfRepository.findByBookingIdAndDocumentType(bookingId, type)
            .orElseThrow(() -> new NotFoundException("PDF not found for bookingId: " + bookingId + " and type: " + type));
    }

    public void deletePdf(Long bookingId, String fileName) {
        LOGGER.debug("Deleting PDF for booking ID {} with file name {}", bookingId, fileName);

        Pdf pdf = pdfRepository.findByBookingIdAndDocumentType(bookingId, fileName)
            .orElseThrow(() -> new NotFoundException("PDF not found for bookingId: " + bookingId + " and file name: " + fileName));

        pdfRepository.delete(pdf);
        LOGGER.info("Deleted PDF for booking ID {} with file name {}", bookingId, fileName);
    }
}

