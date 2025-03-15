package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DetailedRoomDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RoomCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.RoomMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.Lock;
import at.ac.tuwien.sepr.groupphase.backend.entity.Room;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoomRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.SimpleRoomService;
import at.ac.tuwien.sepr.groupphase.backend.service.validator.RoomValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomValidator roomValidator;

    @Mock
    private RoomMapper roomMapper;

    @InjectMocks
    private SimpleRoomService roomService;

    @Test
    public void givenRoom_whenCreate_thenSaveAndReturnRoom() throws ValidationException, IOException {
        RoomCreateDto roomCreateDto = new RoomCreateDto("Deluxe Room", "This is a deluxe room", 100.0, 2, null);
        Room room = new Room();
        room.setName("Deluxe Room");

        when(roomRepository.save(any(Room.class))).thenReturn(room);

        doNothing().when(roomValidator).validateForCreate(eq(roomCreateDto), eq(null), eq(null));

        when(roomMapper.roomCreateDtoToRoom(eq(roomCreateDto), eq(null), eq(null))).thenReturn(room);
        when(roomMapper.roomToDetailedRoomDto(eq(room), eq(null))).thenReturn(new DetailedRoomDto(
            1L, "Deluxe Room", "This is a deluxe room", 100.0, 2, null, new ArrayList<>(), null));

        DetailedRoomDto savedRoom = roomService.create(roomCreateDto, null, null);

        verify(roomRepository, times(1)).save(any(Room.class));
        assertEquals("Deluxe Room", savedRoom.name());
    }

    @Test
    public void givenInvalidId_whenFindOne_thenThrowNotFoundException() {
        when(roomRepository.findRoomById(anyLong())).thenReturn(null);

        assertThrows(NotFoundException.class, () -> roomService.findOne(999L));

        verify(roomRepository, times(1)).findRoomById(999L);
    }
}
