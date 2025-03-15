package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.config.TestSecurityConfig;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DetailedRoomDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RoomUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.RoomMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.Room;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public class RoomMappingTest implements TestData {

    private final Room room = new Room();

    {
        room.setId(TEST_ROOM_ID);
        room.setName(TEST_ROOM_NAME);
        room.setDescription(TEST_ROOM_DESCRIPTION);
        room.setPrice(TEST_ROOM_PRICE);
        room.setCapacity(TEST_ROOM_CAPACITY);
        room.setMainImage(TEST_ROOM_MAIN_IMAGE);
    }

    @Autowired
    private RoomMapper roomMapper;

    @Test
    public void givenRoom_whenMapToDetailedRoomDto_thenDtoHasAllProperties() {
        DetailedRoomDto detailedRoomDto = roomMapper.roomToDetailedRoomDto(room, null);
        assertAll(
            () -> assertEquals(TEST_ROOM_ID, detailedRoomDto.id()),
            () -> assertEquals(TEST_ROOM_NAME, detailedRoomDto.name()),
            () -> assertEquals(TEST_ROOM_DESCRIPTION, detailedRoomDto.description()),
            () -> assertEquals(TEST_ROOM_CAPACITY, detailedRoomDto.capacity()),
            () -> assertEquals(TEST_ROOM_PRICE, detailedRoomDto.price()),
            () -> assertArrayEquals(TEST_ROOM_MAIN_IMAGE, Base64.getDecoder().decode(detailedRoomDto.mainImage()))
        );
    }


    @Test
    public void givenDetailedRoomDto_whenMapToEntity_thenEntityHasAllProperties() {
        DetailedRoomDto detailedRoomDto = new DetailedRoomDto(
            TEST_ROOM_ID,
            TEST_ROOM_NAME,
            TEST_ROOM_DESCRIPTION,
            TEST_ROOM_PRICE,
            TEST_ROOM_CAPACITY,
            TEST_ROOM_MAIN_IMAGE_STRING_BASE64,
            TEST_ROOM_ADDITIONAL_IMAGES_AS_STRING,
            null);

        Room mappedRoom = roomMapper.detailedRoomDtoToRoom(detailedRoomDto);
        assertAll(
            () -> assertEquals(TEST_ROOM_ID, mappedRoom.getId()),
            () -> assertEquals(TEST_ROOM_NAME, mappedRoom.getName()),
            () -> assertEquals(TEST_ROOM_DESCRIPTION, mappedRoom.getDescription()),
            () -> assertEquals(TEST_ROOM_CAPACITY, mappedRoom.getCapacity()),
            () -> assertEquals(TEST_ROOM_PRICE, mappedRoom.getPrice()),
            () -> assertArrayEquals(TEST_ROOM_MAIN_IMAGE, mappedRoom.getMainImage())
        );
    }

    @Test
    public void givenRoomUpdateDto_whenMapToEntity_thenEntityHasAllProperties() throws IOException {
        RoomUpdateDto updateDto = new RoomUpdateDto(TEST_ROOM_ID, TEST_ROOM_NAME, TEST_ROOM_DESCRIPTION, TEST_ROOM_PRICE, TEST_ROOM_CAPACITY, null);


        Room mappedRoom = roomMapper.roomUpdateDtoToRoom(updateDto);

        assertAll(
            () -> assertEquals(TEST_ROOM_ID, mappedRoom.getId()),
            () -> assertEquals(TEST_ROOM_NAME, mappedRoom.getName()),
            () -> assertEquals(TEST_ROOM_DESCRIPTION, mappedRoom.getDescription()),
            () -> assertEquals(TEST_ROOM_PRICE, mappedRoom.getPrice()),
            () -> assertEquals(TEST_ROOM_CAPACITY, mappedRoom.getCapacity())
        );
    }
}
