package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.KeyStatusDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;

import java.io.IOException;

public interface KeyService {

    /**
     * Unlocks a Door.
     *
     * @param id The room ID.
     * @param email The guest email.
     * @throws NotFoundException If the guest or room is not found.
     */
    void unlock(Long id, String email) throws NotFoundException, IOException;

    /**
     * Checks if a smart lock is found by the Nuki API.
     *
     * @param id The room ID.
     * @return If the smart lock is found.
     * @throws IOException If the API call malfunctioned.
     */
    boolean smartLockIsFoundByApi(Long id) throws IOException;

    /**
     * Unlocks a Door.
     *
     * @param id The room ID.
     * @param email The guest email.
     * @return the KeyStatus of the requested room.
     * @throws NotFoundException If the guest or room is not found.
     * @throws IOException If the API call malfunctioned.
     */
    KeyStatusDto getStatus(Long id, String email) throws NotFoundException, IOException;
}
