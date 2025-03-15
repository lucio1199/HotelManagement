package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CheckInDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CheckInStatusDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CheckOutDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Booking;
import at.ac.tuwien.sepr.groupphase.backend.entity.CheckIn;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.CheckOut;
import org.mapstruct.Mapper;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CheckInMapper {
    default CheckIn checkInDtoToCheckIn(CheckInDto checkInDto, Booking booking, ApplicationUser guest, MultipartFile passport) throws IOException {
        if (checkInDto == null || booking == null || guest == null || passport == null) {
            return null;
        }

        return new CheckIn(booking, LocalDateTime.now(), guest, passport.getBytes());
    }

    default CheckOut checkOutDtoToCheckOut(CheckOutDto checkOutDto, Booking booking, ApplicationUser guest) {
        if (checkOutDto == null || booking == null || guest == null) {
            return null;
        }

        return new CheckOut(booking, LocalDateTime.now(), guest);
    }

    default CheckInStatusDto checkInToCheckInStatusDto(CheckIn checkIn) {
        if (checkIn == null) {
            return null;
        }
        if (checkIn.getGuest() == null) {
            return new CheckInStatusDto(checkIn.getBookingId(), "invalid");  // for handling bookings where the guest had already checked in
        }
        return new CheckInStatusDto(checkIn.getBookingId(), checkIn.getGuest().getEmail());
    }

    default List<CheckInStatusDto> checkInsToCheckInStatusDtos(List<CheckIn> checkIns) {
        if (checkIns == null || checkIns.isEmpty()) {
            return List.of();
        }
        return checkIns.stream()
            .map(this::checkInToCheckInStatusDto)
            .toList();
    }
}
