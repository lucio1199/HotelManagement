package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DetailedUiConfigDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UiConfigHomepageDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UiConfigUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.UiConfigMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.UiConfig;
import at.ac.tuwien.sepr.groupphase.backend.entity.UiImage;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.UiConfigRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.UiConfigService;
import at.ac.tuwien.sepr.groupphase.backend.service.validator.UiConfigValidator;
import jakarta.transaction.Transactional;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SimpleUiConfigService implements UiConfigService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final UiConfigRepository uiConfigRepository;
    private final UiConfigMapper uiConfigMapper;
    private final UiConfigValidator uiConfigValidator;

    public SimpleUiConfigService(UiConfigRepository uiConfigRepository, UiConfigMapper uiConfigMapper, UiConfigValidator uiConfigValidator) {
        this.uiConfigRepository = uiConfigRepository;
        this.uiConfigMapper = uiConfigMapper;
        this.uiConfigValidator = uiConfigValidator;
    }

    @Override
    @Transactional
    public DetailedUiConfigDto getConfigById(Long id) {
        LOGGER.debug("Get UI-Config with id {}", id);
        UiConfig uiConfig = uiConfigRepository.findById(id).orElseThrow(() ->
            new NotFoundException("Config with id " + id + " not found"));

        return uiConfigMapper.uiConfigToDetailedUiConfigDto(uiConfig);
    }

    @Override
    @Transactional
    public DetailedUiConfigDto update(Long id, UiConfigUpdateDto uiConfigUpdateDto, List<MultipartFile> images) throws ValidationException {
        LOGGER.debug("Update UI-Config {}", uiConfigUpdateDto);

        // Fetch the existing config
        UiConfig existingConfig = uiConfigRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Config with id " + id + " not found"));

        // Validate the input
        uiConfigValidator.validateForUpdate(uiConfigUpdateDto, images);

        // Update scalar fields
        Optional.ofNullable(uiConfigUpdateDto.hotelName()).ifPresent(existingConfig::setHotelName);
        Optional.ofNullable(uiConfigUpdateDto.descriptionShort()).ifPresent(existingConfig::setDescriptionShort);
        Optional.ofNullable(uiConfigUpdateDto.description()).ifPresent(existingConfig::setDescription);
        Optional.ofNullable(uiConfigUpdateDto.address()).ifPresent(existingConfig::setAddress);
        Optional.ofNullable(uiConfigUpdateDto.roomCleaning()).ifPresent(existingConfig::setRoomCleaning);
        Optional.ofNullable(uiConfigUpdateDto.digitalCheckIn()).ifPresent(existingConfig::setDigitalCheckIn);
        Optional.ofNullable(uiConfigUpdateDto.activities()).ifPresent(existingConfig::setActivities);
        Optional.ofNullable(uiConfigUpdateDto.communication()).ifPresent(existingConfig::setCommunication);
        Optional.ofNullable(uiConfigUpdateDto.nuki()).ifPresent(existingConfig::setNuki);
        Optional.ofNullable(uiConfigUpdateDto.halfBoard()).ifPresent(existingConfig::setHalfBoard);
        Optional.ofNullable(uiConfigUpdateDto.priceHalfBoard()).ifPresent(existingConfig::setPriceHalfBoard);

        if (images != null && !images.isEmpty()) {
            List<UiImage> newUiImages = new ArrayList<>();
            for (MultipartFile file : images) {
                if (!file.isEmpty()) {
                    try {
                        UiImage uiImage = new UiImage();
                        uiImage.setData(file.getBytes());
                        uiImage.setConfig(existingConfig);
                        uiImage.setAltText(file.getOriginalFilename());
                        uiImage.setCreatedAt(LocalDateTime.now());
                        newUiImages.add(uiImage);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to process UI-image file", e);
                    }
                    existingConfig.getImages().clear();
                    existingConfig.getImages().addAll(newUiImages);
                }
            }
        }
        return uiConfigMapper.uiConfigToDetailedUiConfigDto(uiConfigRepository.save(existingConfig));
    }

    @Override
    @Transactional
    public UiConfigHomepageDto getHomepageConfig() {
        LOGGER.debug("Get UiHomepageConfig with id {}", 1L);
        UiConfig uiConfig = uiConfigRepository.findById(1L).orElseThrow(() ->
            new NotFoundException("Config with id 1 not found"));
        return uiConfigMapper.uiConfigToUiConfigHomepageDto(uiConfig);
    }

    @Override
    public boolean isModuleEnabled(String moduleName) {
        UiConfig uiConfig = getUiConfig(); // Fetch the current configuration from the database

        // Use a switch or similar logic to map the module name to the corresponding boolean field
        return switch (moduleName.toLowerCase()) {
            case "roomcleaning" -> uiConfig.getRoomCleaning();
            case "digitalcheckin" -> uiConfig.getDigitalCheckIn();
            case "activities" -> uiConfig.getActivities();
            case "communication" -> uiConfig.getCommunication();
            case "nuki" -> uiConfig.getNuki();
            default -> throw new IllegalArgumentException("Module name not recognized: " + moduleName);
        };

    }

    /**
     * Fetches the UI Configuration from the database.
     *
     * @return The UI Configuration.
     * @throws IllegalStateException If the configuration is not found.
     */
    private UiConfig getUiConfig() {
        return uiConfigRepository.findById(1L)
            .orElseThrow(() -> new IllegalStateException("UI Configuration not found."));
    }
}
