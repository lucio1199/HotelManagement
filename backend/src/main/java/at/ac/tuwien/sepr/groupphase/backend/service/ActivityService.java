package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ActivityCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ActivityListDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ActivitySearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ActivitySlotDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ActivitySlotSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ActivityTimeslotInfoDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ActivityUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DetailedActivityDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ActivityService {

    /**
     * Retrieves detailed information about a specific activity by its ID.
     *
     * @param id the ID of the activity to retrieve.
     * @return a {@link DetailedActivityDto} containing the activity details.
     */
    DetailedActivityDto findOne(Long id);

    /**
     * Creates a new activity based on the provided data, main image, additional images, and timeslots.
     *
     * @param activityDto      the {@link ActivityCreateDto} containing the activity details.
     * @param mainImage        the main {@link MultipartFile} image for the activity.
     * @param additionalImages the list of additional {@link MultipartFile} images for the activity.
     * @param timeslotsDto     the list of {@link ActivityTimeslotInfoDto} representing timeslots for the activity.
     * @return a {@link DetailedActivityDto} representing the created activity.
     * @throws ValidationException if the input data fails validation.
     */
    DetailedActivityDto create(ActivityCreateDto activityDto, MultipartFile mainImage, List<MultipartFile> additionalImages, List<ActivityTimeslotInfoDto> timeslotsDto) throws ValidationException, IOException;

    /**
     * Updates an existing activity identified by its ID.
     *
     * <p>Updates activity details, main image, additional images, and timeslots based on the provided data.</p>
     *
     * @param id               the ID of the activity to update.
     * @param activityUpdateDto the {@link ActivityUpdateDto} containing updated activity details.
     * @param mainImage        the new main {@link MultipartFile} image for the activity (optional).
     * @param additionalImages the new list of additional {@link MultipartFile} images for the activity.
     * @param timeslotsDto     the updated list of {@link ActivityTimeslotInfoDto} for the activity.
     * @return a {@link DetailedActivityDto} representing the updated activity.
     * @throws ValidationException if the updated data fails validation.
     */
    DetailedActivityDto update(Long id, ActivityUpdateDto activityUpdateDto, MultipartFile mainImage, List<MultipartFile> additionalImages, List<ActivityTimeslotInfoDto> timeslotsDto) throws ValidationException, IOException;

    /**
     * Retrieves a list of all activities.
     *
     * @return a list of {@link ActivityListDto} representing all activities.
     */
    Page<ActivityListDto> findAll(Pageable pageable);

    /**
     * Retrieves a recommended activity for a guest based on their previous activity purchases.
     *
     * @return an {@link ActivityListDto} activity.
     */
    ActivityListDto getRecommendedActivity();

    /**
     * Retrieves the main image of an activity by its ID as a byte array.
     *
     * @param id the ID of the activity whose main image is to be retrieved.
     * @return a {@link ResponseEntity} containing the image as a byte array.
     */
    ResponseEntity<byte[]> getMainImage(Long id);

    /**
     * Deletes an activity identified by its ID.
     *
     * @param id the ID of the activity to delete.
     * @throws NotFoundException if the activity with the specified ID does not exist.
     */
    void delete(Long id) throws NotFoundException, ValidationException;

    /**
     * Searches for activities based on the specified criteria.
     *
     * @param activitySearchDto the {@link ActivitySearchDto} containing search filters.
     * @return a list of {@link ActivityListDto} matching the search criteria.
     * @throws ValidationException if the search criteria are invalid.
     */
    Page<ActivityListDto> search(ActivitySearchDto activitySearchDto, Pageable pageable) throws ValidationException;

    Page<ActivitySlotDto> getPaginatedTimeslots(Long activityId, PageRequest pageRequest);

    Page<ActivitySlotDto> getFilteredTimeslots(Long activityId, ActivitySlotSearchDto activitySlotSearchDto, PageRequest pageRequest) throws ValidationException;
}
