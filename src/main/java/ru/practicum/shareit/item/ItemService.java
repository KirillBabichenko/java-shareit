package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto createItem(Long id, ItemDto itemDto);

    ItemDto updateItem(Long idUser, Long id, ItemDto itemDto);

    ItemDto getItemById(Long id);

    List<ItemDto> getAllUserItems(Long idUser);

    List<ItemDto> findItems(String text);

}
