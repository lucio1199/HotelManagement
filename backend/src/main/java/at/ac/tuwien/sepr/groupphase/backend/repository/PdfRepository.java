package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.Pdf;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PdfRepository extends JpaRepository<Pdf, Long> {

    /**
     * Finds a PDF document by its associated booking ID and document type.
     *
     * @param bookingId the ID of the associated booking
     * @param type the type of the PDF document (e.g., invoice, cancellation receipt)
     * @return an {@link Optional} containing the PDF if found, or empty if not
     */
    Optional<Pdf> findByBookingIdAndDocumentType(Long bookingId, String type);
}
