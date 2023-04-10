package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import java.util.List;

import static ru.practicum.shareit.user.Variables.ID_SHARER;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemDto createItem(@RequestHeader(ID_SHARER) Long idUser,
                              @Valid @RequestBody ItemDto itemDto) {
        return itemService.createItem(idUser, itemDto);
    }

    @PatchMapping("/{id}")
    public ItemDto updateItem(@RequestHeader(ID_SHARER) Long idUser,
                              @PathVariable Long id,
                              @RequestBody ItemDto itemDto) {
        return itemService.updateItem(idUser, id, itemDto);
    }

    @GetMapping("/{id}")
    public ItemDto getItemById(@RequestHeader(ID_SHARER) Long idUser,
                               @PathVariable Long id) {
        return itemService.getItemById(idUser, id);
    }

    @GetMapping
    public List<ItemDto> getAllUserItems(@RequestHeader(ID_SHARER) Long idUser) {
        return itemService.getAllUserItems(idUser);
    }

    @GetMapping("/search")
    public List<ItemDto> findItems(@RequestParam(name = "text") String text) {
        return itemService.findItems(text);
    }

    @PostMapping("/{id}/comment")
    public CommentDto addComment(@RequestHeader(ID_SHARER) Long idUser,
                                 @PathVariable Long id,
                                 @Valid @RequestBody CommentDto comment) {
        return itemService.addComment(idUser, id, comment);
    }
}
