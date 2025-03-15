package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

/**
 * Data transfer object for UiImage.
 */
public record UiImageDto(
    Long id,
    String altText,
    String imageData // Base64 string

) {

    @Override
    public String toString() {
        return "UiImage{"
            + "id=" + id
            + ", altText='" + altText + '\''
            + '}';
    }
}