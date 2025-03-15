package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.GuestCreateUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.GuestDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.GuestListDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.GuestSignupDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.SimpleGuestDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Guest;
import at.ac.tuwien.sepr.groupphase.backend.enums.Gender;
import at.ac.tuwien.sepr.groupphase.backend.enums.Nationality;
import at.ac.tuwien.sepr.groupphase.backend.enums.RoleType;
import org.mapstruct.Mapper;

/**
 * Mapper for converting between Guest and Guest DTOs.
 */
@Mapper(componentModel = "spring")
public interface GuestMapper {

    default SimpleGuestDto guestToSimpleGuestDto(Guest guest) {
        if (guest == null) {
            return null;
        }

        return new SimpleGuestDto(
            guest.getEmail()
        );
    }

    default Guest guestSignupDtoToGuest(GuestSignupDto guestSignupDto) {
        if (guestSignupDto == null) {
            return null;
        }

        Guest guest = new Guest();
        guest.setEmail(guestSignupDto.email());
        guest.setPassword(guestSignupDto.password());
        guest.setRoleType(RoleType.ROLE_GUEST);
        return guest;
    }

    default GuestListDto guestToGuestListDto(Guest guest) {
        if (guest == null) {
            return null;
        }

        return new GuestListDto(
            guest.getFirstName(),
            guest.getLastName(),
            guest.getEmail()
        );
    }

    default GuestDetailDto guestToGuestDetailDto(Guest guest) {
        if (guest == null) {
            return null;
        }

        return new GuestDetailDto(
            guest.getFirstName(),
            guest.getLastName(),
            guest.getEmail(),
            guest.getDateOfBirth(),
            guest.getPlaceOfBirth(),
            String.valueOf(guest.getGender()),
            String.valueOf(guest.getNationality()),
            guest.getAddress(),
            guest.getPassportNumber(),
            guest.getPhoneNumber(),
            guest.getPassword()
        );
    }


    default Guest guestCreateUpdateDtoToGuest(GuestCreateUpdateDto guestDto) {
        if (guestDto == null) {
            return null;
        }

        Guest guest = new Guest();

        guest.setFirstName(guestDto.firstName());
        guest.setLastName(guestDto.lastName());
        guest.setEmail(guestDto.email());
        guest.setDateOfBirth(guestDto.dateOfBirth());
        guest.setPlaceOfBirth(guestDto.placeOfBirth());
        guest.setAddress(guestDto.address());
        guest.setPassportNumber(guestDto.passportNumber());
        guest.setPhoneNumber(guestDto.phoneNumber());
        guest.setPassword(guestDto.password());
        guest.setRoleType(RoleType.ROLE_GUEST);

        if (guestDto.gender() != null && !guestDto.gender().isBlank()) {
            guest.setGender(Gender.valueOf(guestDto.gender()));
        } else {
            guest.setGender(null);
        }
        if (guestDto.nationality() != null && !guestDto.nationality().isBlank()) {
            guest.setNationality(Nationality.valueOf(guestDto.nationality()));
        } else {
            guest.setNationality(null);
        }
        return guest;
    }
}
