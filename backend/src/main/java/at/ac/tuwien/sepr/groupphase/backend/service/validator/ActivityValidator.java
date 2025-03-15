package at.ac.tuwien.sepr.groupphase.backend.service.validator;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ActivityCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ActivitySearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ActivitySlotSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ActivityUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ActivityBooking;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ActivityBookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class ActivityValidator {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final List<String> ALLOWED_IMAGE_TYPES = List.of("image/jpeg", "image/png");
    private static final long MAX_IMAGE_SIZE = 1024 * 1024 * 5;
    private final ActivityBookingRepository activityBookingRepository;

    public ActivityValidator(ActivityBookingRepository activityBookingRepository) {
        this.activityBookingRepository = activityBookingRepository;
    }

    /**
     * Validates the {@link ActivityCreateDto} before creating a new activity.
     * Ensures that all required fields and images meet validation criteria.
     *
     * @param activityCreateDto the {@link ActivityCreateDto} to be validated.
     * @param image             a {@link MultipartFile} representing the main image, which also needs to be validated.
     * @param additionalImages  the List of {@link MultipartFile} representing additional images for the activity.
     * @throws ValidationException if validation fails.
     */
    public void validateForCreate(ActivityCreateDto activityCreateDto, MultipartFile image, List<MultipartFile> additionalImages) throws ValidationException {
        LOG.trace("validateForCreate({})", activityCreateDto);
        List<String> validationErrors = new ArrayList<>();

        // Null Checks
        if (activityCreateDto.name() == null) {
            validationErrors.add("Name must not be null.");
        }
        if (activityCreateDto.description() == null) {
            validationErrors.add("Description must not be null.");
        }
        if (activityCreateDto.price() == null) {
            validationErrors.add("Price must not be null.");
        }
        if (activityCreateDto.capacity() == null) {
            validationErrors.add("Capacity must not be null.");
        }

        // Name Validation
        if (activityCreateDto.name() != null && (activityCreateDto.name().length() < 3 || activityCreateDto.name().length() > 100)) {
            validationErrors.add("Name must be between 3 and 100 characters.");
        }

        // Description Validation
        if (activityCreateDto.description() != null && activityCreateDto.description().length() > 1000) {
            validationErrors.add("Description must not exceed 1000 characters.");
        }

        // Price Validation
        if (activityCreateDto.price() != null && activityCreateDto.price() < 0) {
            validationErrors.add("Activity price must be a positive number.");
        } else if (activityCreateDto.price() != null && activityCreateDto.price() > 10000) {
            validationErrors.add("Activity price cannot exceed 10 000.");
        }

        // Capacity Validation
        if (activityCreateDto.capacity() != null && activityCreateDto.capacity() < 1) {
            validationErrors.add("Activity capacity must be at least 1.");
        } else if (activityCreateDto.capacity() != null && activityCreateDto.capacity() > 1000) {
            validationErrors.add("Activity capacity cannot exceed 1000.");
        }

        // Image Validation
        if (image != null) {
            if (!ALLOWED_IMAGE_TYPES.contains(image.getContentType())) {
                validationErrors.add("Invalid image type. Allowed types are: JPEG, PNG.");
            }
            if (image.getSize() > MAX_IMAGE_SIZE) {
                validationErrors.add("Image size exceeds the maximum limit of 5 MB.");
            }
        }

        // Additional Images Validation
        if (additionalImages != null) {
            for (MultipartFile additionalImage : additionalImages) {
                if (!ALLOWED_IMAGE_TYPES.contains(additionalImage.getContentType())) {
                    validationErrors.add("Invalid additional image type. Allowed types are: JPEG, PNG.");
                }
                if (additionalImage.getSize() > MAX_IMAGE_SIZE) {
                    validationErrors.add("Additional image size exceeds the maximum limit of 5 MB.");
                }
            }
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation failed for one or more fields.", validationErrors);
        }
    }

    /**
     * Validates the {@link ActivityUpdateDto} before updating an existing activity.
     * Ensures that required fields and updates adhere to validation rules.
     *
     * @param activityUpdateDto the {@link ActivityUpdateDto} to be validated.
     * @param image             a {@link MultipartFile} representing the main image.
     * @param additionalImages  the List of {@link MultipartFile} representing additional images for the activity.
     * @throws ValidationException if validation fails.
     */
    public void validateForUpdate(ActivityUpdateDto activityUpdateDto, MultipartFile image, List<MultipartFile> additionalImages) throws ValidationException {
        LOG.trace("validateForUpdate({})", activityUpdateDto);
        List<String> validationErrors = new ArrayList<>();

        // ID Validation
        if (activityUpdateDto.id() == null) {
            validationErrors.add("ID must not be null.");
        }

        // Name Validation
        if (activityUpdateDto.name() != null && (activityUpdateDto.name().length() < 3 || activityUpdateDto.name().length() > 100)) {
            validationErrors.add("Name must be between 3 and 100 characters.");
        }

        // Description Validation
        if (activityUpdateDto.description() != null && activityUpdateDto.description().length() > 1000) {
            validationErrors.add("Description must not exceed 1000 characters.");
        }

        // Price Validation
        if (activityUpdateDto.price() != null && activityUpdateDto.price() < 0) {
            validationErrors.add("Activity price must be a positive number.");
        } else if (activityUpdateDto.price() != null && activityUpdateDto.price() > 10000) {
            validationErrors.add("Activity price cannot exceed 10 000.");
        }

        // Capacity Validation
        if (activityUpdateDto.capacity() != null && activityUpdateDto.capacity() < 1) {
            validationErrors.add("Activity capacity must be at least 1.");
        } else if (activityUpdateDto.capacity() != null && activityUpdateDto.capacity() > 1000) {
            validationErrors.add("Activity capacity cannot exceed 1000.");
        }

        // Main Image Validation
        if (image != null) {
            if (!ALLOWED_IMAGE_TYPES.contains(image.getContentType())) {
                validationErrors.add("Invalid image type. Allowed types are: JPEG, PNG.");
            }
            if (image.getSize() > MAX_IMAGE_SIZE) {
                validationErrors.add("Image size exceeds the maximum limit of 5 MB.");
            }
        }

        // Additional Images Validation
        if (additionalImages != null) {
            for (MultipartFile additionalImage : additionalImages) {
                if (!ALLOWED_IMAGE_TYPES.contains(additionalImage.getContentType())) {
                    validationErrors.add("Invalid additional image type. Allowed types are: JPEG, PNG.");
                }
                if (additionalImage.getSize() > MAX_IMAGE_SIZE) {
                    validationErrors.add("Additional image size exceeds the maximum limit of 5 MB.");
                }
            }
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation failed for one or more fields.", validationErrors);
        }
    }

    /**
     * Validates the {@link ActivitySearchDto} to ensure valid search criteria.
     *
     * @param activitySearchDto the {@link ActivitySearchDto} to be validated.
     * @throws ValidationException if validation fails.
     */
    public void validateForSearch(ActivitySearchDto activitySearchDto) throws ValidationException {
        LOG.trace("validateForSearch({})", activitySearchDto);
        List<String> validationErrors = new ArrayList<>();

        if (activitySearchDto.minPrice() != null) {
            if (activitySearchDto.minPrice() < 0) {
                validationErrors.add("Minimum price must be greater than or equal to zero.");
            }
            if (activitySearchDto.minPrice() > 100000) {
                validationErrors.add("Minimum price cannot exceed 100,000.");
            }
        }

        if (activitySearchDto.maxPrice() != null) {
            if (activitySearchDto.maxPrice() < 0) {
                validationErrors.add("Maximum price must be greater than or equal to zero.");
            }
            if (activitySearchDto.maxPrice() > 100000) {
                validationErrors.add("Maximum price cannot exceed 100,000.");
            }
        }

        if (activitySearchDto.minPrice() != null && activitySearchDto.maxPrice() != null) {
            if (activitySearchDto.minPrice() > activitySearchDto.maxPrice()) {
                validationErrors.add("Minimum price must be less than or equal to maximum price.");
            }
        }

        if (activitySearchDto.capacity() != null) {
            if (activitySearchDto.capacity() <= 0) {
                validationErrors.add("Capacity must be greater than zero.");
            }
            if (activitySearchDto.capacity() > 100) {
                validationErrors.add("Capacity cannot exceed 100.");
            }
        }

        if (activitySearchDto.date() != null) {
            if (activitySearchDto.date().isBefore(LocalDate.now())) {
                validationErrors.add("Selected date cannot be in the past.");
            }
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation failed for one or more fields.", validationErrors);
        }
    }

    public void validateForDelete(Long id) throws ValidationException {
        LOG.trace("validateForDelete({})", id);
        List<String> validationErrors = new ArrayList<>();

        if (id == null) {
            validationErrors.add("ID must not be null.");
        }

        List<ActivityBooking> bookings = activityBookingRepository.findByActivityId(id);

        if (!bookings.isEmpty()) {
            validationErrors.add("Activity has associated bookings and cannot be deleted.");
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation failed for Activity deletion.", validationErrors);
        }
    }

    public void validateForTimeslotsFilter(ActivitySlotSearchDto searchDto, Long activityId) throws ValidationException {
        LOG.trace("validateForTimeslotsFilter({}, {})", searchDto, activityId);
        List<String> validationErrors = new ArrayList<>();

        if (activityId == null) {
            validationErrors.add("Activity ID must not be null.");
        }

        if (searchDto.date() != null && searchDto.date().isBefore(LocalDate.now())) {
            validationErrors.add("Selected date cannot be in the past.");
        }

        if (searchDto.participants() != null && searchDto.participants() <= 0) {
            validationErrors.add("Number of participants must be greater than zero.");
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation failed for Activity timeslot filtering.", validationErrors);
        }
    }

}
