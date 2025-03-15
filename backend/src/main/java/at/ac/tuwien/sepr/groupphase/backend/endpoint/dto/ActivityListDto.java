package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.util.List;

/**
 * A data transfer object for listing activities.
 *
 * <p>Provides the necessary fields for displaying activities in a list, including the activity's
 * ID, name, price, capacity, and a main image.</p>
 *
 * @param id the unique identifier of the activity.
 * @param name the name of the activity.
 * @param price the price of the activity.
 * @param capacity the capacity of the activity.
 * @param mainImage main image representing the activity.
 */
public record ActivityListDto(
    Long id,
    String name,
    double price,
    int capacity,
    String mainImage,
    List<ActivityTimeslotInfoDto> activityTimeslotInfos
) {
}
