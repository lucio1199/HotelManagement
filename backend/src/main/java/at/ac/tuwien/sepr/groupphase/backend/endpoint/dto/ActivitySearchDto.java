package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.time.LocalDate;

/**
 * Data transfer object for searching activities.
 */
public record ActivitySearchDto(
    String name,
    LocalDate date,
    Double maxPrice,
    Double minPrice,
    Integer capacity
) {
}
