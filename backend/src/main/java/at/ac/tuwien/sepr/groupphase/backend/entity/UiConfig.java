package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UiConfig {

    @Id
    private Long id;

    @NotNull
    private String hotelName;

    @NotNull
    private String descriptionShort;

    @NotNull
    @Column(length = 1000)
    private String description;

    @NotNull
    private String address;

    @NotNull
    private Boolean roomCleaning;

    @NotNull
    private Boolean digitalCheckIn;

    @NotNull
    private Boolean activities;

    @NotNull
    private Boolean communication;

    @NotNull
    private Boolean nuki;

    @NotNull
    private Boolean halfBoard;

    private Double priceHalfBoard;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "config_id")
    private List<UiImage> images;

}