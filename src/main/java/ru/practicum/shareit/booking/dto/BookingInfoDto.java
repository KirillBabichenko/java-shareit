package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BookingInfoDto {
    private Long id;
    private Long bookerId;
}
