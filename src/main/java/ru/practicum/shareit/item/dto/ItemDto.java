package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.booking.dto.BookingInfoDto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@Builder
public class ItemDto {
    private Long id;
    @NotEmpty
    private String name;
    @NotEmpty
    private String description;
    @NotNull
    private Boolean available;
    private Long idRequest;
    private BookingInfoDto lastBooking;
    private BookingInfoDto nextBooking;
    private List<CommentDto> comments;
}
