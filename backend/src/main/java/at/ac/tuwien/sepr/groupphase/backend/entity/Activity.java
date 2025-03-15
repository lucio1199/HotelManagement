package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Activity {

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

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "activity_id")
    private List<ActivityImage> additionalImages;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "activity_id")
    private List<ActivitySlot> activityTimeslots;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "activity_id")
    private List<ActivityTimeslotInfo> activityTimeslotInfos;

    @Lob
    private byte[] mainImage;

    @Column(nullable = false, length = 1000)
    private String categories;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Activity activity = (Activity) o;
        return id == activity.id
            && Objects.equals(name, activity.name)
            && Objects.equals(description, activity.description)
            && Objects.equals(mainImage, activity.mainImage)
            && price == activity.price;
    }

    public void setActivityTimeslots(List<ActivitySlot> timeslots) {
        if (this.activityTimeslots != null) {
            this.activityTimeslots.clear();
            if (timeslots != null) {
                this.activityTimeslots.addAll(timeslots);
            }
        } else {
            this.activityTimeslots = timeslots;
        }
    }

    public void setActivityTimeslotInfos(List<ActivityTimeslotInfo> timeslots) {
        if (this.activityTimeslotInfos != null) {
            this.activityTimeslotInfos.clear();
            if (timeslots != null) {
                this.activityTimeslotInfos.addAll(timeslots);
            }
        } else {
            this.activityTimeslotInfos = timeslots;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, mainImage, price);
    }

    public static final class ActivityBuilder {
        private Long id;
        private String name;
        private int capacity;
        private double price;
        private String description;
        private byte[] mainImage;
        private LocalDateTime createdAt;
        private List<ActivitySlot> timeslots;
        private List<ActivityTimeslotInfo> timeslotInfos;
        private String categories;


        private ActivityBuilder() {
        }

        public static Activity.ActivityBuilder aActivity() {
            return new Activity.ActivityBuilder();
        }

        public Activity.ActivityBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public Activity.ActivityBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public Activity.ActivityBuilder withCapacity(int capacity) {
            this.capacity = capacity;
            return this;
        }

        public Activity.ActivityBuilder withPrice(double price) {
            this.price = price;
            return this;
        }

        public Activity.ActivityBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Activity.ActivityBuilder withMainImage(byte[] mainImage) {
            this.mainImage = mainImage;
            return this;
        }

        public Activity.ActivityBuilder withCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Activity.ActivityBuilder withTimeslots(List<ActivitySlot> timeslots) {
            this.timeslots = timeslots;
            return this;
        }

        public Activity.ActivityBuilder withCategories(String categories) {
            this.categories = categories;
            return this;
        }

        public Activity.ActivityBuilder withTimeslotInfos(List<ActivityTimeslotInfo> timeslots) {
            this.timeslotInfos = timeslots;
            return this;
        }

        public Activity build() {
            Activity activity = new Activity();
            activity.setId(id);
            activity.setName(name);
            activity.setCapacity(capacity);
            activity.setPrice(price);
            activity.setDescription(description);
            activity.setMainImage(mainImage);
            activity.setActivityTimeslots(timeslots);
            activity.setActivityTimeslotInfos(timeslotInfos);
            activity.setCategories(categories);
            return activity;
        }
    }

    public String getMainImageAsString() {
        return Base64.getEncoder().encodeToString(mainImage);
    }

}
