package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository {

    Item createItem(Item item);

    Item getItemById(Long id);

    Item saveItem(Item item);

    List<Item> getAllUserItems(Long idUser);

    List<Item> findItems(String text);

}
