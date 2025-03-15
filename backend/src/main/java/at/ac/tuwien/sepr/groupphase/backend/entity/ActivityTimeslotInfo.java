package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Entity
public class ActivityTimeslotInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column
    private DayOfWeek dayOfWeek;

    @CreatedDate
    @Column
    private LocalDate specificDate;

    @CreatedDate
    @Column
    private LocalTime startTime;

    @CreatedDate
    @Column
    private LocalTime endTime;



    @PrePersist
    @PreUpdate
    private void validateTimeslot() {
        if ((dayOfWeek == null && specificDate == null)
            || (dayOfWeek != null && specificDate != null)) {
            throw new IllegalArgumentException("A timeslot must have either a DayOfWeek or a specific date, but not both.");
        }
        if (startTime == null || endTime == null || !endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("Start time and end time must be valid and end time must be after start time.");
        }
    }

}
