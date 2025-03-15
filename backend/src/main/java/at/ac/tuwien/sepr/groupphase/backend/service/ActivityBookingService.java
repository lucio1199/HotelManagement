package at.ac.tuwien.sepr.groupphase.backend.service;


import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ActivityBookingCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ActivityBookingDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;

import java.util.List;

public interface ActivityBookingService {

    ActivityBookingDto createBooking(ActivityBookingCreateDto bookingDto, String email) throws NotFoundException, ValidationException;

    void updatePaymentStatus(Long bookingId) throws NotFoundException;

    List<ActivityBookingDto> findByUserEmail(String email) throws NotFoundException;

}
