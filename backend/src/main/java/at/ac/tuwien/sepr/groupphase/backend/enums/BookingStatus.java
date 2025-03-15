package at.ac.tuwien.sepr.groupphase.backend.enums;

/**
 * Enum that describes the various statuses of a booking.
 * <ul>
 *     <li>{@code PENDING}: The booking is created but not yet confirmed or active.</li>
 *     <li>{@code ACTIVE}: The booking is currently active and within the date range.</li>
 *     <li>{@code CANCELLED}: The booking has been cancelled and is no longer valid.</li>
 *     <li>{@code COMPLETED}: The booking has ended and the stay is complete.</li>
 * </ul>
 */
public enum BookingStatus {
    PENDING,
    ACTIVE,
    CANCELLED,
    COMPLETED
}
