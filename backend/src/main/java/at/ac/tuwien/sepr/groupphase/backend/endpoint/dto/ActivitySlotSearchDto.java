package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.time.LocalDate;

public record ActivitySlotSearchDto(
    LocalDate date,
    Integer participants
) {

}
