package at.ac.tuwien.sepr.groupphase.backend.service.validator;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RoomAdminSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RoomCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RoomSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RoomUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RoomCleaningTimeDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class RoomValidator {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final List<String> ALLOWED_IMAGE_TYPES = List.of("image/jpeg", "image/png");
    private static final long MAX_IMAGE_SIZE = 1024 * 1024 * 5;


    /**
     * Validates the {@link RoomCreateDto} before creating a new room.
     * Ensures that all required fields are valid, including the main image.
     *
     * @param roomCreateDto    the {@link RoomCreateDto} to be validated.
     * @param image            a {@link MultipartFile} representing the main image, which also needs to be validated.
     * @param additionalImages the List of {@link MultipartFile} representing additional images for the room.
     * @throws ValidationException if the validation fails.
     */
    public void validateForCreate(RoomCreateDto roomCreateDto, MultipartFile image, List<MultipartFile> additionalImages) throws ValidationException {
        LOG.trace("validateForCreate({})", roomCreateDto);
        List<String> validationErrors = new ArrayList<>();

        // Null Checks
        if (roomCreateDto.name() == null) {
            validationErrors.add("Name must not be null.");
        }
        if (roomCreateDto.description() == null) {
            validationErrors.add("Description must not be null.");
        }
        if (roomCreateDto.price() == null) {
            validationErrors.add("Price must not be null.");
        }
        if (roomCreateDto.capacity() == null) {
            validationErrors.add("Capacity must not be null.");
        }

        // Name Validation
        if (roomCreateDto.name() != null && (roomCreateDto.name().length() < 3 || roomCreateDto.name().length() > 100)) {
            validationErrors.add("Name must be between 3 and 100 characters.");
        }

        // Description Validation
        if (roomCreateDto.description() != null && roomCreateDto.description().length() > 1000) {
            validationErrors.add("Description must not exceed 1000 characters.");
        }

        // Price Validation
        if (roomCreateDto.price() != null && roomCreateDto.price() < 0) {
            validationErrors.add("Room price must be a positive number.");
        } else if (roomCreateDto.price() != null && roomCreateDto.price() > 10000) {
            validationErrors.add("Room price cannot exceed 10 000.");
        }

        // Capacity Validation
        if (roomCreateDto.capacity() != null && roomCreateDto.capacity() < 1) {
            validationErrors.add("Room capacity must be at least 1.");
        } else if (roomCreateDto.capacity() != null && roomCreateDto.capacity() > 10) {
            validationErrors.add("Room capacity cannot exceed 10.");
        }

        // Image Validation
        if (image != null) {
            if (!ALLOWED_IMAGE_TYPES.contains(image.getContentType())) {
                validationErrors.add("Invalid image type. Allowed types are: JPEG, PNG.");
            }
            if (image.getSize() > MAX_IMAGE_SIZE) {
                validationErrors.add("Image size exceeds the maximum limit of 1 MB.");
            }
        }

        // Additional Images Validation
        if (additionalImages != null) {
            for (MultipartFile additionalImage : additionalImages) {
                if (!ALLOWED_IMAGE_TYPES.contains(additionalImage.getContentType())) {
                    validationErrors.add("Invalid additional image type. Allowed types are: JPEG, PNG.");
                }
                if (additionalImage.getSize() > MAX_IMAGE_SIZE) {
                    validationErrors.add("Additional image size exceeds the maximum limit of 1 MB.");
                }
            }
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation failed for one or more fields.", validationErrors);
        }
    }


    /**
     * Validates the {@link RoomUpdateDto} before updating an existing room.
     * This method ensures that all fields are manually validated.
     *
     * @param roomUpdateDto    the {@link RoomUpdateDto} to be validated.
     * @param image            a {@link MultipartFile} representing the main image, which also needs to be validated.
     * @param additionalImages the List of {@link MultipartFile} representing additional images for the room.
     * @throws ValidationException if the validation fails.
     */
    public void validateForUpdate(RoomUpdateDto roomUpdateDto, MultipartFile image, List<MultipartFile> additionalImages) throws ValidationException {
        LOG.trace("validateForUpdate({})", roomUpdateDto);
        List<String> validationErrors = new ArrayList<>();

        // ID Validation
        if (roomUpdateDto.id() == null) {
            validationErrors.add("ID must not be null.");
        }

        // Name Validation
        if (roomUpdateDto.name() != null && (roomUpdateDto.name().length() < 3 || roomUpdateDto.name().length() > 100)) {
            validationErrors.add("Name must be between 3 and 100 characters.");
        }

        // Description Validation
        if (roomUpdateDto.description() != null && roomUpdateDto.description().length() > 1000) {
            validationErrors.add("Description must not exceed 1000 characters.");
        }

        // Price Validation
        if (roomUpdateDto.price() != null && roomUpdateDto.price() < 0) {
            validationErrors.add("Room price must be a positive number.");
        } /*else if (roomUpdateDto.price() != null && roomUpdateDto.price() > 10000) {
            validationErrors.add("Room price cannot exceed 10 000.");
        }*/

        // Capacity Validation
        if (roomUpdateDto.capacity() == null || roomUpdateDto.capacity() < 1) {
            validationErrors.add("Room capacity must be at least 1.");
        } else if (roomUpdateDto.capacity() != null && roomUpdateDto.capacity() > 10) {
            validationErrors.add("Room capacity cannot exceed 10.");
        }

        // Main Image Validation
        if (image != null) {
            if (!ALLOWED_IMAGE_TYPES.contains(image.getContentType())) {
                validationErrors.add("Invalid image type. Allowed types are: JPEG, PNG.");
            }
            if (image.getSize() > MAX_IMAGE_SIZE) {
                validationErrors.add("Image size exceeds the maximum limit of 1 MB.");
            }
        }

        // Additional Images Validation
        if (additionalImages != null) {
            for (MultipartFile additionalImage : additionalImages) {
                if (!ALLOWED_IMAGE_TYPES.contains(additionalImage.getContentType())) {
                    validationErrors.add("Invalid additional image type. Allowed types are: JPEG, PNG.");
                }
                if (additionalImage.getSize() > MAX_IMAGE_SIZE) {
                    validationErrors.add("Additional image size exceeds the maximum limit of 1 MB.");
                }
            }
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation failed for one or more fields.", validationErrors);
        }
    }

    public List<String> validateForSearch(RoomSearchDto roomSearchDto) throws ValidationException {
        List<String> validationErrors = new ArrayList<>();

        if (roomSearchDto.minPrice() != null && roomSearchDto.minPrice() < 1) {
            validationErrors.add("Minimum price must be greater than zero.");
        } else if (roomSearchDto.minPrice() != null && roomSearchDto.minPrice() > 10000) {
            validationErrors.add("Minimum price cannot exceed 10000.");
        }

        if (roomSearchDto.maxPrice() != null && roomSearchDto.maxPrice() < 1) {
            validationErrors.add("Maximum price must be greater than zero.");
        } else if (roomSearchDto.maxPrice() != null && roomSearchDto.maxPrice() > 10000) {
            validationErrors.add("Maximum price cannot exceed 10000.");
        }

        if (roomSearchDto.minPrice() != null && roomSearchDto.maxPrice() != null && roomSearchDto.minPrice() > roomSearchDto.maxPrice()) {
            validationErrors.add("Minimum price must be less than maximum price.");
        }

        if (roomSearchDto.startDate() == null || roomSearchDto.endDate() == null) {
            validationErrors.add("Both start- and end-date must be given");
        }

        if (roomSearchDto.startDate() != null && roomSearchDto.endDate() != null) {
            if (roomSearchDto.startDate().isBefore(LocalDate.now())) {
                validationErrors.add("Start date must not be in the past.");
            }
            if (roomSearchDto.endDate().isBefore(LocalDate.now())) {
                validationErrors.add("End date must not be in the past.");
            }

            if (roomSearchDto.startDate().isAfter(roomSearchDto.endDate())) {
                validationErrors.add("Start date must be before the end date.");
            }

            if (roomSearchDto.startDate().isEqual(roomSearchDto.endDate())) {
                validationErrors.add("Booking period must be at least 1 day.");
            }

            LocalDate today = LocalDate.now();
            if (roomSearchDto.endDate().isAfter(today.plusYears(1))) {
                validationErrors.add("The booking period cannot exceed one year from today.");
            }
        }

        if (roomSearchDto.capacity() != null) {
            if (roomSearchDto.capacity() < 1) {
                validationErrors.add("Guests must be greater than 0.");
            }
            if (roomSearchDto.capacity() > 6) {
                validationErrors.add("Guests must not exceed 6.");
            }
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation failed for search parameters", validationErrors);
        }
        return validationErrors;
    }

    public void validateForAdminSearch(RoomAdminSearchDto roomSearchDto) throws ValidationException {
        LOG.trace("validateForAdminSearch({})", roomSearchDto);
        List<String> validationErrors = new ArrayList<>();

        if (roomSearchDto.minPrice() != null && roomSearchDto.minPrice() < 0) {
            validationErrors.add("Minimum price must be greater than zero");
        }

        if (roomSearchDto.maxPrice() != null && roomSearchDto.maxPrice() < 0) {
            validationErrors.add("Maximum price must be greater than zero");
        }

        if (roomSearchDto.minPrice() != null && roomSearchDto.maxPrice() != null && roomSearchDto.minPrice() > roomSearchDto.maxPrice()) {
            validationErrors.add("Minimum price must be less than maximum price");
        }

        if (roomSearchDto.minCapacity() != null && roomSearchDto.minCapacity() <= 0) {
            validationErrors.add("Capacity must be greater than zero");
        }

        if (roomSearchDto.maxCapacity() != null && roomSearchDto.maxCapacity() <= 0) {
            validationErrors.add("Capacity must be greater than zero");
        }

        if (roomSearchDto.minCapacity() != null && roomSearchDto.maxCapacity() != null && roomSearchDto.minCapacity() > roomSearchDto.maxCapacity()) {
            validationErrors.add("Minimum capacity must be less than maximum capacity");
        }
    }

    public void validateForCleaningTimes(RoomCleaningTimeDto roomCleaningTimeDto) throws ValidationException {
        LOG.trace("validateForCleaningTimes({})", roomCleaningTimeDto);
        List<String> validationErrors = new ArrayList<>();

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime fromTime = LocalTime.parse(roomCleaningTimeDto.cleaningTimeFrom(), timeFormatter);
        LocalDateTime from = LocalDateTime.of(today, fromTime);
        LocalTime toTime = LocalTime.parse(roomCleaningTimeDto.cleaningTimeTo(), timeFormatter);
        LocalDateTime to = LocalDateTime.of(today, toTime);

        if (from.isAfter(to)) {
            validationErrors.add("From cannot be later than To");
        }
        if (from.isBefore(now)) {
            validationErrors.add("From cannot be earlier than now");
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation failed for one or more fields.", validationErrors);
        }
    }
}
