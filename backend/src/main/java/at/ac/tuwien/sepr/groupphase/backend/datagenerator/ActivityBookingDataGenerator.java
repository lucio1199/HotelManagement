package at.ac.tuwien.sepr.groupphase.backend.datagenerator;

import at.ac.tuwien.sepr.groupphase.backend.repository.ActivityBookingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ActivityBookingDataGenerator {

    ActivityBookingRepository activityBookingRepository;

    public ActivityBookingDataGenerator(ActivityBookingRepository activityBookingRepository) {
        log.info("Generating activity bookings");
        this.activityBookingRepository = activityBookingRepository;
        generateBookings();
    }

    private void generateBookings() {
        log.info("Generating activity bookings");


    }
}
