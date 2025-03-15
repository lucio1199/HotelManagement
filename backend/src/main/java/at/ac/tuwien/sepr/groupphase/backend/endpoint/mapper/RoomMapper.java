package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DetailedRoomDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RoomCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RoomListDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RoomUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Room;
import at.ac.tuwien.sepr.groupphase.backend.entity.RoomImage;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Mapper
public interface RoomMapper {
    @Named("roomList")
    default RoomListDto roomToRoomListDto(Room room) {
        if (room == null) {
            return null;
        }

        String mainImage = (room.getMainImage() != null)
            ? room.getMainImageAsString()
            : null;

        return new RoomListDto(
            room.getId(),
            room.getName(),
            room.getPrice(),
            room.getCapacity(),
            room.getLastCleanedAt(),
            room.getCleaningTimeFrom(),
            room.getCleaningTimeTo(),
            mainImage
        );
    }

    @IterableMapping(qualifiedByName = "roomList")
    default Page<RoomListDto> roomToRoomListDto(Page<Room> rooms) {
        if (rooms == null) {
            return Page.empty();
        }
        return rooms.map(this::roomToRoomListDto);
    }

    default DetailedRoomDto roomToDetailedRoomDto(Room room, Long smartLockId) {
        if (room == null) {
            return null;
        }

        String mainImage = (room.getMainImage() != null)
            ? Base64.getEncoder().encodeToString(room.getMainImage())
            : null;

        List<String> additionalImages = Optional.ofNullable(room.getAdditionalImages())
            .orElse(Collections.emptyList())
            .stream()
            .map(image -> Base64.getEncoder().encodeToString(image.getData()))
            .collect(Collectors.toList());

        return new DetailedRoomDto(
            room.getId(),
            room.getName(),
            room.getDescription(),
            room.getPrice(),
            room.getCapacity(),
            mainImage,
            additionalImages,
            smartLockId
        );
    }

    default Room roomCreateDtoToRoom(RoomCreateDto roomDto, MultipartFile mainImage, List<MultipartFile> additionalImages) throws IOException {
        if (roomDto == null) {
            return null;
        }

        Room room = new Room();
        room.setName(roomDto.name());
        room.setDescription(roomDto.description());
        room.setPrice(roomDto.price());
        room.setCapacity(roomDto.capacity());

        room.setCreatedAt(LocalDateTime.now());

        room.setLastCleanedAt(LocalDateTime.now());

        if (mainImage != null && !mainImage.isEmpty()) {
            room.setMainImage(mainImage.getBytes());
        }

        if (additionalImages != null) {
            List<RoomImage> images = new ArrayList<>();
            for (MultipartFile image : additionalImages) {
                RoomImage roomImage = new RoomImage();
                roomImage.setData(image.getBytes());
                images.add(roomImage);
            }
            room.setAdditionalImages(images);
        }
        room.setHalfBoard(false);

        return room;
    }

    default Room detailedRoomDtoToRoom(DetailedRoomDto detailedRoomDto) {
        if (detailedRoomDto == null) {
            return null;
        }

        Room room = new Room();
        room.setId(detailedRoomDto.id());
        room.setName(detailedRoomDto.name());
        room.setDescription(detailedRoomDto.description());
        room.setPrice(detailedRoomDto.price());
        room.setCapacity(detailedRoomDto.capacity());
        room.setMainImage(Base64.getDecoder().decode(detailedRoomDto.mainImage()));
        if (detailedRoomDto.additionalImages() != null) {
            List<RoomImage> images = new ArrayList<>();
            for (String image : detailedRoomDto.additionalImages()) {
                RoomImage roomImage = new RoomImage();
                roomImage.setData(Base64.getDecoder().decode(image));
                images.add(roomImage);
            }
            room.setAdditionalImages(images);
        }
        return room;
    }

    default Room roomUpdateDtoToRoom(RoomUpdateDto updateDto) throws IOException {
        if (updateDto == null) {
            return null;
        }

        Room room = new Room();
        room.setId(updateDto.id());
        if (updateDto.name() != null) {
            room.setName(updateDto.name());
        }
        if (updateDto.description() != null) {
            room.setDescription(updateDto.description());
        }
        if (updateDto.price() != null) {
            room.setPrice(updateDto.price());
        }
        if (updateDto.capacity() != null) {
            room.setCapacity(updateDto.capacity());
        }

        return room;
    }
}
