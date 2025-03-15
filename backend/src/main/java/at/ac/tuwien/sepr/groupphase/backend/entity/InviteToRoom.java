package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "invite_to_room", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"booking_id", "user_id"})
})
public class InviteToRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private ApplicationUser guest;

    public InviteToRoom(Booking booking, ApplicationUser guest) {
        this.booking = booking;
        this.guest = guest;
    }

    // Instance-level getter for booking ID
    public Long getBookingId() {
        return booking != null ? booking.getId() : null;
    }

    // Instance-level getter for room
    public Room getRoom() {
        return booking != null ? booking.getRoom() : null;
    }

    // Instance-level getter for email
    public String getEmail() {
        return guest != null ? guest.getEmail() : null;
    }

    public static final class InviteToRoomBuilder {
        private Long id;
        private Booking booking;
        private ApplicationUser guest;

        private InviteToRoomBuilder() {
        }

        public static InviteToRoomBuilder aCheckIn() {
            return new InviteToRoomBuilder();
        }

        public InviteToRoomBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public InviteToRoomBuilder withBooking(Booking booking) {
            this.booking = booking;
            return this;
        }

        public InviteToRoomBuilder withGuest(ApplicationUser guest) {
            this.guest = guest;
            return this;
        }

        public InviteToRoom build() {
            InviteToRoom inviteToRoom = new InviteToRoom(booking, guest);
            inviteToRoom.setId(id);  // Set the ID if available
            return inviteToRoom;
        }
    }
}
