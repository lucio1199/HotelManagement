package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DetailedRoomDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RoomCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RoomListDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RoomUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RoomSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RoomCleaningTimeDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RoomAdminSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.RoomMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.Booking;
import at.ac.tuwien.sepr.groupphase.backend.entity.CheckIn;
import at.ac.tuwien.sepr.groupphase.backend.entity.CheckOut;
import at.ac.tuwien.sepr.groupphase.backend.entity.Lock;
import at.ac.tuwien.sepr.groupphase.backend.entity.Room;
import at.ac.tuwien.sepr.groupphase.backend.entity.RoomImage;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.BookingRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.CheckInRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.CheckOutRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.LockRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoomRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.RoomService;
import at.ac.tuwien.sepr.groupphase.backend.service.validator.RoomValidator;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class SimpleRoomService implements RoomService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;
    private final RoomValidator roomValidator;
    private final BookingRepository bookingRepository;
    private final CheckInRepository checkInRepository;
    private final CheckOutRepository checkOutRepository;
    private final LockRepository lockRepository;

    public SimpleRoomService(RoomRepository roomRepository, RoomMapper roomMapper, RoomValidator roomValidator,
                             BookingRepository bookingRepository, CheckInRepository checkInRepository,
                             CheckOutRepository checkOutRepository, LockRepository lockRepository) {
        this.roomRepository = roomRepository;
        this.roomMapper = roomMapper;
        this.roomValidator = roomValidator;
        this.bookingRepository = bookingRepository;
        this.checkInRepository = checkInRepository;
        this.checkOutRepository = checkOutRepository;
        this.lockRepository = lockRepository;
    }

    @Transactional
    @Override
    public DetailedRoomDto create(RoomCreateDto roomDto, MultipartFile image, List<MultipartFile> additionalImages) throws ValidationException {
        LOGGER.debug("Create new room {}", roomDto);
        roomValidator.validateForCreate(roomDto, image, additionalImages);

        try {
            Room room = roomMapper.roomCreateDtoToRoom(roomDto, image, additionalImages);
            DetailedRoomDto dto = roomMapper.roomToDetailedRoomDto(roomRepository.save(room), roomDto.smartLockId());
            if (roomDto.smartLockId() != null) {
                Lock lock = Lock.LockBuilder.aLock().withSmartLockId(roomDto.smartLockId()).withRoom(room).build();
                lockRepository.save(lock);
            }
            return dto;
        } catch (IOException e) {
            LOGGER.error("Error creating room", e);
            throw new RuntimeException("Failed to create room due to image processing error", e);
        }
    }


    @Transactional
    @Override
    public DetailedRoomDto update(Long id, RoomUpdateDto roomUpdateDto, MultipartFile mainImage, List<MultipartFile> additionalImages) throws ValidationException {
        LOGGER.debug("Update room with ID {}", id);
        Room existingRoom = roomRepository.findById(id).orElseThrow(() ->
            new NotFoundException("Room with id " + id + " not found"));

        roomValidator.validateForUpdate(roomUpdateDto, mainImage, additionalImages);

        Optional.ofNullable(roomUpdateDto.name()).ifPresent(existingRoom::setName);
        Optional.ofNullable(roomUpdateDto.description()).ifPresent(existingRoom::setDescription);
        Optional.ofNullable(roomUpdateDto.price()).ifPresent(existingRoom::setPrice);
        Optional.ofNullable(roomUpdateDto.capacity()).ifPresent(existingRoom::setCapacity);
        Optional.ofNullable(mainImage).ifPresent(image -> {
            try {
                existingRoom.setMainImage(image.getBytes());
            } catch (IOException e) {
                throw new RuntimeException("Failed to process main image bytes", e);
            }
        });
        if (mainImage == null) {
            existingRoom.setMainImage(null);
        } else {
            try {
                existingRoom.setMainImage(mainImage.getBytes());
            } catch (IOException e) {
                throw new RuntimeException("Failed to process main image bytes", e);
            }
        }

        if (additionalImages == null || additionalImages.isEmpty()) {
            existingRoom.getAdditionalImages().clear();
        } else {
            List<RoomImage> newImages = new ArrayList<>();
            for (MultipartFile file : additionalImages) {
                if (!file.isEmpty()) {
                    try {
                        RoomImage image = new RoomImage();
                        image.setData(file.getBytes());
                        image.setAltText(file.getOriginalFilename());
                        newImages.add(image);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to process additional image bytes", e);
                    }
                }
            }
            existingRoom.getAdditionalImages().clear();
            existingRoom.getAdditionalImages().addAll(newImages);
        }
        Lock lock = Lock.LockBuilder.aLock().withSmartLockId(roomUpdateDto.smartLockId()).withRoom(existingRoom).build();
        Long smartLockId = 0L;
        if (lock != null && lock.getSmartLockId() != null) {
            Lock oldLock = lockRepository.findLockByRoom(existingRoom).orElse(null);
            if (oldLock != null) {
                lockRepository.delete(oldLock);
            }
            lockRepository.save(lock);
            smartLockId = lock.getSmartLockId();
        } else {
            lock = lockRepository.findLockByRoom(existingRoom).orElse(null);
            if (lock != null) {
                lockRepository.delete(lock);
            }
        }

        DetailedRoomDto roomDto = roomMapper.roomToDetailedRoomDto(roomRepository.save(existingRoom), smartLockId);
        return roomDto;
    }

    @Transactional
    @Override
    public RoomListDto updateLastCleanedAt(Long id) throws NotFoundException {
        LOGGER.debug("Updating lastCleanedAt for room with ID {}", id);
        Room room = roomRepository.findById(id).orElseThrow(() ->
            new NotFoundException("Room with id " + id + " not found"));
        room.setLastCleanedAt(LocalDateTime.now());
        roomRepository.save(room);
        return roomMapper.roomToRoomListDto(room);
    }

    @Transactional
    @Override
    public RoomListDto updateCleaningTime(Long id, RoomCleaningTimeDto cleaningTimeDto) throws ValidationException {
        LOGGER.debug("Updating cleaning time for room with ID {}", id);
        roomValidator.validateForCleaningTimes(cleaningTimeDto);

        Room room = roomRepository.findById(id).orElseThrow(() ->
            new NotFoundException("Room with id " + id + " not found"));

        LocalDate today = LocalDate.now();
        //LocalDate tomorrow = today.plusDays(1);

        LocalTime cleaningTimeFrom = LocalTime.parse(cleaningTimeDto.cleaningTimeFrom());
        LocalTime cleaningTimeTo = LocalTime.parse(cleaningTimeDto.cleaningTimeTo());

        LocalDateTime cleaningFromDateTime = LocalDateTime.of(today, cleaningTimeFrom);
        LocalDateTime cleaningToDateTime = LocalDateTime.of(today, cleaningTimeTo);

        /*if (cleaningFromDateTime.isBefore(LocalDateTime.now())) {
            cleaningFromDateTime = LocalDateTime.of(tomorrow, cleaningTimeFrom);
            cleaningToDateTime = LocalDateTime.of(tomorrow, cleaningTimeTo);
        }*/

        room.setCleaningTimeFrom(cleaningFromDateTime);
        room.setCleaningTimeTo(cleaningToDateTime);

        roomRepository.save(room);

        return roomMapper.roomToRoomListDto(room);
    }

    @Transactional
    @Override
    public RoomListDto deleteCleaningTime(Long id) throws NotFoundException {
        LOGGER.debug("Deleting cleaning time for room with ID {}", id);

        Room room = roomRepository.findById(id).orElseThrow(() ->
            new NotFoundException("Room with id " + id + " not found"));

        room.setCleaningTimeFrom(null);
        room.setCleaningTimeTo(null);

        roomRepository.save(room);

        return roomMapper.roomToRoomListDto(room);
    }


    @Override
    public Page<RoomListDto> search(RoomSearchDto roomSearchDto, Pageable pageable) throws ValidationException {
        LOGGER.debug("Search for rooms with criteria: {}", roomSearchDto);
        roomValidator.validateForSearch(roomSearchDto);
        return roomMapper.roomToRoomListDto(roomRepository.findRoomsByCriteria(
            roomSearchDto.startDate(),
            roomSearchDto.endDate(),
            roomSearchDto.maxPrice(),
            roomSearchDto.minPrice(),
            roomSearchDto.capacity(),
            pageable));
    }

    @Transactional
    @Override
    public DetailedRoomDto findOne(Long id) {
        Room room = roomRepository.findRoomById(id);
        if (room == null) {
            throw new NotFoundException("Room with id " + id + " not found");
        }
        Lock lock = lockRepository.findLockByRoom(room).orElse(null);
        Long smartLockId = null;
        if (lock != null) {
            smartLockId = lock.getSmartLockId();
        }

        return roomMapper.roomToDetailedRoomDto(room, smartLockId);
    }

    @Override
    public Page<RoomListDto> findAll(Pageable pageable) {
        Page<RoomListDto> returnValue = roomMapper.roomToRoomListDto(roomRepository.findAllByOrderByPriceAsc(pageable));

        LOGGER.debug("list of rooms: {}", returnValue.stream()
            .map(room -> String.format("\nRoom(id=%d, name=%s, price=%.2f, capacity=%d, lastCleanedAt=%s, cleaningTimeFrom=%s, , cleaningTimeTo=%s)",
                room.id(),
                room.name(),
                room.price(),
                room.capacity(),
                room.lastCleanedAt() != null ? room.lastCleanedAt().toString() : "null",
                room.cleaningTimeFrom() != null ? room.cleaningTimeFrom().toString() : "null",
                room.cleaningTimeTo() != null ? room.cleaningTimeTo().toString() : "null"))
            .toList());
        return returnValue;
    }

    @Override
    public Page<RoomListDto> findAllForClean(Pageable pageable) {
        Page<RoomListDto> returnValue = roomMapper.roomToRoomListDto(roomRepository.findAllRoomsCleaning(pageable));

        LOGGER.debug("list of rooms: {}", returnValue.stream()
            .map(room -> String.format("\nRoom(id=%d, name=%s, price=%.2f, capacity=%d, lastCleanedAt=%s, cleaningTimeFrom=%s, , cleaningTimeTo=%s)",
                room.id(),
                room.name(),
                room.price(),
                room.capacity(),
                room.lastCleanedAt() != null ? room.lastCleanedAt().toString() : "null",
                room.cleaningTimeFrom() != null ? room.cleaningTimeFrom().toString() : "null",
                room.cleaningTimeTo() != null ? room.cleaningTimeTo().toString() : "null"))
            .toList());
        return returnValue;
    }

    @Override
    public ResponseEntity<byte[]> getMainImage(Long id) {
        Room room = roomRepository.findRoomById(id);
        if (room == null) {
            throw new NotFoundException("Room with id " + id + " not found");
        }
        byte[] image = room.getMainImage();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "image/*");

        return new ResponseEntity<>(image, headers, org.springframework.http.HttpStatus.OK);
    }

    @Override
    public void delete(Long id) throws NotFoundException, ConflictException {
        LOGGER.debug("Delete room {}", id);
        if (!bookingRepository.findBookingsByRoomIdAndStartDateBetween(id, LocalDate.now(), LocalDate.now().plusYears(9999)).isEmpty()) {
            throw new ConflictException("Cannot delete room since there is an upcoming booking.", Collections.singletonList(""));
        }
        if (!bookingRepository.findBookingsByRoomIdAndStartDateBetween(id, LocalDate.now().minusYears(9999), LocalDate.now()).isEmpty()) {
            // Clear past related Bookings, Check-ins and Check-outs.
            List<Booking> bookings = bookingRepository.findBookingsByRoomIdAndStartDateBetween(id, LocalDate.now().minusYears(9999), LocalDate.now());
            List<CheckIn> checkIns = new ArrayList<>();
            for (Booking booking : bookings) {
                List<CheckIn> additionalCheckIns = checkInRepository.findCheckInByBooking(booking);
                checkIns.addAll(additionalCheckIns);
            }
            List<CheckOut> checkOuts = new ArrayList<>();
            for (Booking booking : bookings) {
                List<CheckOut> additionalCheckOuts = checkOutRepository.findCheckOutByBooking(booking);
                checkOuts.addAll(additionalCheckOuts);
            }
            for (CheckIn checkIn : checkIns) {
                checkInRepository.delete(checkIn);
            }
            for (CheckOut checkOut : checkOuts) {
                checkOutRepository.delete(checkOut);
            }
            for (Booking booking : bookings) {
                bookingRepository.delete(booking);
            }
        }
        Room existingRoom = roomRepository.findById(id).orElseThrow(() ->
            new NotFoundException("Room with id " + id + " not found"));
        Lock lock = lockRepository.findLockByRoom(existingRoom).orElse(null);
        if (lock != null) {
            lockRepository.delete(lock);
        }
        roomRepository.delete(existingRoom);
    }

    @Override
    public Page<RoomListDto> adminSearch(RoomAdminSearchDto roomAdminSearchDto, Pageable pageable) throws ValidationException {
        LOGGER.debug("Search for rooms with admin criteria: {}", roomAdminSearchDto);
        roomValidator.validateForAdminSearch(roomAdminSearchDto);
        return roomMapper.roomToRoomListDto(roomRepository.findRoomsByAdminCriteria(
            roomAdminSearchDto.name(),
            roomAdminSearchDto.minPrice(),
            roomAdminSearchDto.maxPrice(),
            roomAdminSearchDto.minCapacity(),
            roomAdminSearchDto.maxCapacity(),
            roomAdminSearchDto.description(),
            pageable));
    }
}
