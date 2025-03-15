package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.entity.Pdf;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.PdfRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.SimplePdfStorageService;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.PdfMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;



@ExtendWith(MockitoExtension.class)
class PdfStorageServiceTest {

    @Mock
    private PdfRepository pdfRepository;

    @Mock
    private PdfMapper pdfMapper;

    @InjectMocks
    private SimplePdfStorageService pdfStorageService;

    /**
     * Test case to verify that a PDF is stored correctly in the repository.
     * It ensures that the mapped PDF entity is saved in the repository.
     */
    @Test
    void testStorePdf() {
        Long bookingId = 1L;
        String type = "invoice";
        byte[] content = new byte[]{1, 2, 3};
        Pdf mappedPdf = new Pdf();

        when(pdfMapper.mapToEntity(bookingId, type, content)).thenReturn(mappedPdf);

        pdfStorageService.storePdf(bookingId, type, content);

        verify(pdfRepository, times(1)).save(mappedPdf);
    }

    /**
     * Test case to verify that a PDF can be successfully retrieved from the repository when it exists.
     * It ensures that the correct PDF is returned when found in the repository.
     */
    @Test
    void testGetPdf_Success() {
        Long bookingId = 1L;
        String type = "invoice";
        Pdf pdf = new Pdf();
        when(pdfRepository.findByBookingIdAndDocumentType(bookingId, type)).thenReturn(java.util.Optional.of(pdf));

        Pdf result = pdfStorageService.getPdf(bookingId, type);

        assertNotNull(result);
    }


    /**
     * Test case to verify that a {@link NotFoundException} is thrown when no PDF is found in the repository.
     * It ensures that the exception is thrown with the correct message when the PDF is not found.
     */
    @Test
    void testGetPdf_NotFound() {
        Long bookingId = 1L;
        String type = "invoice";
        when(pdfRepository.findByBookingIdAndDocumentType(bookingId, type)).thenReturn(java.util.Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            pdfStorageService.getPdf(bookingId, type);
        });

        assertEquals("PDF not found for bookingId: 1 and type: invoice", exception.getMessage());
    }

    /**
     * Test case to verify that a {@link RuntimeException} is thrown if an error occurs while storing a PDF in the repository.
     * It ensures that the exception is properly propagated when a database error occurs during the save operation.
     */
    @Test
    void testStorePdf_Exception() {
        Long bookingId = 1L;
        String type = "invoice";
        byte[] content = new byte[]{1, 2, 3};
        Pdf mappedPdf = new Pdf();

        when(pdfMapper.mapToEntity(bookingId, type, content)).thenReturn(mappedPdf);
        doThrow(new RuntimeException("Database error")).when(pdfRepository).save(mappedPdf);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pdfStorageService.storePdf(bookingId, type, content);
        });

        assertEquals("Database error", exception.getMessage());
    }

    /**
     * Test case to verify that a {@link NotFoundException} is thrown when trying to delete a PDF that does not exist in the repository.
     */
    @Test
    void testDeletePdf_NotFound() {
        Long bookingId = 1L;
        String fileName = "invoice";

        when(pdfRepository.findByBookingIdAndDocumentType(bookingId, fileName)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            pdfStorageService.deletePdf(bookingId, fileName);
        });

        assertEquals("PDF not found for bookingId: 1 and file name: invoice", exception.getMessage());
    }

    /**
     * Test case to verify that an {@link IllegalArgumentException} is thrown when trying to store a PDF with a null bookingId.
     */
    @Test
    void testStorePdf_NullBookingId() {
        Long bookingId = null;
        String type = "invoice";
        byte[] content = new byte[]{1, 2, 3};

        assertThrows(IllegalArgumentException.class, () -> {
            pdfStorageService.storePdf(bookingId, type, content);
        });
    }
}