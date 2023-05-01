package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.MissingIdException;
import ru.practicum.shareit.item.ItemRepositoryJpa;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.MapperRequest;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.request.dto.MapperRequest.toItemRequest;
import static ru.practicum.shareit.request.dto.MapperRequest.toItemRequestDto;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository repository;
    private final UserService userService;
    private final ItemRepositoryJpa itemRepositoryJpa;

    /**
     * Создать новый запрос на вещь
     */
    @Transactional
    public ItemRequestDto createRequest(Long idUser, ItemRequestDto itemRequestDto) {
        LocalDateTime dateTime = LocalDateTime.now();
        ItemRequest itemRequest = toItemRequest(itemRequestDto, userService.getUserById(idUser).getId(), dateTime);
        return toItemRequestDto(repository.save(itemRequest));
    }

    /**
     * Получить список запросов созданных пользователем с id
     */
    public List<ItemRequestDto> getAllRequestsById(Long idUser) {
        checkUser(idUser);
        List<ItemRequest> itemRequests = repository.findByRequestorIdOrderByCreatedAsc(idUser);
        return itemRequests.stream()
                .map(MapperRequest::toItemRequestDto)
                .peek(itemRequestDto -> itemRequestDto.setItems(itemRepositoryJpa.findByRequestId(itemRequestDto.getId())))
                .collect(Collectors.toList());
    }

    /**
     * Получить список всех запросов кроме своих
     */
    public List<ItemRequestDto> getAllRequests(Long idUser, Integer from, Integer size) {
        checkUser(idUser);
        PageRequest page = PageRequest.of(from, size);
        List<ItemRequest> itemRequests = repository.findByRequestorIdNotOrderByCreatedAsc(idUser, page);
        return itemRequests.stream()
                .map(MapperRequest::toItemRequestDto)
                .peek(itemRequestDto -> itemRequestDto.setItems(itemRepositoryJpa.findByRequestId(itemRequestDto.getId())))
                .collect(Collectors.toList());
    }

    /**
     * Получить запрос по его id
     */
    public ItemRequestDto getRequestById(Long idUser, Long requestId) {
        checkUser(idUser);
        ItemRequest itemRequest = repository.findById(requestId)
                .orElseThrow(() -> new MissingIdException("При запросе запроса произошла ошибка"));
        ItemRequestDto itemRequestDto = toItemRequestDto(itemRequest);
        itemRequestDto.setItems(itemRepositoryJpa.findByRequestId(requestId));
        return itemRequestDto;
    }

    /**
     * Проверка что пользователь существует
     */
    private void checkUser(Long idUser) {
        userService.getUserById(idUser);
    }

}
