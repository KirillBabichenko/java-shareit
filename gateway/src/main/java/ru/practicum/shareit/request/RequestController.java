package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import static ru.practicum.shareit.client.Variables.ID_SHARER;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class RequestController {
    private final RequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> createRequest(@RequestHeader(ID_SHARER) Long idUser,
                                                @Valid @RequestBody ItemRequestDto itemRequestDto) {
        log.info("Create request {}, from user id {}", itemRequestDto, idUser);
        return requestClient.createRequest(idUser, itemRequestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getAllRequestsById(@RequestHeader(ID_SHARER) Long idUser) {
        log.info("Get all requests from user id {}", idUser);
        return requestClient.getAllRequestsById(idUser);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(@RequestHeader(ID_SHARER) Long idUser,
                                                 @RequestParam(required = false, defaultValue = "0") @Min(0) Integer from,
                                                 @RequestParam(required = false, defaultValue = "10") @Min(1) Integer size) {
        log.info("Get all requests from user id {}, from {}, size {}", idUser, from, size);
        return requestClient.getAllRequests(idUser, from, size);
    }

    @GetMapping("{requestId}")
    public ResponseEntity<Object> getRequestById(@RequestHeader(ID_SHARER) Long idUser,
                                                 @PathVariable Long requestId) {
        log.info("Get request by id {}, from user id {}", requestId, idUser);
        return requestClient.getRequestById(idUser, requestId);
    }
}
