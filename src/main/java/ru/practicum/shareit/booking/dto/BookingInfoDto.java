package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookingInfoDto {
    private Long id;
    private Long bookerId;
}
