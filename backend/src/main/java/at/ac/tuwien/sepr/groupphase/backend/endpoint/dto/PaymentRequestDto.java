package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;


public record PaymentRequestDto(
    Long bookingId,
    Long roomId
) {
}
