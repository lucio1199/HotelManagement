package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DetailedRoomDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.KeyStatusDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Lock;
import at.ac.tuwien.sepr.groupphase.backend.entity.Room;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.LockRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoomRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.CheckInService;
import at.ac.tuwien.sepr.groupphase.backend.service.KeyService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

@Service
public class SimpleKeyService implements KeyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final CheckInService checkInService;
    private final RoomRepository roomRepository;
    private final LockRepository lockRepository;

    private static final String API_STATUS_URL = "https://api.nuki.io/smartlock/{smartLockId}";
    private static final String API_UNLOCK_URL = "https://api.nuki.io/smartlock/{smartLockId}/action/unlock";

    @Value("${spring.nuki.api_token}")
    private String apiToken;


    public SimpleKeyService(CheckInService checkInService, RoomRepository roomRepository, LockRepository lockRepository) {
        this.checkInService = checkInService;
        this.roomRepository = roomRepository;
        this.lockRepository = lockRepository;
    }

    @Override
    @Transactional
    public void unlock(Long id, String email) throws NotFoundException, IOException {
        LOGGER.debug("Unlock id: {}, email: {}", id, email);

        KeyStatusDto dto = getStatus(id, email);
        if (dto.status() == null || dto.smartLockId() == null) {
            throw new NotFoundException("Smart lock not found");
        }

        if (Objects.equals(dto.status(), "available")) {
            HttpURLConnection connection = null;
            try {
                // Replace the placeholder in the URL with the actual smartLockId
                String endpoint = API_UNLOCK_URL.replace("{smartLockId}", dto.smartLockId().toString());

                // Set up the connection
                URL url = new URL(endpoint);
                connection = (HttpURLConnection) url.openConnection();

                // Configure the HTTP request
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", "Bearer " + apiToken);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                // Prepare the body
                String body = "{}"; // Empty body for unlock action

                // Write the body to the request
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = body.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                // Read the response
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                        StringBuilder response = new StringBuilder();
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                        LOGGER.debug("Unlock successful: {}", response.toString());
                    }
                } else {
                    LOGGER.error("Unlock failed. HTTP Code {}", responseCode);
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"))) {
                        StringBuilder response = new StringBuilder();
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                        LOGGER.error("Error response: {}", response.toString());
                    }
                }
            } catch (IOException e) {
                throw new IOException("There were problems opening the door, please try again later.");
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
    }

    @Override
    @Transactional
    public boolean smartLockIsFoundByApi(Long id) throws IOException {
        HttpURLConnection connection = null;
        try {
            // Replace the placeholder in the URL with the actual smartLockId
            String endpoint = API_STATUS_URL.replace("{smartLockId}", id.toString());

            // Set up the connection
            URL url = new URL(endpoint);
            connection = (HttpURLConnection) url.openConnection();

            // Configure the HTTP request
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + apiToken);

            // Read the response
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Log success response
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    LOGGER.debug("Status received successfully: {}", response.toString());
                }
                return true; // Smart lock was found
            } else {
                // Log error response
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        errorResponse.append(responseLine.trim());
                    }
                    LOGGER.warn("Error fetching smart lock status. HTTP Code: {}, Response: {}", responseCode, errorResponse.toString());
                }
            }
        } catch (IOException e) {
            LOGGER.error("Error while checking smart lock status", e);
            throw new IOException("There were problems checking the smart lock status. Please try again later.");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return false; // Smart lock not found
    }

    @Override
    @Transactional
    public KeyStatusDto getStatus(Long id, String email) throws NotFoundException, IOException {
        LOGGER.debug("Get status for id: {}, email: {}", id, email);
        Room room = null;

        DetailedRoomDto[] roomDtos = checkInService.getGuestRooms(email);
        for (DetailedRoomDto roomDto : roomDtos) {
            if (Objects.equals(roomDto.id(), id)) {
                room = roomRepository.findRoomById(id);
            }
        }

        if (room == null) {
            throw new NotFoundException("Specified room not found");
        }
        Lock lock = lockRepository.findLockByRoom(room).orElseThrow(() -> new NotFoundException("Smart lock not found"));
        if (lock == null) {
            return new KeyStatusDto(room.getId(), null, "unavailable");
        }
        if (smartLockIsFoundByApi(lock.getSmartLockId())) {
            return new KeyStatusDto(room.getId(), lock.getSmartLockId(), "available");
        }
        return new KeyStatusDto(room.getId(), null, "unavailable");
    }
}
