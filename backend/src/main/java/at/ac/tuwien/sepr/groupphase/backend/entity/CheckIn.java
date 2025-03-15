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
public class CheckIn {

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

    @Lob
    private byte[] passport;

    public CheckIn(Booking booking, LocalDateTime date, ApplicationUser guest, byte[] passport) {
        this.booking = booking;
        this.date = date;
        this.guest = guest;
        this.passport = passport;
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

    public static final class CheckInBuilder {
        private Long id;
        private Booking booking;
        private LocalDateTime date;
        private ApplicationUser guest;
        private byte[] passport;

        private CheckInBuilder() {
        }

        public static CheckInBuilder aCheckIn() {
            return new CheckInBuilder();
        }

        public CheckInBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public CheckInBuilder withBooking(Booking booking) {
            this.booking = booking;
            return this;
        }

        public CheckInBuilder withDate(LocalDateTime date) {
            this.date = date;
            return this;
        }

        public CheckInBuilder withGuest(ApplicationUser guest) {
            this.guest = guest;
            return this;
        }

        public CheckInBuilder withPassport(byte[] passport) {
            this.passport = passport;
            return this;
        }

        public CheckIn build() {
            CheckIn checkIn = new CheckIn(booking, date, guest, passport);
            checkIn.setId(id);  // Set the ID if available
            return checkIn;
        }
    }
}
