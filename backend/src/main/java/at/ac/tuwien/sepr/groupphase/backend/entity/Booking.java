package at.ac.tuwien.sepr.groupphase.backend.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import at.ac.tuwien.sepr.groupphase.backend.enums.BookingStatus;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private ApplicationUser user;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(name = "is_paid", nullable = false)
    private boolean isPaid = false;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @Column
    private LocalDate cancellationDate;

    @Column(name = "booking_number", nullable = false, unique = true)
    private String bookingNumber;

    @Column(nullable = false, updatable = false)
    private LocalDate bookingDate;

    @Column
    private String stripeSessionId;

    @Column
    private String stripePaymentIntentId;

    @Column(nullable = false, unique = true)
    private String invoiceNumber;

    @Column(nullable = false)
    private LocalDate invoiceDate;

    @Column(nullable = false)
    private Double taxAmount;

    @Column(nullable = false)
    private Integer numberOfNights;

    @PrePersist
    @PreUpdate
    public void updateStatus() {
        if (this.status == BookingStatus.CANCELLED) {
            return;
        }

        LocalDate today = LocalDate.now();
        if ((startDate.isEqual(today) || startDate.isBefore(today)) && endDate.isAfter(today)) {
            this.status = BookingStatus.ACTIVE;
        } else if (endDate.isBefore(today)) {
            this.status = BookingStatus.COMPLETED;
        } else {
            this.status = BookingStatus.PENDING;
        }

        if (this.invoiceNumber == null) {
            this.invoiceNumber = generateInvoiceNumber();
        }

        if (this.invoiceDate == null) {
            this.invoiceDate = LocalDate.now();
        }
        if (this.taxAmount == null) {
            this.taxAmount = calculateTaxAmount();
        }

        if (this.numberOfNights == null) {
            this.numberOfNights = calculateNumberOfNights();
        }
    }

    public Double getTotalAmount() {
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        double netAmount = room.getPrice() * days;
        double taxAmount = netAmount * 0.10;
        return netAmount + taxAmount;
    }

    private Double calculateTaxAmount() {
        double netAmount = getTotalAmount() / 1.10;  // Assuming 10% tax rate
        return getTotalAmount() - netAmount;
    }

    public String generateBookingNumber() {
        return "BOOK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generateInvoiceNumber() {
        return "INV-" + UUID.randomUUID();
    }

    public Guest getGuest() {
        if (this.user instanceof Guest) {
            return (Guest) this.user;
        }
        return null;
    }

    public Integer calculateNumberOfNights() {
        if (startDate != null && endDate != null) {
            return (int) ChronoUnit.DAYS.between(startDate, endDate);
        }
        return 0;
    }


    public Booking(Room room, ApplicationUser user, LocalDate startDate, LocalDate endDate, boolean isPaid, BookingStatus status) {
        this.room = room;
        this.user = user;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isPaid = isPaid;
        this.status = status != null ? status : BookingStatus.PENDING;
        this.bookingNumber = bookingNumber != null ? bookingNumber : generateBookingNumber();
        this.bookingDate = LocalDate.now();
    }

    public static final class BookingBuilder {
        private Long id;
        private Room room;
        private ApplicationUser user;
        private LocalDate startDate;
        private LocalDate endDate;
        private boolean isPaid = true;
        private BookingStatus status = null;

        private BookingBuilder() {
        }


        public static BookingBuilder aBooking() {
            return new BookingBuilder();
        }


        public BookingBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public BookingBuilder withRoom(Room room) {
            this.room = room;
            return this;
        }

        public BookingBuilder withUser(ApplicationUser user) {
            this.user = user;
            return this;
        }

        public BookingBuilder withStartDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public BookingBuilder withEndDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public BookingBuilder withIsPaid(boolean isPaid) {
            this.isPaid = isPaid;
            return this;
        }

        public BookingBuilder withStatus(BookingStatus status) {
            this.status = status;
            return this;
        }

        public Booking build() {
            Booking booking = new Booking(room, user, startDate, endDate, isPaid, status);
            booking.setId(id);  // Set the ID if available
            return booking;
        }
    }
}
