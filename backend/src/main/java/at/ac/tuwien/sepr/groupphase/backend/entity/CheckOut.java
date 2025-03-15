package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class CheckOut {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private ApplicationUser guest;

    private LocalDateTime date;

    public CheckOut(Booking booking, LocalDateTime date, ApplicationUser guest) {
        this.booking = booking;
        this.date = date;
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

    public static final class CheckOutBuilder {
        private Long id;
        private Booking booking;
        private LocalDateTime date;
        private ApplicationUser guest;

        private CheckOutBuilder() {
        }

        public static CheckOutBuilder aCheckIn() {
            return new CheckOutBuilder();
        }

        public CheckOutBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public CheckOutBuilder withBooking(Booking booking) {
            this.booking = booking;
            return this;
        }

        public CheckOutBuilder withDate(LocalDateTime date) {
            this.date = date;
            return this;
        }

        public CheckOutBuilder withGuest(ApplicationUser guest) {
            this.guest = guest;
            return this;
        }

        public CheckOut build() {
            CheckOut checkOut = new CheckOut(booking, date, guest);
            checkOut.setId(id);  // Set the ID if available
            return checkOut;
        }
    }
}
