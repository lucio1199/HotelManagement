package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.util.List;

/**
 * A data transfer object for updating an existing activity.
 *
 * <p>Contains the necessary fields for updating an activity, including its unique identifier,
 * name, description, price, and capacity. This DTO is used to transfer data from the client
 * to the server when an activity is updated.</p>
 *
 * @param id the unique identifier of the activity to be updated, must not be {@code null}.
 * @param name the updated name of the activity, may be {@code null} if not being modified.
 * @param description the updated description of the activity, may be {@code null} if not being modified.
 * @param price the updated price of the activity, may be {@code null} if not being modified.
 * @param capacity the updated capacity of the activity, may be {@code null} if not being modified.
 */
public record ActivityUpdateDto(
    Long id,
    String name,
    String description,
    Double price,
    Integer capacity,
    String categories

) {
}
