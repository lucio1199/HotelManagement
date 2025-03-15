package at.ac.tuwien.sepr.groupphase.backend.datagenerator;

import at.ac.tuwien.sepr.groupphase.backend.entity.Activity;
import at.ac.tuwien.sepr.groupphase.backend.entity.ActivitySlot;
import at.ac.tuwien.sepr.groupphase.backend.entity.ActivityTimeslotInfo;
import at.ac.tuwien.sepr.groupphase.backend.repository.ActivityRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.ActivityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ActivityDataGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final int NUMBER_OF_ACTIVITIES_TO_GENERATE = 19;
    private static final int TEST_ACTIVITY_CAPACITY = 1;
    private static final long TEST_ACTIVITY_PRICE = 5L;
    private static final LocalDateTime TEST_ACTIVITY_CREATED_AT = LocalDateTime.now();


    private final ActivityRepository activityRepository;
    private final Random random = new Random();

    private List<String> activityNames;
    private List<String> activityDescriptions;
    private List<String> activityCategories;


    public ActivityDataGenerator(ActivityRepository activityRepository, ActivityService activityService) {
        this.activityRepository = activityRepository;
        try {
            loadActivityData();
        } catch (IOException e) {
            LOGGER.error("Failed to load activity data from JSON files: {}", e.getMessage(), e);
        }
    }

    /**
     * Generates NUMBER_OF_ACTIVITIES_TO_GENERATE activities.
     */
    public void generateActivities() {
        LOGGER.debug("generating {} activity entries", NUMBER_OF_ACTIVITIES_TO_GENERATE);

        for (int i = 1; i <= NUMBER_OF_ACTIVITIES_TO_GENERATE; i++) {
            try {
                List<ActivityTimeslotInfo> timeslotInfos = getRandomTimeslots();
                byte[] imageBytes = getBytesFromImage("activity" + i + ".jpg");
                Activity activity = Activity.ActivityBuilder.aActivity()
                    .withName(getRandomActivityName())
                    .withDescription(getRandomActivityDescription())
                    .withCapacity(TEST_ACTIVITY_CAPACITY + random.nextInt(100))
                    .withPrice(TEST_ACTIVITY_PRICE + random.nextInt(100))
                    .withCreatedAt(TEST_ACTIVITY_CREATED_AT)
                    .withTimeslotInfos(timeslotInfos)
                    .withMainImage(imageBytes)
                    .withCategories(getRandomActivityCategory())
                    .build();

                List<ActivitySlot> timeslots = generateTimeslotsFromEntities(activity, timeslotInfos);
                activity.setActivityTimeslots(timeslots);
                LOGGER.debug("saving activity {}", activity.getName());
                activityRepository.save(activity);
            } catch (IOException | IllegalArgumentException ex) {
                LOGGER.error("Failed to load image for activity {}: {}", i, ex.getMessage(), ex);

                Activity activity = Activity.ActivityBuilder.aActivity()
                    .withName(getRandomActivityName())
                    .withDescription(getRandomActivityDescription())
                    .withCapacity(TEST_ACTIVITY_CAPACITY + random.nextInt(6))
                    .withPrice(TEST_ACTIVITY_PRICE + random.nextInt(100))
                    .withCreatedAt(TEST_ACTIVITY_CREATED_AT)
                    .withTimeslotInfos(getRandomTimeslots())
                    .withCategories(getRandomActivityCategory())
                    .build();

                LOGGER.warn("Saving activity {} without an image", activity.getName());
                activityRepository.save(activity);
            }
        }
        try {
            List<ActivityTimeslotInfo> timeslotInfos = getRandomTimeslots();
            byte[] imageBytes = getBytesFromImage("activity20.jpg");
            Activity activity = Activity.ActivityBuilder.aActivity()
                .withName("Arcade")
                .withDescription("Own a slot in the ultimate entertainment hub! Attracts all ages with vibrant lights, exciting games, and endless fun. Reserve now!"
                    + System.lineSeparator()
                    + "The room is located on the second floor of the hotel, near the elevator.")
                .withCapacity(70)
                .withPrice(12.0)
                .withCreatedAt(TEST_ACTIVITY_CREATED_AT)
                .withTimeslotInfos(timeslotInfos)
                .withMainImage(imageBytes)
                .withCategories("Kids")
                .build();

            List<ActivitySlot> timeslots = generateTimeslotsFromEntities(activity, timeslotInfos);
            activity.setActivityTimeslots(timeslots);
            LOGGER.debug("saving activity {}", activity.getName());
            activityRepository.save(activity);
        } catch (IOException | IllegalArgumentException ex) {
            LOGGER.error("Failed to load image for activity {}: {}", ex.getMessage(), ex);

            Activity activity = Activity.ActivityBuilder.aActivity()
                .withName("Arcade")
                .withDescription("Own a slot in the ultimate entertainment hub! Attracts all ages with vibrant lights, exciting games, and endless fun. Reserve now!"
                    + System.lineSeparator()
                    + "The room is located on the second floor of the hotel, near the elevator.")
                .withCapacity(70)
                .withPrice(12.0)
                .withCreatedAt(TEST_ACTIVITY_CREATED_AT)
                .withTimeslotInfos(getRandomTimeslots())
                .withCategories("Kids")
                .build();

            LOGGER.warn("Saving activity {} without an image", activity.getName());
            activityRepository.save(activity);
        }

    }

    private List<ActivityTimeslotInfo> getRandomTimeslots() {
        List<ActivityTimeslotInfo> timeslots = new ArrayList<>();
        LocalDate today = LocalDate.now();
        Random rnd = new Random(); // Um Zufallsgenerator nur einmal zu initialisieren

        // Generate a start time between 08:00 and 20:40
        LocalTime startTime = LocalTime.of(8 + rnd.nextInt(13), rnd.nextInt(3) * 20);

        // Generate a duration between 20 and 120 minutes, adjusted to end on 00, 20, or 40
        int duration = (1 + rnd.nextInt(6)) * 20; // Multiples of 20, between 20 and 120
        LocalTime endTime = startTime.plusMinutes(duration);

        // Ensure endTime does not exceed the day or become invalid
        if (endTime.isBefore(startTime)) {
            endTime = startTime.plusMinutes(80); // Default fixed duration if invalid
        }

        // Randomly decide whether the timeslot is weekly, daily, or a specific date
        int type = rnd.nextInt(3);

        switch (type) {
            case 0: // Daily (DayOfWeek)
                for (int i = 1; i <= 7; i++) {
                    timeslots.add(createTimeslotInfo(DayOfWeek.of(i), null, startTime, endTime));
                }
                break;
            case 1: // Specific date
                timeslots.add(createTimeslotInfo(null, today.plusDays(rnd.nextInt(30)), startTime, endTime));
                break;
            case 2: // Weekly (Repeating DayOfWeek starting today or later)
                Set<DayOfWeek> chosenDays = new HashSet<>();
                int count = 1 + rnd.nextInt(7);

                for (int i = 0; i < count; i++) {
                    DayOfWeek dayOfWeek;
                    do {
                        dayOfWeek = DayOfWeek.from(today.plusDays(1 + rnd.nextInt(7)));
                    } while (chosenDays.contains(dayOfWeek));

                    chosenDays.add(dayOfWeek);
                    timeslots.add(createTimeslotInfo(dayOfWeek, null, startTime, endTime));
                }
                break;
            default:
                timeslots.add(createTimeslotInfo(null, today.plusDays(rnd.nextInt(30)), startTime, endTime));
                break;
        }

        return timeslots;
    }


    public List<ActivitySlot> generateTimeslotsFromEntities(Activity activity, List<ActivityTimeslotInfo> timeslotEntities) {
        List<ActivitySlot> timeslots = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate sixMonthsFromNow = today.plusMonths(6);
        Random random = new Random(); // Random instance to generate random values

        for (ActivityTimeslotInfo entity : timeslotEntities) {
            if (entity.getSpecificDate() != null) {
                if (!entity.getSpecificDate().isBefore(today)) {
                    timeslots.add(createTimeslot(
                        activity,
                        activity.getCapacity(),
                        entity.getSpecificDate(),
                        entity.getStartTime(),
                        entity.getEndTime(),
                        random.nextInt(activity.getCapacity() + 1) // Random value for occupied
                    ));
                }
            } else if (entity.getDayOfWeek() != null) {
                LocalDate current = today;
                while (!current.isAfter(sixMonthsFromNow)) {
                    if (current.getDayOfWeek() == entity.getDayOfWeek()) {
                        timeslots.add(createTimeslot(
                            activity,
                            activity.getCapacity(),
                            current,
                            entity.getStartTime(),
                            entity.getEndTime(),
                            random.nextInt(activity.getCapacity() + 1) // Random value for occupied
                        ));
                    }
                    current = current.plusDays(1);
                }
            } else {
                LocalDate current = today;
                while (!current.isAfter(sixMonthsFromNow)) {
                    timeslots.add(createTimeslot(
                        activity,
                        activity.getCapacity(),
                        current,
                        entity.getStartTime(),
                        entity.getEndTime(),
                        random.nextInt(activity.getCapacity() + 1) // Random value for occupied
                    ));
                    current = current.plusDays(1);
                }
            }
        }

        return timeslots;
    }

    // Updated createTimeslot method to accept 'occupied' parameter
    private ActivitySlot createTimeslot(Activity activity, int capacity, LocalDate date, LocalTime startTime, LocalTime endTime, int occupied) {
        ActivitySlot slot = new ActivitySlot();
        slot.setActivity(activity);
        slot.setCapacity(capacity);
        slot.setDate(date);
        slot.setStartTime(startTime);
        slot.setEndTime(endTime);
        slot.setOccupied(occupied); // Set the random occupied value
        return slot;
    }

    private ActivityTimeslotInfo createTimeslotInfo(DayOfWeek dayOfWeek, LocalDate specificDate, LocalTime startTime, LocalTime endTime) {
        ActivityTimeslotInfo timeslot = new ActivityTimeslotInfo();
        timeslot.setDayOfWeek(dayOfWeek);
        timeslot.setSpecificDate(specificDate);
        timeslot.setStartTime(startTime);
        timeslot.setEndTime(endTime);
        return timeslot;
    }

    /**
     * Clears all existing activities and associated images from the database.
     */
    public void clearExistingActivities() {
        List<Activity> existingActivities = activityRepository.findAll();
        if (!existingActivities.isEmpty()) {
            LOGGER.debug("clearing {} existing activity entries", existingActivities.size());
            activityRepository.deleteAll();
        }
    }

    /**
     * Reads the image file from the resources folder and returns the image as a byte array.
     *
     * @param imagePath the path of the image
     * @return byte array of the image
     * @throws IOException if an error occurs during reading
     */
    private byte[] getBytesFromImage(String imagePath) throws IOException {
        ClassPathResource resource = new ClassPathResource(imagePath);

        if (!resource.exists()) {
            LOGGER.warn("Image file not found: {}", imagePath);
            return new byte[0];
        }

        try (InputStream inputStream = resource.getInputStream()) {
            return inputStream.readAllBytes();
        }
    }

    /**
     * Loads the possible activity names and descriptions into memory.
     *
     * @throws IOException If a file is corrupted.
     */
    private void loadActivityData() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        // Load room names
        try (InputStream activityNamesStream = new ClassPathResource("activity_names.json").getInputStream()) {
            activityNames = objectMapper.readValue(
                activityNamesStream,
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
        }

        // Load room descriptions
        try (InputStream activityDescriptionsStream = new ClassPathResource("activity_descriptions.json").getInputStream()) {
            activityDescriptions = objectMapper.readValue(
                activityDescriptionsStream,
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
        }

        // Load room descriptions
        try (InputStream activityCategoriesStream = new ClassPathResource("activity_categories.json").getInputStream()) {
            activityCategories = objectMapper.readValue(
                activityCategoriesStream,
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
        }

        LOGGER.debug("Loaded {} activity names and {} activity descriptions and {} activity categories", activityNames.size(), activityDescriptions.size(), activityCategories.size());
    }

    /**
     * Gets a random activity name.
     *
     * @return the activity name.
     */
    private String getRandomActivityName() {
        return activityNames.get(random.nextInt(activityNames.size()));
    }

    /**
     * Gets a random activity description.
     *
     * @return the activity description.
     */
    private String getRandomActivityDescription() {
        return activityDescriptions.get(random.nextInt(activityDescriptions.size()));
    }

    /**
     * Gets up to 3 random activity categories.
     *
     * @return the activity categories.
     */
    private String getRandomActivityCategory() {

        Collections.shuffle(activityCategories, new Random());

        // Get up to 3 random categories
        int numberOfCategories = Math.min(3, activityCategories.size());

        // Take the first 'numberOfCategories' items, join them with commas, and return as a string
        List<String> selectedCategories = activityCategories.subList(0, numberOfCategories);
        return selectedCategories.stream().collect(Collectors.joining(", "));
    }
}
