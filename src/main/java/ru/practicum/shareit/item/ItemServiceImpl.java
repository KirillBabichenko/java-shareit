package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.FailedOwnerException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static ru.practicum.shareit.item.dto.ItemMapper.toItem;
import static ru.practicum.shareit.item.dto.ItemMapper.toItemDto;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public ItemDto createItem(Long id, ItemDto itemDto) {
        Item item = toItem(itemDto, userRepository.getUserById(id));
        return toItemDto(itemRepository.createItem(item));
    }

    public ItemDto updateItem(Long idUser, Long id, ItemDto itemDto) {
        Item updateItem = itemRepository.getItemById(id);
        checkOwner(idUser, updateItem);
        ofNullable(itemDto.getName()).ifPresent(updateItem::setName);
        ofNullable(itemDto.getDescription()).ifPresent(updateItem::setDescription);
        ofNullable(itemDto.getAvailable()).ifPresent(updateItem::setAvailable);
        return toItemDto(itemRepository.saveItem(updateItem));
    }

    @Override
    public ItemDto getItemById(Long id) {
        return toItemDto(itemRepository.getItemById(id));
    }

    @Override
    public List<ItemDto> getAllUserItems(Long idUser) {
        return itemRepository.getAllUserItems(idUser).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> findItems(String text) {

        return itemRepository.findItems(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private void checkOwner(Long id, Item item) {
        if (!Objects.equals(item.getOwner().getId(), id)) {
            throw new FailedOwnerException("Пользователь с id = " + id + " не является владельцем вещи");
        }
    }
}
