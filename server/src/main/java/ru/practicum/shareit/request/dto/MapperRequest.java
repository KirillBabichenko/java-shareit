package ru.practicum.shareit.request.dto;

import ru.practicum.shareit.request.ItemRequest;

import java.time.LocalDateTime;

public class MapperRequest {

    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .requestorId(itemRequest.getRequestorId())
                .build();
    }

    public static ItemRequest toItemRequest(ItemRequestDto itemRequestDto, Long idUser, LocalDateTime dateTime) {
        return ItemRequest.builder()
                .id(itemRequestDto.getId())
                .description(itemRequestDto.getDescription())
                .created(dateTime)
                .requestorId(idUser)
                .build();
    }
}
