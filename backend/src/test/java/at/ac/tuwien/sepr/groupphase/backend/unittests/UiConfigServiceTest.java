package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DetailedUiConfigDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UiConfigHomepageDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UiConfigUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.UiConfigMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.UiConfig;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.UiConfigRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.UiConfigService;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.SimpleUiConfigService;
import at.ac.tuwien.sepr.groupphase.backend.service.validator.UiConfigValidator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class UiConfigServiceTest {
    @Mock
    private UiConfigRepository uiConfigRepository;

    @Mock
    private UiConfigValidator uiConfigValidator;

    @Mock
    private UiConfigMapper uiConfigMapper;

    private UiConfigService uiConfigService;


    @BeforeEach
    void beforeEach() {
        uiConfigService = new SimpleUiConfigService(uiConfigRepository, uiConfigMapper, uiConfigValidator);
    }

    @Test
    void update_ValidConfig_ReturnsUpdatedConfig() throws ValidationException {
        Long id = 1L;
        UiConfigUpdateDto updateDto = new UiConfigUpdateDto(
            id,
            "Updated Hotel Name",
            "Updated Short Description",
            "Updated Description",
            "Updated Address",
            true, // roomCleaning
            true, // digitalCheckIn
            true, // activities
            true, // communication
            true, // nuki
            true, // halfBoard
            50.0  // priceHalfBoard
        );

        UiConfig uiConfig = new UiConfig(
            id,
            "Original Hotel Name",
            "Original Short Description",
            "Original Description",
            "Original Address",
            false, // roomCleaning
            false, // digitalCheckIn
            false, // activities
            false, // communication
            true,  // nuki
            true,  // halfBoard
            40.0,  // priceHalfBoard
            new ArrayList<>()
        );

        DetailedUiConfigDto detailedUiConfigDto = new DetailedUiConfigDto(
            id,
            "Updated Hotel Name",
            "Updated Short Description",
            "Updated Description",
            "Updated Address",
            true,  // roomCleaning
            true,  // digitalCheckIn
            true,  // activities
            true,  // communication
            true,  // nuki
            true,  // halfBoard
            50.0,  // priceHalfBoard
            new ArrayList<>()
        );

        List<MultipartFile> mockImages = Collections.emptyList();

        when(uiConfigRepository.findById(id)).thenReturn(Optional.of(uiConfig));
        doNothing().when(uiConfigValidator).validateForUpdate(updateDto, mockImages);
        when(uiConfigRepository.save(uiConfig)).thenReturn(uiConfig);
        when(uiConfigMapper.uiConfigToDetailedUiConfigDto(uiConfig)).thenReturn(detailedUiConfigDto);

        DetailedUiConfigDto result = uiConfigService.update(id, updateDto, new ArrayList<>());

        assertAll(
            "Verify the updated UI configuration",
            () -> assertNotNull(result, "The result should not be null"),
            () -> assertEquals(detailedUiConfigDto.id(), result.id(), "The IDs should match"),
            () -> assertEquals(detailedUiConfigDto.hotelName(), result.hotelName(), "The hotel names should match"),
            () -> assertEquals(detailedUiConfigDto.descriptionShort(), result.descriptionShort(), "The short descriptions should match"),
            () -> assertEquals(detailedUiConfigDto.description(), result.description(), "The descriptions should match"),
            () -> assertEquals(detailedUiConfigDto.address(), result.address(), "The addresses should match"),
            () -> assertEquals(detailedUiConfigDto.roomCleaning(), result.roomCleaning(), "Room cleaning flags should match"),
            () -> assertEquals(detailedUiConfigDto.digitalCheckIn(), result.digitalCheckIn(), "Digital check-in flags should match"),
            () -> assertEquals(detailedUiConfigDto.activities(), result.activities(), "Activities flags should match"),
            () -> assertEquals(detailedUiConfigDto.communication(), result.communication(), "Communication flags should match"),
            () -> assertEquals(detailedUiConfigDto.nuki(), result.nuki(), "Nuki flags should match"),
            () -> assertEquals(detailedUiConfigDto.halfBoard(), result.halfBoard(), "Half-board flags should match"),
            () -> assertEquals(detailedUiConfigDto.priceHalfBoard(), result.priceHalfBoard(), "Half-board prices should match"),
            () -> assertEquals(detailedUiConfigDto.images(), result.images(), "The images should match")
        );

        verify(uiConfigRepository).findById(id);
        verify(uiConfigValidator).validateForUpdate(updateDto, mockImages);
        verify(uiConfigRepository).save(uiConfig);
        verify(uiConfigMapper).uiConfigToDetailedUiConfigDto(uiConfig);
    }


    @Test
    void update_NonExistingConfig_ThrowsNotFoundException() {
        // Arrange
        Long id = 999L;
        UiConfigUpdateDto updateDto = new UiConfigUpdateDto(
            id,
            "Updated Hotel Name",
            "Updated Short Description",
            "Updated Description",
            "Updated Address",
            true,  // roomCleaning
            true,  // digitalCheckIn
            true,  // activities
            true,  // communication
            true,  // nuki
            true,  // halfBoard
            20.0   // priceHalfBoard
        );

        when(uiConfigRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> uiConfigService.update(id, updateDto, new ArrayList<>()),
            "Expected NotFoundException to be thrown"
        );

        assertEquals("Config with id 999 not found", exception.getMessage());
        verify(uiConfigRepository).findById(id);
        verifyNoInteractions(uiConfigValidator, uiConfigMapper);
    }

    @Test
    void update_InvalidConfig_ThrowsValidationException() throws ValidationException {
        Long id = 1L;
        UiConfigUpdateDto updateDto = new UiConfigUpdateDto(
            id,
            "Invalid Name",
            "Invalid Short Description",
            "Updated Descritpion",
            "Updated Address",
            null,  // roomCleaning
            null,  // digitalCheckIn
            null,  // activities
            null,  // communication
            null,  // nuki
            null,  // halfBoard
            null   // priceHalfBoard
        );

        UiConfig uiConfig = new UiConfig(
            id,
            "Original Name",
            "Original Short Description",
            "Original Description",
            "Original Address",
            false, // roomCleaning
            false, // digitalCheckIn
            false, // activities
            false, // communication
            false, // nuki
            false, // halfBoard
            null,  // priceHalfBoard
            new ArrayList<>()
        );

        List<MultipartFile> mockImages = Collections.emptyList();

        when(uiConfigRepository.findById(id)).thenReturn(Optional.of(uiConfig));
        doThrow(new ValidationException("Validation failed", List.of("Invalid name", "Invalid short description")))
            .when(uiConfigValidator).validateForUpdate(updateDto, mockImages);

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> uiConfigService.update(id, updateDto, new ArrayList<>()),
            "Expected ValidationException to be thrown"
        );

        assertEquals("Validation failed. Failed validations: Invalid name, Invalid short description.", exception.getMessage());
        verify(uiConfigRepository).findById(id);
        verify(uiConfigValidator).validateForUpdate(updateDto, mockImages);
        verifyNoInteractions(uiConfigMapper);
        verifyNoMoreInteractions(uiConfigRepository);
    }

    @Test
    void update_PartialUpdate_ReturnsUpdatedConfig() throws ValidationException {
        // Arrange
        Long id = 1L;
        UiConfigUpdateDto updateDto = new UiConfigUpdateDto(
            id,
            "Updated Hotel Name",
            null,  // descriptionShort
            null,  // description
            "Updated Address",
            null,  // roomCleaning
            true,  // digitalCheckIn
            null,  // activities
            true,  // communication
            null,  // nuki
            null,  // halfBoard
            null   // priceHalfBoard
        );

        UiConfig uiConfig = new UiConfig(
            id,
            "Original Hotel Name",
            "Original Short Description",
            "Original Description",
            "Original Address",
            true,  // roomCleaning
            false, // digitalCheckIn
            true,  // activities
            false, // communication
            true,  // nuki
            true,  // halfBoard
            50.0,  // priceHalfBoard
            new ArrayList<>()
        );

        UiConfig updatedConfig = new UiConfig(
            id,
            "Updated Hotel Name",
            "Original Short Description",
            "Original Description",
            "Updated Address",
            true,  // roomCleaning
            true,  // digitalCheckIn
            true,  // activities
            true,  // communication
            true,  // nuki
            true,  // halfBoard
            50.0,  // priceHalfBoard
            new ArrayList<>()
        );

        DetailedUiConfigDto detailedUiConfigDto = new DetailedUiConfigDto(
            id,
            "Updated Hotel Name",
            "Original Short Description",
            "Original Description",
            "Updated Address",
            true,  // roomCleaning
            true,  // digitalCheckIn
            true,  // activities
            true,  // communication
            true,  // nuki
            true,  // halfBoard
            50.0,  // priceHalfBoard
            new ArrayList<>()
        );

        List<MultipartFile> mockImages = Collections.emptyList();

        when(uiConfigRepository.findById(id)).thenReturn(Optional.of(uiConfig));
        doNothing().when(uiConfigValidator).validateForUpdate(updateDto, mockImages);
        when(uiConfigRepository.save(uiConfig)).thenReturn(updatedConfig);
        when(uiConfigMapper.uiConfigToDetailedUiConfigDto(updatedConfig)).thenReturn(detailedUiConfigDto);

        // Act
        DetailedUiConfigDto result = uiConfigService.update(id, updateDto, new ArrayList<>());

        // Assert
        assertAll(
            "Verify the partially updated UI configuration",
            () -> assertNotNull(result),
            () -> assertEquals(detailedUiConfigDto.id(), result.id()),
            () -> assertEquals(detailedUiConfigDto.hotelName(), result.hotelName()),
            () -> assertEquals(detailedUiConfigDto.descriptionShort(), result.descriptionShort()),
            () -> assertEquals(detailedUiConfigDto.description(), result.description()),
            () -> assertEquals(detailedUiConfigDto.address(), result.address()),
            () -> assertEquals(detailedUiConfigDto.roomCleaning(), result.roomCleaning()),
            () -> assertEquals(detailedUiConfigDto.digitalCheckIn(), result.digitalCheckIn()),
            () -> assertEquals(detailedUiConfigDto.activities(), result.activities()),
            () -> assertEquals(detailedUiConfigDto.communication(), result.communication()),
            () -> assertEquals(detailedUiConfigDto.nuki(), result.nuki()),
            () -> assertEquals(detailedUiConfigDto.halfBoard(), result.halfBoard()),
            () -> assertEquals(detailedUiConfigDto.priceHalfBoard(), result.priceHalfBoard())
        );

        verify(uiConfigRepository).findById(id);
        verify(uiConfigValidator).validateForUpdate(updateDto, mockImages);
        verify(uiConfigRepository).save(uiConfig);
        verify(uiConfigMapper).uiConfigToDetailedUiConfigDto(updatedConfig);
    }

    @Test
    void update_NoChanges_ReturnsOriginalConfig() throws ValidationException {
        // Arrange
        Long id = 1L;
        UiConfigUpdateDto updateDto = new UiConfigUpdateDto(
            id,
            null, // hotelName
            null, // descriptionShort
            null, // description
            null, // address
            null, // roomCleaning
            null, // digitalCheckIn
            null, // activities
            null, // communication
            null, // nuki
            null, // halfBoard
            null  // priceHalfBoard
        );

        UiConfig uiConfig = new UiConfig(
            id,
            "Original Hotel Name",
            "Original Short Description",
            "Original Description",
            "Original Address",
            false, // roomCleaning
            false, // digitalCheckIn
            false, // activities
            true,  // communication
            true,  // nuki
            false, // halfBoard
            null,  // priceHalfBoard
            new ArrayList<>()
        );

        DetailedUiConfigDto detailedUiConfigDto = new DetailedUiConfigDto(
            id,
            "Original Hotel Name",
            "Original Short Description",
            "Original Description",
            "Original Address",
            false, // roomCleaning
            false, // digitalCheckIn
            false, // activities
            true,  // communication
            true,  // nuki
            false, // halfBoard
            null,  // priceHalfBoard
            new ArrayList<>()
        );

        List<MultipartFile> mockImages = Collections.emptyList();

        when(uiConfigRepository.findById(id)).thenReturn(Optional.of(uiConfig));
        doNothing().when(uiConfigValidator).validateForUpdate(updateDto, mockImages);
        when(uiConfigRepository.save(uiConfig)).thenReturn(uiConfig);
        when(uiConfigMapper.uiConfigToDetailedUiConfigDto(uiConfig)).thenReturn(detailedUiConfigDto);

        // Act
        DetailedUiConfigDto result = uiConfigService.update(id, updateDto, new ArrayList<>());

        // Assert
        assertAll(
            "Verify no changes are made",
            () -> assertNotNull(result),
            () -> assertEquals(detailedUiConfigDto.id(), result.id()),
            () -> assertEquals(detailedUiConfigDto.hotelName(), result.hotelName()),
            () -> assertEquals(detailedUiConfigDto.descriptionShort(), result.descriptionShort()),
            () -> assertEquals(detailedUiConfigDto.description(), result.description()),
            () -> assertEquals(detailedUiConfigDto.address(), result.address()),
            () -> assertEquals(detailedUiConfigDto.roomCleaning(), result.roomCleaning()),
            () -> assertEquals(detailedUiConfigDto.digitalCheckIn(), result.digitalCheckIn()),
            () -> assertEquals(detailedUiConfigDto.activities(), result.activities()),
            () -> assertEquals(detailedUiConfigDto.communication(), result.communication()),
            () -> assertEquals(detailedUiConfigDto.nuki(), result.nuki()),
            () -> assertEquals(detailedUiConfigDto.halfBoard(), result.halfBoard()),
            () -> assertEquals(detailedUiConfigDto.priceHalfBoard(), result.priceHalfBoard())
        );

        verify(uiConfigRepository).findById(id);
        verify(uiConfigValidator).validateForUpdate(updateDto, mockImages);
        verify(uiConfigRepository).save(uiConfig);
        verify(uiConfigMapper).uiConfigToDetailedUiConfigDto(uiConfig);
    }

    @Test
    void getHomepageConfig_ValidConfig_ReturnsUiConfigHomepageDto() {
        // Arrange
        Long id = 1L;
        UiConfig uiConfig = new UiConfig(
            id,
            "Test Hotel",
            "Short description",
            "Long description",
            "Test Address",
            true,  // roomCleaning
            true,  // digitalCheckIn
            true,  // activities
            true,  // communication
            true,  // nuki
            true,  // halfBoard
            50.0,  // priceHalfBoard
            new ArrayList<>()
        );

        UiConfigHomepageDto expectedDto = new UiConfigHomepageDto(
            "Test Hotel",
            "Short description",
            "Long description",
            "Test Address",
            new ArrayList<>()
        );

        when(uiConfigRepository.findById(id)).thenReturn(Optional.of(uiConfig));
        when(uiConfigMapper.uiConfigToUiConfigHomepageDto(uiConfig)).thenReturn(expectedDto);
        // Act
        UiConfigHomepageDto result = uiConfigService.getHomepageConfig();

        // Assert
        assertAll(
            () -> assertNotNull(result, "The result should not be null"),
            () -> assertEquals(uiConfig.getHotelName(), result.hotelName(), "The hotel names should match"),
            () -> assertEquals(uiConfig.getDescriptionShort(), result.descriptionShort(), "The short descriptions should match"),
            () -> assertEquals(uiConfig.getDescription(), result.description(), "The descriptions should match"),
            () -> assertEquals(uiConfig.getAddress(), result.address(), "The addresses should match"),
            () -> assertEquals(uiConfig.getImages(), result.images(), "The images should match"),
            () -> assertEquals(uiConfig.getHotelName(), result.hotelName(), "The hotel names should match")
        );
        verify(uiConfigRepository).findById(id);
    }
}
