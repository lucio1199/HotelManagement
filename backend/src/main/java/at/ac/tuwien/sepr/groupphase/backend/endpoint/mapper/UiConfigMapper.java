package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DetailedUiConfigDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UiConfigHomepageDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.UiConfig;
import org.mapstruct.Mapper;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Mapper for converting UiConfig entities to DTOs.
 */
@Mapper
public interface UiConfigMapper {

    /**
     * Maps a UiConfig entity to a DetailedUiConfigDto.
     *
     * @param uiConfig The UiConfig entity to map.
     * @return The resulting DetailedUiConfigDto.
     */
    default DetailedUiConfigDto uiConfigToDetailedUiConfigDto(UiConfig uiConfig) {
        if (uiConfig == null) {
            return null;
        }

        List<String> uiImages = Optional.ofNullable(uiConfig.getImages())
            .orElse(Collections.emptyList())
            .stream()
            .map(image -> Base64.getEncoder().encodeToString(image.getData()))
            .toList();

        return new DetailedUiConfigDto(
            uiConfig.getId(),
            uiConfig.getHotelName(),
            uiConfig.getDescriptionShort(),
            uiConfig.getDescription(),
            uiConfig.getAddress(),
            uiConfig.getRoomCleaning(),
            uiConfig.getDigitalCheckIn(),
            uiConfig.getActivities(),
            uiConfig.getCommunication(),
            uiConfig.getNuki(),
            uiConfig.getHalfBoard(),
            uiConfig.getPriceHalfBoard(),
            uiImages
        );
    }


    /**
     * Maps a UiConfig entity to a UiConfigHomepageDto.
     *
     * @param uiConfig The UiConfig entity to map.
     * @return The resulting UiConfigHomepageDto.
     */
    default UiConfigHomepageDto uiConfigToUiConfigHomepageDto(UiConfig uiConfig) {
        if (uiConfig == null) {
            return null;
        }

        List<String> uiImages = Optional.ofNullable(uiConfig.getImages())
            .orElse(Collections.emptyList())
            .stream()
            .map(image -> Base64.getEncoder().encodeToString(image.getData()))
            .toList();

        return new UiConfigHomepageDto(
            uiConfig.getHotelName(),
            uiConfig.getDescriptionShort(),
            uiConfig.getDescription(),
            uiConfig.getAddress(),
            uiImages
        );
    }
}
