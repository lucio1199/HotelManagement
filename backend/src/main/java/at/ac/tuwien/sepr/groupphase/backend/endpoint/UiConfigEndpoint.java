package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DetailedUiConfigDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UiConfigHomepageDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UiConfigUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.service.UiConfigService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.lang.invoke.MethodHandles;
import java.util.List;

/**
 * REST endpoint for UI configuration.
 */
@RestController
@RequestMapping("api/v1/ui-config")
public class UiConfigEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final UiConfigService uiConfigService;

    public UiConfigEndpoint(UiConfigService uiConfigService) {
        this.uiConfigService = uiConfigService;
    }

    /**
     * Updates the UI configuration.
     *
     * @param id                the ID of the UI configuration
     * @param uiConfigUpdateDto the updated UI configuration
     * @param images            the images to update
     * @return the updated UI configuration
     * @throws ValidationException if the UI configuration is invalid
     */
    @Secured("ROLE_ADMIN")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public DetailedUiConfigDto update(
        @PathVariable("id") Long id,
        @Valid @ModelAttribute UiConfigUpdateDto uiConfigUpdateDto,
        @RequestPart(value = "images", required = false) List<MultipartFile> images) throws ValidationException {
        LOGGER.info("PUT /api/v1/ui-config/{}", id);
        return uiConfigService.update(id, uiConfigUpdateDto, images);
    }

    /**
     * Gets the UI configuration by ID.
     *
     * @param id the ID of the UI configuration
     * @return the UI configuration
     */
    @Secured("ROLE_ADMIN")
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public DetailedUiConfigDto getConfig(@PathVariable("id") Long id) {
        LOGGER.info("GET /api/v1/ui-config/{}", id);
        return uiConfigService.getConfigById(id);
    }

    /**
     * Gets the homepage configuration.
     *
     * @return the homepage configuration
     */
    @PermitAll
    @GetMapping("/homepage")
    @ResponseStatus(HttpStatus.OK)
    public UiConfigHomepageDto getHomepageConfig() {
        LOGGER.info("GET /api/v1/homepage");
        return uiConfigService.getHomepageConfig();
    }

    /**
     * Checks if a module is enabled.
     *
     * @param moduleName the name of the module
     * @return true if the module is enabled, false otherwise
     */
    @PermitAll
    @GetMapping("/module-enabled/{moduleName}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Boolean> isModuleEnabled(@PathVariable("moduleName") String moduleName) {
        LOGGER.info("GET /api/v1/ui-config/module-enabled/{}", moduleName);
        boolean isEnabled = uiConfigService.isModuleEnabled(moduleName);
        return ResponseEntity.ok(isEnabled);
    }
}
