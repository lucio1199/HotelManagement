package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.util.List;

/**
 * A data transfer object for creating a new activity.
 *
 * <p>Contains the necessary fields for creating an activity, including name, description, price and capacity. </p>
 *
 * @param name the name of the activity
 * @param description the description of the activity
 * @param price the price of the activity per person
 * @param capacity the capacity of the activity per slot
 */
public record ActivityCreateDto(
    String name,

    String description,

    Integer capacity,

    Double price,

    String categories

) {
}
