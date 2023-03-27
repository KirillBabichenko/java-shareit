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
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemDto createItem(@RequestHeader("X-Sharer-User-Id") Long idUser,
                              @Valid @RequestBody ItemDto itemDto) {
        return itemService.createItem(idUser, itemDto);
    }

    @PatchMapping("/{id}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") Long idUser,
                              @PathVariable Long id,
                              @RequestBody ItemDto itemDto) {
        return itemService.updateItem(idUser, id, itemDto);
    }

    @GetMapping("/{id}")
    public ItemDto getItemById(@PathVariable Long id) {
        return itemService.getItemById(id);
    }

    @GetMapping
    public List<ItemDto> getAllUserItems(@RequestHeader("X-Sharer-User-Id") Long idUser) {
        return itemService.getAllUserItems(idUser);
    }

    @GetMapping("/search")
    public List<ItemDto> findItems(@RequestParam(name = "text") String text) {
        if (text.isEmpty()) {
            return new ArrayList<>();
        }
        return itemService.findItems(text);
    }
}
