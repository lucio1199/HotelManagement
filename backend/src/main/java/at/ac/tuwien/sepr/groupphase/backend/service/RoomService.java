package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RoomSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RoomAdminSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RoomCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RoomListDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DetailedRoomDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RoomUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RoomCleaningTimeDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Room;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import jakarta.persistence.EntityNotFoundException;
import java.io.IOException;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;


public interface RoomService {


    DetailedRoomDto findOne(Long id);

    /**
     * Creates a new room entity based on the provided {@link RoomCreateDto} and main image file.
     *
     * <p>Maps the {@link RoomCreateDto} to a {@link Room} entity, saves the entity to the repository,
     * and returns a {@link DetailedRoomDto} representing the newly created room. The main image is
     * processed and stored as part of the room's data.</p>
     *
     * @param roomDto          the {@link RoomCreateDto} object containing details for creating the room.
     * @param mainImage        the {@link MultipartFile} representing the main image for the room.
     * @param additionalImages the List of {@link MultipartFile} representing additional images for the room.
     * @return a {@link DetailedRoomDto} object representing the created room.
     * @throws ValidationException if validation fails for the room data or the main image.
     * @throws RuntimeException    if an error occurs during image processing or room creation.
     */
    DetailedRoomDto create(RoomCreateDto roomDto, MultipartFile mainImage, List<MultipartFile> additionalImages) throws ValidationException;

    /**
     * Updates the details of an existing room identified by its ID, including processing a new main image file.
     *
     * <p>Updates attributes such as name, description, price, capacity, and images based on the
     * provided {@link RoomUpdateDto}. Fields in the {@link RoomUpdateDto} that are {@code null}
     * are ignored, preserving the current values. If a new main image is provided, it replaces
     * the existing main image for the room.</p>
     *
     * <p>If the room with the specified ID does not exist, a {@link NotFoundException} is thrown.
     * If the provided data or image processing fails validation, a {@link ValidationException} is thrown.</p>
     *
     * @param id               the ID of the room to be updated.
     * @param roomUpdateDto    the {@link RoomUpdateDto} containing the updated room details.
     * @param mainImage        the {@link MultipartFile} representing a new main image for the room (optional).
     * @param additionalImages the List of {@link MultipartFile} representing additional images for the room.
     * @return the updated {@link DetailedRoomDto} representing the room after the update.
     * @throws NotFoundException   if a room with the specified ID cannot be found.
     * @throws ValidationException if validation fails for the updated data or image processing.
     */
    DetailedRoomDto update(Long id, RoomUpdateDto roomUpdateDto, MultipartFile mainImage, List<MultipartFile> additionalImages) throws ValidationException;

    /**
     * Updates the lastCleanedAt Date of an existing room identified by its ID.
     *
     * <p>If the room with the specified ID does not exist, a {@link NotFoundException} is thrown.
     *
     * @param id the ID of the room to be updated.
     * @return the updated {@link DetailedRoomDto} representing the room after the update.
     * @throws ValidationException if validation fails for the updated data or image processing.
     */
    RoomListDto updateLastCleanedAt(Long id) throws NotFoundException;

    /**
     * Updates the preferred Room Cleaning Time of an existing room identified by its ID.
     *
     * <p>If the room with the specified ID does not exist, a {@link NotFoundException} is thrown.
     *
     * @param id the ID of the room to be updated.
     * @param cleaningTimeDto the {@link RoomCleaningTimeDto} containing the updated room cleaning times and its ID.
     * @return the updated {@link RoomListDto} representing the room after the update.
     * @throws NotFoundException if a room with the specified ID cannot be found.
     */
    @Transactional
    RoomListDto updateCleaningTime(Long id, RoomCleaningTimeDto cleaningTimeDto) throws NotFoundException, ValidationException;

    /**
     * Deletes the Room Cleaning Time of an existing room identified by its ID.
     *
     * <p>If the room with the specified ID does not exist, a {@link NotFoundException} is thrown.
     *
     * @param id the ID of the room to be updated.
     * @throws NotFoundException if a room with the specified ID cannot be found.
     */
    @Transactional
    RoomListDto deleteCleaningTime(Long id) throws NotFoundException;

    /**
     * Searches for rooms based on the criteria specified in the {@link RoomSearchDto}.
     * Filters rooms by name, description, capacity range, and price range.
     *
     * @param roomSearchDto the {@link RoomSearchDto} object containing the search criteria.
     * @return a list of {@link Room} entities matching the search criteria.
     */
    Page<RoomListDto> search(RoomSearchDto roomSearchDto, Pageable pageable) throws ValidationException;

    /**
     * Retrieves all rooms from the repository, ordered by price in ascending order.
     *
     * @return a list of all {@link Room} entities, sorted by price in ascending order.
     */
    Page<RoomListDto> findAll(Pageable pageable);

    /**
     * Retrieves all rooms from the repository, ordered by cleaningTimeTo ascending order and then lastCleanedAt ascending order.
     *
     * @return a list of all {@link Room} entities, ordered by cleaningTimeTo ascending order and then lastCleanedAt ascending order.
     */
    Page<RoomListDto> findAllForClean(Pageable pageable);


    /**
     * Retrieves the main image of a room by its ID and returns it as a {@link ResponseEntity}.
     * The image is included in the response body as a byte array with appropriate HTTP headers.
     *
     * @param id the ID of the room whose main image is to be retrieved.
     * @return a {@link ResponseEntity} containing the image as a byte array with content type
     *         set to "image/*" and an HTTP status of 200 (OK).
     * @throws EntityNotFoundException if no room with the given ID is found.
     */
    ResponseEntity<byte[]> getMainImage(Long id);

    /**
     * Deletes a room entity based on the provided ID.
     *
     * @param id the ID representing the room to be deleted.
     * @throws NotFoundException if a room with the specified ID cannot be found.
     * @throws ConflictException if the room corresponding to the ID is booked in the future.
     */
    void delete(Long id) throws NotFoundException, ConflictException;

    /**
     * Searches for rooms based on the criteria specified in the {@link RoomAdminSearchDto}.
     *
     * @param roomAdminSearchDto the {@link RoomAdminSearchDto} object containing the search criteria.
     * @return a list of {@link RoomListDto} entities matching the search criteria.
     */
    Page<RoomListDto> adminSearch(RoomAdminSearchDto roomAdminSearchDto, Pageable pageable) throws ValidationException;
}
