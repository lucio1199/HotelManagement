package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.util.List;

/**
 * A DTO for the UI configuration homepage.
 */
public record UiConfigHomepageDto(
    String hotelName,
    String descriptionShort,
    String description,
    String address,
    List<String> images
) {
}
