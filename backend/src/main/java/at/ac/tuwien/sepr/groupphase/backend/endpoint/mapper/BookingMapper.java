package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DetailedBookingDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.BookingCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EmployeeBookingDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.GuestDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Booking;
import at.ac.tuwien.sepr.groupphase.backend.entity.Guest;
import at.ac.tuwien.sepr.groupphase.backend.entity.Room;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Named("detailedBooking")
    default DetailedBookingDto bookingToDetailedBookingDto(Booking booking) {
        if (booking == null) {
            return null;
        }

        Room room = booking.getRoom();
        ApplicationUser user = booking.getUser();
        return new DetailedBookingDto(
            booking.getId(),
            room != null ? room.getId() : null,
            user != null ? user.getId() : null,
            booking.getStartDate(),
            booking.getEndDate(),
            room != null ? room.getName() : null,
            room != null ? room.getPrice() : null,
            booking.isPaid(),
            booking.getStatus().name(),
            booking.getBookingNumber(),
            booking.getBookingDate(),
            booking.getTotalAmount(),
            booking.getNumberOfNights(),
            booking.getStripePaymentIntentId()
        );
    }

    @IterableMapping(qualifiedByName = "detailedBooking")
    default List<DetailedBookingDto> bookingsToDetailedBookingDtos(List<Booking> bookings) {
        if (bookings == null) {
            return null;
        }
        return bookings.stream()
            .map(this::bookingToDetailedBookingDto)
            .collect(Collectors.toList());
    }

    @Named("employeeBooking")
    default EmployeeBookingDto bookingToEmployeeBookingDto(Booking booking) {
        if (booking == null) {
            return null;
        }

        Room room = booking.getRoom();
        ApplicationUser user = booking.getUser();
        boolean isActive = LocalDate.now().isEqual(booking.getStartDate())
            || LocalDate.now().isEqual(booking.getEndDate())
            || (LocalDate.now().isAfter(booking.getStartDate()) && LocalDate.now().isBefore(booking.getEndDate()));
        long daysBooked = java.time.temporal.ChronoUnit.DAYS.between(booking.getStartDate(), booking.getEndDate());
        double totalPrice = daysBooked * booking.getRoom().getPrice();


        String firstName = null;
        String lastName = null;
        String address = null;
        String dateOfBirth = null;
        String nationality = null;
        String passportNumber = null;
        String phoneNumber = null;
        String gender = null;
        String placeOfBirth = null;

        if (user instanceof Guest guest) {
            firstName = guest.getFirstName();
            lastName = guest.getLastName();
            address = guest.getAddress();
            dateOfBirth = String.valueOf(guest.getDateOfBirth());
            nationality = guest.getNationality() != null ? guest.getNationality().name() : null;
            passportNumber = guest.getPassportNumber();
            phoneNumber = guest.getPhoneNumber();
            gender = String.valueOf(guest.getGender());
            placeOfBirth = guest.getPlaceOfBirth();
        }


        return new EmployeeBookingDto(
            booking.getId(),
            room != null ? room.getId() : null,
            user.getId(),
            booking.getStartDate(),
            booking.getEndDate(),
            room != null ? room.getName() : null,
            user.getEmail(),
            totalPrice,
            isActive,
            firstName,
            lastName,
            address,
            dateOfBirth,
            nationality,
            passportNumber,
            phoneNumber,
            gender,
            placeOfBirth,
            room != null ? room.getCapacity() : null,
            room != null && room.getLastCleanedAt() != null ? LocalDateTime.from(room.getLastCleanedAt()) : null,
            booking.isPaid(),
            booking.getStatus().name(),
            booking.getBookingNumber(),
            booking.getBookingDate(),
            booking.getTotalAmount(),
            booking.getNumberOfNights(),
            booking.getStripePaymentIntentId()
        );
    }

    @IterableMapping(qualifiedByName = "employeeBooking")
    default List<EmployeeBookingDto> bookingsToEmployeeBookingDtos(List<Booking> bookings) {
        if (bookings == null) {
            return null;
        }
        return bookings.stream()
            .map(this::bookingToEmployeeBookingDto)
            .collect(Collectors.toList());
    }


}
