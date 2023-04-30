package ru.practicum.shareit.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.item.ItemDto;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class ItemRequestDto {
    private Long id;
    @NotEmpty
    private String description;
    private LocalDateTime created;
    private Long requestorId;
    private List<ItemDto> items;
}
