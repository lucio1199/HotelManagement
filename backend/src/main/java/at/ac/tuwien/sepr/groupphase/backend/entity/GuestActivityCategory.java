package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Data;

@Entity
@Data
public class GuestActivityCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "guest_id", nullable = false)
    private Guest guest;

    @Column(nullable = false)
    private Double education;

    @Column(nullable = false)
    private Double music;

    @Column(nullable = false)
    private Double fitness;

    @Column(nullable = false)
    private Double nature;

    @Column(nullable = false)
    private Double cooking;

    @Column(nullable = false)
    private Double teamwork;

    @Column(nullable = false)
    private Double creativity;

    @Column(nullable = false)
    private Double wellness;

    @Column(nullable = false)
    private Double recreation;

    @Column(nullable = false)
    private Double sports;

    @Column(nullable = false)
    private Double kids;

    @Column(nullable = false)
    private Double workshop;
}
