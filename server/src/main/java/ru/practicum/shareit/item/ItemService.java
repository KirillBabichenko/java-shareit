package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto createItem(Long id, ItemDto itemDto);

    ItemDto updateItem(Long idUser, Long id, ItemDto itemDto);

    ItemDto getItemById(Long idUser, Long id);

    List<ItemDto> getAllUserItems(Long idUser, Integer from, Integer size);

    List<ItemDto> findItems(String text, Integer from, Integer size);

    CommentDto addComment(Long idUser, Long idItem, CommentDto commentDto);

}
