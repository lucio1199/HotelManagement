package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;


public record StripeSessionDto(
    String url,
    String sessionId
) {
}
