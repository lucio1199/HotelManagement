package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Objects;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long smartLockId;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false) // Ensures a lock is always associated with a room
    private Room room;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Lock lock = (Lock) o;
        return Objects.equals(id, lock.id)
            && Objects.equals(smartLockId, lock.smartLockId)
            && Objects.equals(room, lock.room); // Added room comparison
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, smartLockId, room);
    }

    public static final class LockBuilder {
        private Long id;
        private Long smartLockId;
        private Room room;

        private LockBuilder() {
        }

        public static LockBuilder aLock() {
            return new LockBuilder();
        }

        public LockBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public LockBuilder withSmartLockId(Long smartLockId) {
            this.smartLockId = smartLockId;
            return this;
        }

        public LockBuilder withRoom(Room room) {
            this.room = room;
            return this;
        }

        public Lock build() {
            Lock lock = new Lock();
            lock.setId(id);
            lock.setSmartLockId(smartLockId);
            lock.setRoom(room);
            return lock;
        }
    }
}