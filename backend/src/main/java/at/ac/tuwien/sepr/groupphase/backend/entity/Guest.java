package at.ac.tuwien.sepr.groupphase.backend.entity;

import at.ac.tuwien.sepr.groupphase.backend.enums.Gender;
import at.ac.tuwien.sepr.groupphase.backend.enums.Nationality;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@Table(
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"passportNumber", "nationality"})
    }
)
public class Guest extends ApplicationUser {

    @Size(max = 64)
    private String firstName;

    @Size(max = 64)
    private String lastName;

    private LocalDate dateOfBirth;

    @Size(max = 128)
    private String placeOfBirth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private Nationality nationality;

    @Size(max = 256)
    private String address;

    @Size(max = 64)
    private String passportNumber;

    @Size(max = 20)
    private String phoneNumber;

    @OneToOne(mappedBy = "guest", cascade = CascadeType.ALL, orphanRemoval = true)
    private GuestActivityCategory guestActivityCategory;
}

