package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Min;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.util.Objects;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private double price;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private boolean halfBoard;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "room_id")
    private List<RoomImage> additionalImages;

    @CreatedDate
    @Column
    private LocalDateTime lastCleanedAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedDate
    @Column
    private LocalDateTime cleaningTimeFrom;

    @CreatedDate
    @Column
    private LocalDateTime cleaningTimeTo;

    @Lob
    private byte[] mainImage;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Room room = (Room) o;
        return id == room.id
            && Objects.equals(name, room.name)
            && Objects.equals(description, room.description)
            && Objects.equals(mainImage, room.mainImage)
            && price == room.price;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, mainImage, price);
    }

    public static final class RoomBuilder {
        private Long id;
        private String name;
        private int capacity;
        private double price;
        private String description;
        private byte[] mainImage;
        private LocalDateTime createdAt;
        private LocalDateTime lastCleanedAt;

        private RoomBuilder() {
        }

        public static RoomBuilder aRoom() {
            return new RoomBuilder();
        }

        public RoomBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public RoomBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public RoomBuilder withCapacity(int capacity) {
            this.capacity = capacity;
            return this;
        }

        public RoomBuilder withPrice(double price) {
            this.price = price;
            return this;
        }

        public RoomBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public RoomBuilder withMainImage(byte[] mainImage) {
            this.mainImage = mainImage;
            return this;
        }

        public RoomBuilder withCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public RoomBuilder withLastCleanedAt(LocalDateTime lastCleanedAt) {
            this.lastCleanedAt = lastCleanedAt;
            return this;
        }

        public Room build() {
            Room room = new Room();
            room.setId(id);
            room.setName(name);
            room.setCapacity(capacity);
            room.setPrice(price);
            room.setDescription(description);
            room.setMainImage(mainImage);
            room.setCreatedAt(createdAt);
            room.setLastCleanedAt(lastCleanedAt);
            return room;
        }
    }

    public String getMainImageAsString() {
        return Base64.getEncoder().encodeToString(mainImage);
    }
}
