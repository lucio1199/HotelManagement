package at.ac.tuwien.sepr.groupphase.backend.datagenerator;

import at.ac.tuwien.sepr.groupphase.backend.entity.UiConfig;
import at.ac.tuwien.sepr.groupphase.backend.entity.UiImage;
import at.ac.tuwien.sepr.groupphase.backend.repository.UiConfigRepository;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates a UI configuration if there is no UI configuration found in the database.
 */
@Component
public class UiConfigDataGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final UiConfigRepository uiConfigRepository;
    private final Random random = new Random();

    private List<String> hotelNames;
    private List<String> hotelShortDescriptions;
    private List<String> hotelDescriptions;
    private List<String> hotelAddresses;

    public UiConfigDataGenerator(UiConfigRepository uiConfigRepository) {
        this.uiConfigRepository = uiConfigRepository;
        try {
            loadUiData();
        } catch (IOException e) {
            LOGGER.error("Failed to load room data from JSON files: {}", e.getMessage(), e);
        }
    }

    /**
     * Generates a UI configuration if there is no UI configuration found in the database.
     */
    public void generateUiConfig() {
        if (uiConfigRepository.count() == 0) {
            LOGGER.info("No UI configuration found in the database. Creating randomized configuration...");
            List<UiImage> defaultImages = new ArrayList<>();
            for (int i = 1; i <= 3; i++) {
                try {
                    byte[] imageBytes = getBytesFromImage("home" + i + ".jpg");
                    UiImage image = new UiImage(
                        null, // ID will be auto-generated
                        imageBytes,
                        "Default Alt Text for Image " + i, // Alt text
                        LocalDateTime.now(), // Created date
                        null // Config will be set later
                    );
                    defaultImages.add(image);
                } catch (IOException e) {
                    LOGGER.error("Failed to load image for uiConfig {}: {}", i, e.getMessage(), e);
                }
            }
            String hotelName = getRandomHotelName();
            UiConfig defaultConfig = new UiConfig(
                1L,
                hotelName,
                getRandomHotelShortDescription(),
                hotelName + " " + getRandomHotelDescription(),
                getRandomHotelAddress(),
                true,
                true,
                false,
                false,
                true,
                false,
                10.00,
                defaultImages
            );
            uiConfigRepository.save(defaultConfig);
            LOGGER.info("UI configuration created.");
        } else {
            LOGGER.info("UI configuration already exists. Skipping default initialization.");
        }
    }

    /**
     * Clears all existing UI configurations and associated images from the database.
     */
    public void clearExistingUiConfig() {
        List<UiConfig> existingUiConfigs = uiConfigRepository.findAll();
        if (!existingUiConfigs.isEmpty()) {
            LOGGER.info("Clearing existing UI configurations...");
            uiConfigRepository.deleteAll();
            LOGGER.info("All UI configurations and associated images have been cleared.");
        } else {
            LOGGER.info("No existing UI configurations found. Nothing to clear.");
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
     * Loads the possible hotel names, short descriptions, descriptions and addresses into memory.
     *
     * @throws IOException If a file is corrupted.
     */
    private void loadUiData() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        try (InputStream roomNamesStream = new ClassPathResource("hotel_names.json").getInputStream()) {
            hotelNames = objectMapper.readValue(
                roomNamesStream,
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
        }

        try (InputStream roomNamesStream = new ClassPathResource("hotel_short_descriptions.json").getInputStream()) {
            hotelShortDescriptions = objectMapper.readValue(
                roomNamesStream,
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
        }

        try (InputStream roomNamesStream = new ClassPathResource("hotel_descriptions.json").getInputStream()) {
            hotelDescriptions = objectMapper.readValue(
                roomNamesStream,
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
        }

        try (InputStream roomNamesStream = new ClassPathResource("hotel_addresses.json").getInputStream()) {
            hotelAddresses = objectMapper.readValue(
                roomNamesStream,
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
        }

        LOGGER.debug("Loaded {} hotel names, {} hotel short descriptions, {} hotel descriptions and {} hotel addresses", hotelNames.size(), hotelShortDescriptions.size(), hotelDescriptions.size(), hotelAddresses.size());
    }

    /**
     * Gets a random hotel name.
     *
     * @return the hotel name.
     */
    private String getRandomHotelName() {
        return hotelNames.get(random.nextInt(hotelNames.size()));
    }

    /**
     * Gets a random hotel short description.
     *
     * @return the hotel short description.
     */
    private String getRandomHotelShortDescription() {
        return hotelShortDescriptions.get(random.nextInt(hotelShortDescriptions.size()));
    }

    /**
     * Gets a random hotel description.
     *
     * @return the hotel description.
     */
    private String getRandomHotelDescription() {
        return hotelDescriptions.get(random.nextInt(hotelDescriptions.size()));
    }

    /**
     * Gets a random hotel address.
     *
     * @return the hotel address.
     */
    private String getRandomHotelAddress() {
        return hotelAddresses.get(random.nextInt(hotelAddresses.size()));
    }
}
