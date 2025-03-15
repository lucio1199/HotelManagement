package at.ac.tuwien.sepr.groupphase.backend.datagenerator;

import at.ac.tuwien.sepr.groupphase.backend.entity.Employee;
import at.ac.tuwien.sepr.groupphase.backend.entity.Guest;
import at.ac.tuwien.sepr.groupphase.backend.enums.Gender;
import at.ac.tuwien.sepr.groupphase.backend.enums.Nationality;
import at.ac.tuwien.sepr.groupphase.backend.enums.RoleType;
import at.ac.tuwien.sepr.groupphase.backend.repository.EmployeeRepository;
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

@Component
public class EmployeeDataGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeDataGenerator.class);
    private static final int NUMBER_OF_EMPLOYEES_TO_GENERATE = 10;

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final Random random = new Random();

    private List<String> firstNames;
    private List<String> lastNames;
    private List<String> emails;

    public EmployeeDataGenerator(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;

        try {
            loadEmployeeData();
        } catch (IOException e) {
            LOGGER.error("Failed to load guest data from JSON files: {}", e.getMessage(), e);
        }
    }

    /**
     * Generates employee accounts.
     */
    public void generateEmployees() {

        // Manager data generation
        Employee manager = new Employee();
        manager.setEmail("manager@example.com");
        manager.setPassword(passwordEncoder.encode("managerPassword"));
        manager.setRoleType(RoleType.ROLE_ADMIN);
        manager.setVerified(true);
        manager.setFirstName(getRandomFirstName());
        manager.setLastName(getRandomLastName());
        manager.setPhoneNumber(generatePhoneNumber());

        LOGGER.debug("Saving employee: {}", manager);
        employeeRepository.save(manager);

        // Cleaner data generation
        Employee cleaner = new Employee();
        cleaner.setEmail("cleaner@example.com");
        cleaner.setPassword(passwordEncoder.encode("cleanerPassword"));
        cleaner.setRoleType(RoleType.ROLE_CLEANING_STAFF);
        cleaner.setVerified(true);
        cleaner.setFirstName(getRandomFirstName());
        cleaner.setLastName(getRandomLastName());
        cleaner.setPhoneNumber(generatePhoneNumber());

        LOGGER.debug("Saving employee: {}", cleaner);
        employeeRepository.save(cleaner);

        // Receptionist data generation
        Employee receptionist = new Employee();
        receptionist.setEmail("receptionist@example.com");
        receptionist.setPassword(passwordEncoder.encode("receptionistPassword"));
        receptionist.setRoleType(RoleType.ROLE_RECEPTIONIST);
        receptionist.setVerified(true);
        receptionist.setFirstName(getRandomFirstName());
        receptionist.setLastName(getRandomLastName());
        receptionist.setPhoneNumber(generatePhoneNumber());

        LOGGER.debug("Saving employee: {}", receptionist);
        employeeRepository.save(receptionist);

        for (int i = 1; i <= NUMBER_OF_EMPLOYEES_TO_GENERATE; i++) {
            Employee employee = new Employee();
            employee.setEmail(getRandomEmail());
            employee.setPassword(passwordEncoder.encode("password"));
            employee.setRoleType(RoleType.values()[1 + random.nextInt(RoleType.values().length - 1)]);
            employee.setVerified(true);
            employee.setFirstName(getRandomFirstName());
            employee.setLastName(getRandomLastName());
            employee.setPhoneNumber(generatePhoneNumber());

            LOGGER.debug("Saving employee: {}", employee);
            employeeRepository.save(employee);
        }
    }

    /**
     * Clears all existing employees from the database.
     */
    public void clearExistingEmployees() {
        LOGGER.debug("Deleting all existing employees");
        employeeRepository.deleteAll();
    }

    /**
     * Loads the possible employee first names, last names and emails into memory.
     *
     * @throws IOException If a file is corrupted.
     */
    private void loadEmployeeData() throws IOException {
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

        // Load emails
        try (InputStream emailStream = new ClassPathResource("emails.json").getInputStream()) {
            emails = objectMapper.readValue(
                emailStream,
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
        }

        LOGGER.debug("Loaded {} first names, {} last names and {} emails", firstNames.size(), lastNames.size(), emails.size());
    }


    /**
     * Gets a random first name.
     *
     * @return the first name.
     */
    private String getRandomFirstName() {
        return firstNames.get(random.nextInt(firstNames.size()));
    }

    /**
     * Gets a random last name.
     *
     * @return the last name.
     */
    private String getRandomLastName() {
        return lastNames.get(random.nextInt(lastNames.size()));
    }

    /**
     * Gets a random email.
     *
     * @return the email.
     */
    private String getRandomEmail() {
        String email = emails.get(random.nextInt(emails.size()));
        emails.remove(email);
        return email;
    }

    /**
     * Generates a random phone number.
     *
     * @return the phone number.
     */
    private String generatePhoneNumber() {
        // Generate a random country code. For the sake of example, it could be between +1 and +999 (inclusive)
        int countryCode = random.nextInt(999) + 1; // Random country code between +1 and +999
        String countryCodeStr = "+" + countryCode;

        // Generate the first group of three digits
        String firstGroup = String.format("%03d", random.nextInt(1000)); // ensures 3 digits

        // Generate the second group of three digits
        String secondGroup = String.format("%03d", random.nextInt(1000)); // ensures 3 digits

        // Generate the third group of four digits
        String thirdGroup = String.format("%04d", random.nextInt(10000)); // ensures 4 digits

        // Concatenate and return the full phone number
        return countryCodeStr + " " + firstGroup + " " + secondGroup + " " + thirdGroup;
    }
}

