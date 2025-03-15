package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ActivityCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ActivityListDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ActivitySearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ActivitySlotDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ActivitySlotSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ActivityTimeslotInfoDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ActivityUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DetailedActivityDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RoomListDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.service.ActivityService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.security.PermitAll;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;

@RestController
@RequestMapping("api/v1/activity")
public class ActivityEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private ActivityService activityService;

    public ActivityEndpoint(ActivityService activityService) {
        this.activityService = activityService;
    }

    @Secured("ROLE_ADMIN")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public DetailedActivityDto create(
        @Valid @ModelAttribute ActivityCreateDto activityDto,
        @RequestPart(value = "mainImage", required = false) MultipartFile mainImage,
        @RequestPart(value = "additionalImages", required = false) List<MultipartFile> additionalImages,
        @RequestPart(value = "timeslots", required = false) String timeslotsJson) throws ValidationException, IOException {

        LOGGER.info("POST /api/v1/activity multipart");
        LOGGER.debug("create dto: {}", activityDto);
        LOGGER.debug("timeslots json: {}", timeslotsJson);

        // Deserialize timeslots
        List<ActivityTimeslotInfoDto> timeslots = null;
        if (timeslotsJson != null) {
            try {
                // Register JavaTimeModule to handle time-related types
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule()); // Required for Java 8 time classes

                timeslots = objectMapper.readValue(
                    timeslotsJson,
                    new TypeReference<List<ActivityTimeslotInfoDto>>() {}
                );
            } catch (Exception e) {
                LOGGER.error("Failed to deserialize timeslots JSON", e);
                throw new ValidationException("Validation failed for one or more fields.", List.of("Invalid timeslots JSON"));
            }
        }

        LOGGER.info("Parsed timeslots: {}", timeslots);
        if (mainImage != null) {
            LOGGER.debug("main image_type: {}, main_image_name: {}, main_image_size: {}",
                mainImage.getContentType(), mainImage.getOriginalFilename(), mainImage.getSize());
        }
        LOGGER.info("Parsed categories: {}", activityDto.categories());


        // Handle the case where timeslot might not have a specific date and relies on dayOfWeek instead
        if (timeslots != null) {
            timeslots.forEach(timeslot -> {
                if (timeslot.specificDate() == null && timeslot.dayOfWeek() != null) {
                    // Here you might decide how to handle a `null` specificDate and populate or map accordingly.
                    LOGGER.debug("Timeslot with null specificDate found: {}", timeslot);
                    // You can either process that here or do something based on dayOfWeek
                }
            });
        }

        return activityService.create(activityDto, mainImage, additionalImages, timeslots);
    }

    @Secured("ROLE_ADMIN")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public DetailedActivityDto updateActivity(
        @PathVariable("id") Long id,
        @Valid @ModelAttribute ActivityUpdateDto activityDto,
        @RequestPart(value = "mainImage", required = false) MultipartFile mainImage,
        @RequestPart(value = "additionalImages", required = false) List<MultipartFile> additionalImages,
        @RequestPart(value = "timeslots", required = false) String timeslotsJson) throws ValidationException, IOException {

        LOGGER.info("PUT /api/v1/activity multipart");
        LOGGER.debug("update dto: {}", activityDto);
        LOGGER.debug("timeslots json: {}", timeslotsJson);

        // Deserialize timeslots
        List<ActivityTimeslotInfoDto> timeslots = null;
        if (timeslotsJson != null) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule()); // Required for Java 8 time classes

                timeslots = objectMapper.readValue(
                    timeslotsJson,
                    new TypeReference<List<ActivityTimeslotInfoDto>>() {}
                );
            } catch (Exception e) {
                LOGGER.error("Failed to deserialize timeslots JSON", e);
                throw new ValidationException("Validation failed for one or more fields.", List.of("Invalid timeslots JSON"));
            }
        }

        LOGGER.info("Parsed timeslots: {}", timeslots);
        if (mainImage != null) {
            LOGGER.debug("main image_type: {}, main_image_name: {}, main_image_size: {}",
                mainImage.getContentType(), mainImage.getOriginalFilename(), mainImage.getSize());
        }

        // Handle the case where timeslot might not have a specific date and relies on dayOfWeek instead
        if (timeslots != null) {
            timeslots.forEach(timeslot -> {
                if (timeslot.specificDate() == null && timeslot.dayOfWeek() != null) {
                    // Here you might decide how to handle a `null` specificDate and populate or map accordingly.
                    LOGGER.debug("Timeslot with null specificDate found: {}", timeslot);
                    // You can either process that here or do something based on dayOfWeek
                }
            });
        }
        return activityService.update(id, activityDto, mainImage, additionalImages, timeslots);
    }

    @PermitAll
    @GetMapping("/all")
    @Transactional
    @ResponseStatus(HttpStatus.OK)
    public Page<ActivityListDto> findAll(
        @RequestParam("pageIndex") int pageIndex,
        @RequestParam("pageSize") int pageSize) {
        LOGGER.info("GET /api/v1/activity/all");
        return activityService.findAll(PageRequest.of(pageIndex, pageSize));
    }

    @Secured("ROLE_GUEST")
    @GetMapping("/recommended")
    @Transactional
    @ResponseStatus(HttpStatus.OK)
    public ActivityListDto getRecommendedActivity() {
        LOGGER.info("GET /api/v1/activity/recommended");
        return activityService.getRecommendedActivity();
    }


    @PermitAll
    @GetMapping("/image/{id}")
    public ResponseEntity<byte[]> getMainImage(@PathVariable("id") Long id) {
        LOGGER.info("GET /api/v1/activity/image/{}", id);
        return activityService.getMainImage(id);
    }

    @PermitAll
    @GetMapping("/search")
    @Transactional
    @ResponseStatus(HttpStatus.OK)
    public Page<ActivityListDto> search(
        ActivitySearchDto activitySearchDto,
        @RequestParam("pageIndex") int pageIndex,
        @RequestParam("pageSize") int pageSize
    ) throws ValidationException {
        LOGGER.info("GET /api/v1/activity with criteria:{}", activitySearchDto);
        return activityService.search(activitySearchDto, PageRequest.of(pageIndex, pageSize));
    }

    @PermitAll
    @GetMapping("/{id}")
    @Transactional
    @ResponseStatus(HttpStatus.OK)
    public DetailedActivityDto findOne(@PathVariable("id") Long id) {
        LOGGER.info("GET /api/v1/activity/{}", id);
        return activityService.findOne(id);
    }

    @Secured("ROLE_ADMIN")
    @DeleteMapping("/activities/{id}")
    public void delete(@PathVariable("id") Long id) throws NotFoundException, ValidationException {
        LOGGER.info("DELETE /api/v1/activity/activities/{}", id);
        activityService.delete(id);
    }


    @GetMapping("/timeslots/{activityId}")
    public Page<ActivitySlotDto> getPaginatedTimeslots(
        @PathVariable("activityId") Long activityId,
        @RequestParam("pageIndex") int pageIndex,
        @RequestParam("pageSize") int pageSize
    ) {
        return activityService.getPaginatedTimeslots(activityId, PageRequest.of(pageIndex, pageSize));
    }

    @GetMapping("/timeslots/search/{activityId}")
    public Page<ActivitySlotDto> getPaginatedFilteredTimeslots(
        @PathVariable("activityId") Long activityId,
        ActivitySlotSearchDto activitySlotSearchDto,
        @RequestParam("pageIndex") int pageIndex,
        @RequestParam("pageSize") int pageSize
    ) throws ValidationException {
        return activityService.getFilteredTimeslots(activityId, activitySlotSearchDto, PageRequest.of(pageIndex, pageSize));
    }



}
