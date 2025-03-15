package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.entity.Pdf;
import org.mapstruct.Mapper;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface PdfMapper {

    default Pdf mapToEntity(Long bookingId, String type, byte[] content) {
        return new Pdf(
            null,
            bookingId,
            LocalDateTime.now(),
            type,
            content
        );
    }
}
