package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RoomCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RoomUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RoomListDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DetailedRoomDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RoomCleaningTimeDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RoomSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RoomAdminSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.service.RoomService;
import jakarta.annotation.security.PermitAll;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;

@RestController
@RequestMapping("api/v1/room")
public class RoomEndpoint {


    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private RoomService roomService;

    public RoomEndpoint(RoomService roomService) {
        this.roomService = roomService;
    }

    @Secured("ROLE_ADMIN")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public DetailedRoomDto create(
        @Valid @ModelAttribute RoomCreateDto roomDto,
        @RequestPart(value = "mainImage", required = false) MultipartFile mainImage,
        @RequestPart(value = "additionalImages", required = false) List<MultipartFile> additionalImages) throws ValidationException, IOException {
        LOGGER.info("POST /api/v1/room multipart");
        LOGGER.debug("request body: {}", roomDto);
        if (mainImage != null) {
            LOGGER.debug("main image_type: {}, main_image_name: {}, main_image_size: {}", mainImage.getContentType(), mainImage.getOriginalFilename(), mainImage.getSize());
        }
        return roomService.create(roomDto, mainImage, additionalImages);
    }

    @Secured("ROLE_ADMIN")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public DetailedRoomDto updateRoom(
        @PathVariable("id") Long id,
        @Valid @ModelAttribute RoomUpdateDto roomDto,
        @RequestPart(value = "mainImage", required = false) MultipartFile mainImage,
        @RequestPart(value = "additionalImages", required = false) List<MultipartFile> additionalImages) throws ValidationException {
        LOGGER.info("PUT /api/v1/room/{}", id);
        LOGGER.debug("request body: {}", roomDto);

        return roomService.update(id, roomDto, mainImage, additionalImages);
    }

    @Secured("ROLE_CLEANING_STAFF")
    @PutMapping(value = "/{id}/clean")
    @ResponseStatus(HttpStatus.OK)
    public RoomListDto updateRoomLastCleanedAt(
        @PathVariable("id") Long id
    ) {
        LOGGER.info("PUT /api/v1/room/{}/clean", id);
        return roomService.updateLastCleanedAt(id);
    }

    @PutMapping(value = "/{id}/clean-time", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public RoomListDto updateRoomCleaningTime(
        @PathVariable("id") Long id,
        @RequestBody RoomCleaningTimeDto cleaningTimeDto) throws ValidationException {
        LOGGER.info("PUT /api/v1/room/{}/clean-time", id);
        LOGGER.debug("Cleaning time update request: {}", cleaningTimeDto);

        return roomService.updateCleaningTime(id, cleaningTimeDto);
    }

    @Secured("ROLE_CLEANING_STAFF")
    @DeleteMapping("/{id}")
    public RoomListDto deleteRoomCleaningTime(@PathVariable("id") Long id) throws NotFoundException {
        LOGGER.info("DELETE /api/v1/room/{}", id);
        return roomService.deleteCleaningTime(id);
    }

    @PermitAll
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<RoomListDto> search(RoomSearchDto roomSearchDto, Pageable pageable) throws ValidationException {
        LOGGER.info("GET /api/v1/room with criteria:{}", roomSearchDto);
        return roomService.search(roomSearchDto, pageable);
    }

    @PermitAll
    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    public Page<RoomListDto> findAll(Pageable pageable) {
        LOGGER.info("GET /api/v1/room/all");
        return roomService.findAll(pageable);
    }

    @Secured("ROLE_CLEANING_STAFF")
    @GetMapping("/clean")
    @ResponseStatus(HttpStatus.OK)
    public Page<RoomListDto> searchForClean(Pageable pageable) {
        LOGGER.info("GET /api/v1/room");
        return roomService.findAllForClean(pageable);
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/admin")
    @ResponseStatus(HttpStatus.OK)
    public Page<RoomListDto> adminSearch(RoomAdminSearchDto roomAdminSearchDto, Pageable pageable) throws ValidationException {
        LOGGER.info("GET /api/v1/room/admin");
        return roomService.adminSearch(roomAdminSearchDto, pageable);
    }

    @PermitAll
    @GetMapping("/{id}")
    @Transactional
    @ResponseStatus(HttpStatus.OK)
    public DetailedRoomDto findOne(@PathVariable("id") Long id) {
        LOGGER.info("GET /api/v1/room/{}", id);
        return roomService.findOne(id);
    }

    @PermitAll
    @GetMapping("/image/{id}")
    public ResponseEntity<byte[]> getMainImage(@PathVariable("id") Long id) {
        LOGGER.info("GET /api/v1/room/image/{}", id);
        return roomService.getMainImage(id);
    }

    @Secured("ROLE_ADMIN")
    @DeleteMapping("/rooms/{id}")
    public void delete(@PathVariable("id") Long id) throws NotFoundException, ConflictException {
        LOGGER.info("DELETE /api/v1/room/rooms/{}", id);
        roomService.delete(id);
    }
}
