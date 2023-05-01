package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
@RequestMapping(path = "/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestHeader(ID_SHARER) Long idUser,
                                             @Valid @RequestBody ItemDto itemDto) {
        log.info("Create item {} from user id = {}", itemDto, idUser);
        return itemClient.createItem(idUser, itemDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateItem(@RequestHeader(ID_SHARER) Long idUser,
                                             @PathVariable Long id,
                                             @RequestBody ItemDto itemDto) {
        log.info("Update item id = {} by user id = {} with itemDto {} ", id, idUser, itemDto);
        return itemClient.updateItem(idUser, id, itemDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getItemById(@RequestHeader(ID_SHARER) Long idUser,
                                              @PathVariable Long id) {
        log.info("Get item by id = {} by user id = {}", id, idUser);
        return itemClient.getItemById(idUser, id);
    }

    @GetMapping
    public ResponseEntity<Object> getAllUserItems(@RequestHeader(ID_SHARER) Long idUser,
                                                  @RequestParam(required = false, defaultValue = "0") @Min(0) Integer from,
                                                  @RequestParam(required = false, defaultValue = "10") @Min(1) Integer size) {
        log.info("Get all items by user id {}, from {}, size {}", idUser, from, size);
        return itemClient.getAllUserItems(idUser, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> findItems(@RequestHeader(ID_SHARER) Long idUser,
                                            @RequestParam(name = "text") String text,
                                            @RequestParam(required = false, defaultValue = "0") @Min(0) Integer from,
                                            @RequestParam(required = false, defaultValue = "10") @Min(1) Integer size) {
        log.info("Find items by user id = {}, with text {}, from {}, size {}", idUser, text, from, size);
        return itemClient.findItems(idUser, text, from, size);
    }

    @PostMapping("/{id}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader(ID_SHARER) Long idUser,
                                             @PathVariable Long id,
                                             @Valid @RequestBody CommentDto comment) {
        log.info("Add comment {}, for item {} from user id {}", comment, id, idUser);
        return itemClient.addComment(idUser, id, comment);
    }

}