package at.ac.tuwien.sepr.groupphase.backend.service.validator;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UiConfigUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

/**
 * Validates the {@link UiConfigUpdateDto} before updating the UI configuration.
 * Ensures that all required fields are valid, including images.
 */
@Component
public class UiConfigValidator {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final List<String> ALLOWED_IMAGE_TYPES = List.of("image/jpeg", "image/png");
    private static final long MAX_IMAGE_SIZE = 1024 * 1024;

    /**
     * Validates the {@link UiConfigUpdateDto} before updating the UI configuration.
     * Ensures that all required fields are valid, including images.
     *
     * @param uiConfigDto the {@link UiConfigUpdateDto} to be validated.
     * @throws ValidationException if the validation fails.
     */
    public void validateForUpdate(UiConfigUpdateDto uiConfigDto, List<MultipartFile> images) throws ValidationException {
        LOG.trace("validateForUpdate({})", uiConfigDto);
        List<String> validationErrors = new ArrayList<>();

        if (uiConfigDto.id() == null) {
            validationErrors.add("ID must not be null.");
        }

        if (uiConfigDto.hotelName() != null && (uiConfigDto.hotelName().length() < 3 || uiConfigDto.hotelName().length() > 100)) {
            validationErrors.add("Hotel name must be between 3 and 100 characters.");
        }

        if (uiConfigDto.descriptionShort() != null && (uiConfigDto.descriptionShort().length() < 3 || uiConfigDto.descriptionShort().length() > 100)) {
            validationErrors.add("Short description must be between 3 and 100 characters.");
        }

        if (uiConfigDto.description() != null && (uiConfigDto.description().length() < 3 || uiConfigDto.description().length() > 1000)) {
            validationErrors.add("Description must be between 3 and 1000 characters.");
        }

        if (uiConfigDto.address() != null && (uiConfigDto.address().length() < 3 || uiConfigDto.address().length() > 100)) {
            validationErrors.add("Address must be between 3 and 100 characters.");
        }
        if (uiConfigDto.address() != null && !(uiConfigDto.address().contains("Österreich") || uiConfigDto.address().contains("Austria"))) {
            validationErrors.add("Address must be in Austria.");
        }

        if (uiConfigDto.roomCleaning() == null) {
            validationErrors.add("Room cleaning preference must be specified.");
        }

        if (uiConfigDto.digitalCheckIn() == null) {
            validationErrors.add("Digital check-in preference must be specified.");
        }

        if (uiConfigDto.activities() == null) {
            validationErrors.add("Activities preference must be specified.");
        }

        if (uiConfigDto.communication() == null) {
            validationErrors.add("Communication preference must be specified.");
        }

        if (uiConfigDto.nuki() == null) {
            validationErrors.add("Nuki preference must be specified.");
        }

        if (uiConfigDto.halfBoard() == null) {
            validationErrors.add("Half-board preference must be specified.");

        } else {
            if (uiConfigDto.halfBoard()) {
                if (uiConfigDto.priceHalfBoard() == null || uiConfigDto.priceHalfBoard() < 0 || uiConfigDto.priceHalfBoard() > 10000) {
                    validationErrors.add("Half-board price must be a number between 0 and 10000.");
                }
            }
        }

        if (images != null) {
            for (MultipartFile image : images) {
                if (image == null) {
                    continue;
                }
                if (!ALLOWED_IMAGE_TYPES.contains(image.getContentType())) {
                    LOG.debug("Invalid image type: {}", image.getContentType());
                    validationErrors.add("Invalid image type. Allowed types are: JPEG, PNG.");
                }
                if (image.getSize() > MAX_IMAGE_SIZE) {
                    validationErrors.add("Image size exceeds the maximum limit of 1 MB.");
                }
            }
        }


        if (!validationErrors.isEmpty()) {
            LOG.debug("Validation errors: {}", validationErrors);
            throw new ValidationException("Validation failed for one or more fields.", validationErrors);
        }
    }
}