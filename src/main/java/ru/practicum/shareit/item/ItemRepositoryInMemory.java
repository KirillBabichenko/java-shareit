package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.MissingIdException;
import ru.practicum.shareit.item.model.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class ItemRepositoryInMemory implements ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private Long idItem = 0L;

    @Override
    public Item createItem(Item item) {
        item.setId(++idItem);
        items.put(idItem, item);
        return items.get(idItem);
    }

    @Override
    public Item getItemById(Long id) {
        checkItem(id);
        return items.get(id);
    }

    @Override
    public Item saveItem(Item item) {
        return items.put(item.getId(), item);
    }

    @Override
    public List<Item> getAllUserItems(Long idUser) {
        return items.values().stream()
                .filter(item -> item.getOwner().getId().equals(idUser))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> findItems(String text) {
        return items.values().stream()
                .filter(item -> item.getName().toLowerCase().contains(text.toLowerCase()) ||
                        item.getDescription().toLowerCase().contains(text.toLowerCase()))
                .filter(Item::getAvailable)
                .collect(Collectors.toList());
    }

    private void checkItem(Long id) {
        if (!items.containsKey(id)) {
            throw new MissingIdException("Вещи с id = " + id + " не найдено.");
        }
    }
}
