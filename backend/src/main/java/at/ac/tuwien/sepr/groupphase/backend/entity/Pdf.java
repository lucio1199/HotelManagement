package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pdf_documents")
public class Pdf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long bookingId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private String documentType;

    @Lob
    @Column(nullable = false)
    private byte[] content;

    public Pdf(Long bookingId, String documentType, byte[] content, LocalDateTime createdAt) {
        this.bookingId = bookingId;
        this.createdAt = createdAt;
        this.documentType = documentType;
        this.content = content;
    }
}
