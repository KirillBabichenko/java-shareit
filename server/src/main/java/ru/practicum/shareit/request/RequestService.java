package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface RequestService {

    ItemRequestDto createRequest(Long idUser, ItemRequestDto itemRequestDto);

    List<ItemRequestDto> getAllRequestsById(Long idUser);

    List<ItemRequestDto> getAllRequests(Long idUser, Integer from, Integer size);

    ItemRequestDto getRequestById(Long idUser, Long requestId);
}
