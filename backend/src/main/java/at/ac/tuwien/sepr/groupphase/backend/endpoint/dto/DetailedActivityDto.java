package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * A data transfer object representing detailed information about an activity.
 *
 * <p>This DTO includes all the necessary fields to represent an activity in detail,
 * such as its unique identifier, name, description, price, capacity, main image,
 * additional images, and available timeslots. It is typically used for displaying
 * comprehensive information about an activity.</p>
 *
 * @param id the unique identifier of the activity, must not be {@code null}.
 * @param name the name of the activity, must not be {@code null} and must be between 3 and 100 characters.
 * @param description a detailed description of the activity, must not be {@code null}.
 * @param price the price of the activity, must not be {@code null} and must be non-negative.
 * @param capacity the capacity of the activity, must not be {@code null} and must be at least 1.
 * @param mainImage the URL or path to the main image representing the activity, must not be {@code null}.
 * @param additionalImages a list of URLs or paths to additional images for the activity, eagerly fetched.
 * @param timeslots a list of timeslots during which the activity is available, eagerly fetched.
 */
public record DetailedActivityDto(

    Long id,

    String name,
    String description,
    Double price,
    Integer capacity,
    String mainImage,
    List<String> additionalImages,
    List<ActivityTimeslotInfoDto> timeslots,
    String categories
) {
}
