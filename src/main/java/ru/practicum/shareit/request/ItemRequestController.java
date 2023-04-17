package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

import static ru.practicum.shareit.user.Variables.ID_SHARER;

@Validated
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private final RequestService requestService;

    @PostMapping
    public ItemRequestDto createRequest(@RequestHeader(ID_SHARER) Long idUser,
                                        @Valid @RequestBody ItemRequestDto itemRequestDto) {
        return requestService.createRequest(idUser, itemRequestDto);
    }

    @GetMapping
    public List<ItemRequestDto> getAllRequestsById(@RequestHeader(ID_SHARER) Long idUser) {
        return requestService.getAllRequestsById(idUser);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getRequestById(@RequestHeader(ID_SHARER) Long idUser,
                                               @RequestParam(required = false, defaultValue = "0") @Min(0) Integer from,
                                               @RequestParam(required = false, defaultValue = "10") @Min(1) Integer size) {
        return requestService.getAllRequests(idUser, from, size);
    }

    @GetMapping("{requestId}")
    public ItemRequestDto getRequestById(@RequestHeader(ID_SHARER) Long idUser,
                                         @PathVariable Long requestId) {
        return requestService.getRequestById(idUser, requestId);
    }

}