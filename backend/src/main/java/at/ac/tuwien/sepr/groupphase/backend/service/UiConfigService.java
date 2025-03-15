package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DetailedUiConfigDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UiConfigHomepageDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UiConfigUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service for managing UI-Configs.
 */

public interface UiConfigService {

    /**
     * Returns the UI-Config with the given id.
     *
     * @param id The id of the UI-Config to return.
     * @return The UI-Config with the given id.
     */
    DetailedUiConfigDto getConfigById(Long id);

    /**
     * Updates the UI-Config with the given id.
     *
     * @param id The id of the UI-Config to update.
     * @param config The new values for the UI-Config.
     * @return The updated UI-Config.
     * @throws ValidationException If the given values are invalid.
     */
    DetailedUiConfigDto update(Long id, UiConfigUpdateDto config, List<MultipartFile> images) throws ValidationException;


    /**
     * Returns the homepage configuration.
     *
     * @return The homepage configuration.
     */
    UiConfigHomepageDto getHomepageConfig();

    /**
     * Returns whether the given module is enabled.
     *
     * @param moduleName The name of the module to check.
     * @return Whether the given module is enabled.
     */
    boolean isModuleEnabled(String moduleName);


}
