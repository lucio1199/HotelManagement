package at.ac.tuwien.sepr.groupphase.backend.datagenerator;

import at.ac.tuwien.sepr.groupphase.backend.entity.Room;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoomRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class RoomDataGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final int NUMBER_OF_ROOMS_TO_GENERATE = 10;
    private static final int TEST_ROOM_CAPACITY = 1;
    private static final long TEST_ROOM_PRICE = 100L;
    private static final LocalDateTime TEST_ROOM_CREATED_AT = LocalDateTime.now();
    private static final LocalDateTime TEST_ROOM_LAST_CLEANED_AT = LocalDateTime.now();


    private final RoomRepository roomRepository;
    private final Random random = new Random();

    private List<String> roomNames;
    private List<String> roomDescriptions;

    public RoomDataGenerator(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
        try {
            loadRoomData();
        } catch (IOException e) {
            LOGGER.error("Failed to load room data from JSON files: {}", e.getMessage(), e);
        }
    }

    /**
     * Generates NUMBER_OF_ROOMS_TO_GENERATE rooms.
     */
    public void generateRooms() {
        LOGGER.debug("generating {} room entries", NUMBER_OF_ROOMS_TO_GENERATE);

        for (int i = 1; i <= NUMBER_OF_ROOMS_TO_GENERATE; i++) {
            try {
                byte[] imageBytes = getBytesFromImage("room" + i + ".jpg");
                Room room = Room.RoomBuilder.aRoom()
                    .withName(getRandomRoomName())
                    .withDescription(getRandomRoomDescription())
                    .withCapacity(TEST_ROOM_CAPACITY + random.nextInt(6))
                    .withPrice(TEST_ROOM_PRICE + random.nextInt(1000))
                    .withCreatedAt(TEST_ROOM_CREATED_AT)
                    .withLastCleanedAt(getRandomDateWithinLast30Days())
                    .withMainImage(imageBytes)
                    .build();
                LOGGER.debug("saving room {}", room.getName());
                roomRepository.save(room);
            } catch (IOException | IllegalArgumentException ex) {
                LOGGER.error("Failed to load image for room {}: {}", i, ex.getMessage(), ex);

                Room room = Room.RoomBuilder.aRoom()
                    .withName(getRandomRoomName())
                    .withDescription(getRandomRoomDescription())
                    .withCapacity(TEST_ROOM_CAPACITY + random.nextInt(6))
                    .withPrice(TEST_ROOM_PRICE + random.nextInt(1000))
                    .withCreatedAt(TEST_ROOM_CREATED_AT)
                    .withLastCleanedAt(TEST_ROOM_LAST_CLEANED_AT)
                    .build();

                LOGGER.warn("Saving room {} without an image", room.getName());
                roomRepository.save(room);
            }
        }
    }

    /**
     * Clears all existing rooms and associated images from the database.
     */
    public void clearExistingRooms() {
        List<Room> existingRooms = roomRepository.findAll();
        if (!existingRooms.isEmpty()) {
            LOGGER.debug("clearing {} existing room entries", existingRooms.size());
            roomRepository.deleteAll();
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
     * Loads the possible room names and descriptions into memory.
     *
     * @throws IOException If a file is corrupted.
     */
    private void loadRoomData() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        // Load room names
        try (InputStream roomNamesStream = new ClassPathResource("room_names.json").getInputStream()) {
            roomNames = objectMapper.readValue(
                roomNamesStream,
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
        }

        // Load room descriptions
        try (InputStream roomDescriptionsStream = new ClassPathResource("room_descriptions.json").getInputStream()) {
            roomDescriptions = objectMapper.readValue(
                roomDescriptionsStream,
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
        }

        LOGGER.debug("Loaded {} room names and {} room descriptions", roomNames.size(), roomDescriptions.size());
    }

    /**
     * Gets a random room name.
     *
     * @return the room name.
     */
    private String getRandomRoomName() {
        return roomNames.get(random.nextInt(roomNames.size()));
    }

    /**
     * Gets a random room description.
     *
     * @return the room description.
     */
    private String getRandomRoomDescription() {
        return roomDescriptions.get(random.nextInt(roomDescriptions.size()));
    }

    public static LocalDateTime getRandomDateWithinLast30Days() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyDaysAgo = now.minusDays(7);

        long start = thirtyDaysAgo.toEpochSecond(java.time.ZoneOffset.UTC);
        long end = now.toEpochSecond(java.time.ZoneOffset.UTC);

        long randomEpochSecond = ThreadLocalRandom.current().nextLong(start, end);
        return LocalDateTime.ofEpochSecond(randomEpochSecond, 0, java.time.ZoneOffset.UTC);
    }
}
