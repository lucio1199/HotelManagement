package at.ac.tuwien.sepr.groupphase.backend.entity;

import at.ac.tuwien.sepr.groupphase.backend.enums.BookingStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;

import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActivityBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private ApplicationUser user;

    @ManyToOne
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @Column
    private LocalDate bookingDate;

    @ManyToOne
    @JoinColumn(name = "activity_slot_id")
    private ActivitySlot activitySlot;

    @Column
    private String stripeSessionId;

    @Column
    private String stripePaymentIntentId;

    @Column
    private int participants;

    @Column
    private double totalPrice;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @Column
    private boolean isPaid = false;

    public Double getTotalAmount() {
        return activity.getPrice() * participants;
    }
}
