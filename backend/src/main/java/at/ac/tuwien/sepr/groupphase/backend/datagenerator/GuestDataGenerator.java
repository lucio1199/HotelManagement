package at.ac.tuwien.sepr.groupphase.backend.datagenerator;

import at.ac.tuwien.sepr.groupphase.backend.entity.Guest;
import at.ac.tuwien.sepr.groupphase.backend.entity.GuestActivityCategory;
import at.ac.tuwien.sepr.groupphase.backend.enums.Gender;
import at.ac.tuwien.sepr.groupphase.backend.enums.RoleType;

import at.ac.tuwien.sepr.groupphase.backend.enums.Nationality;
import at.ac.tuwien.sepr.groupphase.backend.repository.GuestActivityCategoryRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.GuestRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class GuestDataGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(GuestDataGenerator.class);
    private static final int NUMBER_OF_GUESTS_TO_GENERATE = 100;

    private final GuestRepository guestRepository;
    private final PasswordEncoder passwordEncoder;
    private final Random random = new Random();

    private List<String> firstNames;
    private List<String> lastNames;
    private List<String> cities;
    private List<String> addresses;
    private final GuestActivityCategoryRepository guestActivityCategoryRepository;


    public GuestDataGenerator(GuestRepository guestRepository, PasswordEncoder passwordEncoder, GuestActivityCategoryRepository guestActivityCategoryRepository) {
        this.guestRepository = guestRepository;
        this.passwordEncoder = passwordEncoder;
        this.guestActivityCategoryRepository = guestActivityCategoryRepository;

        try {
            loadGuestData();
        } catch (IOException e) {
            LOGGER.error("Failed to load guest data from JSON files: {}", e.getMessage(), e);
        }
    }

    /**
     * Generates NUMBER_OF_GUESTS_TO_GENERATE guest accounts.
     */
    public void generateGuests() {
        LOGGER.debug("Generating {} guest entries", NUMBER_OF_GUESTS_TO_GENERATE);

        for (int i = 1; i <= 10; i++) {
            Guest guest = new Guest();

            guest.setEmail("guest" + i + "@example.com");
            guest.setPassword(passwordEncoder.encode("securePassword" + i));
            guest.setRoleType(RoleType.ROLE_GUEST);
            guest.setVerified(true);
            guest.setPhoneNumber(generatePhoneNumber());

            guest.setFirstName(getRandomFirstName());
            guest.setLastName(getRandomLastName());
            guest.setDateOfBirth(LocalDate.parse(getRandomBirthDate()));
            guest.setPlaceOfBirth(getRandomCity());
            guest.setGender(random.nextInt(2) % 2 == 0 ? Gender.MALE : Gender.FEMALE);
            guest.setNationality(Nationality.values()[random.nextInt(Nationality.values().length)]);
            guest.setAddress(getRandomAddress());
            guest.setPassportNumber(generatePassportNumber());

            LOGGER.debug("Saving guest: {}", guest.getEmail());
            guestRepository.save(guest);

            generateGuestActivityCategoriesWeights(guest);
        }

        for (int i = 1; i <= NUMBER_OF_GUESTS_TO_GENERATE; i++) {
            Guest guest = new Guest();

            guest.setPassword(passwordEncoder.encode("securePassword"));
            guest.setRoleType(RoleType.ROLE_GUEST);
            guest.setVerified(true);
            guest.setPhoneNumber(generatePhoneNumber());

            guest.setFirstName(getRandomFirstName());
            guest.setLastName(getRandomLastName());
            guest.setDateOfBirth(LocalDate.parse(getRandomBirthDate()));
            guest.setPlaceOfBirth(getRandomCity());
            guest.setGender(random.nextInt(2) % 2 == 0 ? Gender.MALE : Gender.FEMALE);
            guest.setNationality(Nationality.values()[random.nextInt(Nationality.values().length)]);
            guest.setAddress(getRandomAddress());
            guest.setPassportNumber(generatePassportNumber());
            guest.setEmail(getRandomEmail(guest));

            LOGGER.debug("Saving guest: {}", guest.getEmail());
            guestRepository.save(guest);

            generateGuestActivityCategoriesWeights(guest);
        }
    }

    /**
     * Clears all existing guests from the database.
     */
    public void clearExistingGuests() {
        LOGGER.debug("Clearing all users");
        guestRepository.deleteAll();
    }

    /**
     * Loads the possible guest first and last names, cities and addresses into memory.
     *
     * @throws IOException If a file is corrupted.
     */
    private void loadGuestData() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        // Load first names
        try (InputStream firstNamesStream = new ClassPathResource("first_names.json").getInputStream()) {
            firstNames = objectMapper.readValue(
                firstNamesStream,
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
        }

        // Load last names
        try (InputStream lastNamesStream = new ClassPathResource("last_names.json").getInputStream()) {
            lastNames = objectMapper.readValue(
                lastNamesStream,
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
        }

        // Load cities
        try (InputStream cityStream = new ClassPathResource("city_names.json").getInputStream()) {
            cities = objectMapper.readValue(
                cityStream,
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
        }

        // Load addresses
        try (InputStream addressStream = new ClassPathResource("guest_addresses.json").getInputStream()) {
            addresses = objectMapper.readValue(
                addressStream,
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
        }

        LOGGER.debug("Loaded {} first names, {} last names, {} cities and {} addresses", firstNames.size(), lastNames.size(), cities.size(), addresses.size());
    }

    /**
     * Gets a random first name.
     *
     * @return The first name.
     */
    private String getRandomFirstName() {
        return firstNames.get(random.nextInt(firstNames.size()));
    }

    /**
     * Gets a random last name.
     *
     * @return The last name.
     */
    private String getRandomLastName() {
        return lastNames.get(random.nextInt(lastNames.size()));
    }

    /**
     * Generates a random birthdate.
     *
     * @return The generated birthdate.
     */
    private static String getRandomBirthDate() {
        // Current date
        LocalDate today = LocalDate.now();

        // Calculate the range of years
        LocalDate maxDate = today.minusYears(18); // 18 years ago
        LocalDate minDate = today.minusYears(120); // 120 years ago

        // Generate a random day in the range
        long randomDay = ThreadLocalRandom.current()
            .nextLong(minDate.toEpochDay(), maxDate.toEpochDay());

        // Convert the random day to a LocalDate
        LocalDate randomDate = LocalDate.ofEpochDay(randomDay);

        // Format the date as "YYYY-MM-DD"
        return randomDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * Gets a random city.
     *
     * @return The city.
     */
    private String getRandomCity() {
        return cities.get(random.nextInt(cities.size()));
    }

    /**
     * Gets a random address.
     *
     * @return The address.
     */
    private String getRandomAddress() {
        return addresses.get(random.nextInt(addresses.size()));
    }

    /**
     * Generates a random passport number.
     *
     * @return The passport number.
     */
    private String generatePassportNumber() {
        // The passport number typically has 9 characters: 2 letters followed by 7 digits.
        StringBuilder passportNumber = new StringBuilder();

        // Generate the 2 uppercase letters
        for (int i = 0; i < 2; i++) {
            char randomLetter = (char) (random.nextInt(26) + 'A'); // Random uppercase letter A-Z
            passportNumber.append(randomLetter);
        }

        // Generate the 7 digits
        for (int i = 0; i < 7; i++) {
            int randomDigit = random.nextInt(10); // Random digit 0-9
            passportNumber.append(randomDigit);
        }

        return passportNumber.toString();
    }

    /**
     * Generates a random phone number.
     *
     * @return The phone number.
     */
    private String generatePhoneNumber() {
        // We decide if we want a phone number with or without a country code
        boolean includeCountryCode = random.nextBoolean(); // Randomly decide if we include country code

        StringBuilder phoneNumber = new StringBuilder();

        if (includeCountryCode) {
            // Generate a random country code (between +1 and +999, so 1 to 3 digits long)
            int countryCode = random.nextInt(999) + 1; // Random country code between +1 and +999
            phoneNumber.append("+").append(countryCode);
        }

        // Generate the rest of the phone number (between 1 and 19 digits if there's a country code, up to 20 without it)
        int remainingLength = 20 - phoneNumber.length();
        if (remainingLength > 0) {
            // Append random digits for the rest of the number (max of remainingLength digits)
            for (int i = 0; i < remainingLength; i++) {
                phoneNumber.append(random.nextInt(10)); // Random digit from 0 to 9
            }
        }

        return phoneNumber.toString();
    }

    /**
     * Generates category weights for guest activity preferences.
     *
     * @param guest the guest activity categories weights to generate
     */
    public void generateGuestActivityCategoriesWeights(Guest guest) {
        LOGGER.debug("Generating {} guest activity category entries", guest);

        GuestActivityCategory guestActivityCategory = new GuestActivityCategory();
        guestActivityCategory.setGuest(guest);

        guestActivityCategory.setEducation(0.0);
        guestActivityCategory.setMusic(0.0);
        guestActivityCategory.setFitness(0.0);
        guestActivityCategory.setNature(0.0);
        guestActivityCategory.setCooking(0.0);
        guestActivityCategory.setTeamwork(0.0);
        guestActivityCategory.setCreativity(0.0);
        guestActivityCategory.setWellness(0.0);
        guestActivityCategory.setRecreation(0.0);
        guestActivityCategory.setSports(0.0);
        guestActivityCategory.setKids(0.0);
        guestActivityCategory.setWorkshop(0.0);

        // Save the generated entity
        LOGGER.debug("Saving guest activity category for Guest {}", guestActivityCategory.getGuest().getId());
        guestActivityCategoryRepository.save(guestActivityCategory);

    }


    /**
     * Gets a random email.
     *
     * @param guest The guest that owns the email.
     * @return The email.
     */
    private String getRandomEmail(Guest guest) {
        String emailSuffix = "@gmail.com";
        if (random.nextInt(7) == 6) {
            emailSuffix = "@yahoo.com";
        } else if (random.nextInt(5) == 4) {
            emailSuffix = "@icloud.com";
        } else if (random.nextInt(3) == 2) {
            emailSuffix = "@gmx.at";
        }

        if (random.nextInt(10) % 2 == 0) {
            return guest.getFirstName() + "." + guest.getLastName() + random.nextInt(1000) + emailSuffix;
        } else {
            return guest.getFirstName() + random.nextInt(1000) + guest.getLastName() + emailSuffix;
        }
    }

}
