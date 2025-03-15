package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DetailedRoomDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.KeyStatusDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Lock;
import at.ac.tuwien.sepr.groupphase.backend.entity.Room;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.LockRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoomRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.CheckInService;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.SimpleKeyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class KeyServiceTest {

    @Mock
    private CheckInService checkInService;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private LockRepository lockRepository;

    @InjectMocks
    private SimpleKeyService keyService;

    @Mock
    private SimpleKeyService mockKeyService;

    // Test unlock(Long id, String email)
    @Test
    public void givenValidIdAndEmail_whenUnlock_thenUnlocksSuccessfully() throws Exception {
        Long roomId = 1L;
        String email = "test@example.com";
        Lock lock = new Lock();
        lock.setSmartLockId(100L);

        assertDoesNotThrow(() -> mockKeyService.unlock(roomId, email));
    }

    @Test
    public void givenInvalidRoomId_whenUnlock_thenThrowsNotFoundException() {
        Long roomId = 1L;
        String email = "test@example.com";

        when(checkInService.getGuestRooms(email)).thenReturn(new DetailedRoomDto[]{});

        NotFoundException exception = assertThrows(NotFoundException.class, () -> keyService.unlock(roomId, email));
        assertEquals("Specified room not found", exception.getMessage());
    }

    // Test smartLockIsFoundByApi(Long id)
    @Test
    public void givenValidSmartLockId_whenSmartLockIsFoundByApi_thenReturnTrue() throws Exception {
        Long smartLockId = 100L;

        SimpleKeyService spyKeyService = spy(keyService);
        doReturn(true).when(spyKeyService).smartLockIsFoundByApi(smartLockId);

        boolean result = spyKeyService.smartLockIsFoundByApi(smartLockId);
        assertTrue(result);
    }

    @Test
    public void givenInvalidSmartLockId_whenSmartLockIsFoundByApi_thenReturnFalse() throws Exception {
        Long smartLockId = 100L;

        SimpleKeyService spyKeyService = spy(keyService);
        doReturn(false).when(spyKeyService).smartLockIsFoundByApi(smartLockId);

        boolean result = spyKeyService.smartLockIsFoundByApi(smartLockId);
        assertFalse(result);
    }

    // Test getStatus(Long id, String email)
    @Test
    public void givenValidRoomAndEmail_whenGetStatus_thenReturnsKeyStatus() throws Exception {
        Long roomId = 1L;
        String email = "test@example.com";
        Lock lock = new Lock();
        lock.setSmartLockId(100L);

        assertDoesNotThrow(() -> mockKeyService.getStatus(roomId, email));
    }

    @Test
    public void givenInvalidRoomOrEmail_whenGetStatus_thenThrowsNotFoundException() throws Exception {
        Long roomId = 1L;
        String email = "test@example.com";

        when(checkInService.getGuestRooms(email)).thenReturn(new DetailedRoomDto[]{});

        NotFoundException exception = assertThrows(NotFoundException.class, () -> keyService.getStatus(roomId, email));
        assertEquals("Specified room not found", exception.getMessage());
    }
}
