package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.exception.FailedOwnerException;
import ru.practicum.shareit.exception.MissingIdException;
import ru.practicum.shareit.exception.RequestFailedException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static ru.practicum.shareit.booking.dto.MapperBooking.toBookingInfoDto;
import static ru.practicum.shareit.item.dto.CommentMapper.toComment;
import static ru.practicum.shareit.item.dto.CommentMapper.toCommentDto;
import static ru.practicum.shareit.item.dto.ItemMapper.toItem;
import static ru.practicum.shareit.item.dto.ItemMapper.toItemDto;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final UserService userService;
    private final ItemRepositoryJpa itemRepositoryJpa;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    /**
     * Добавить вещь
     */
    @Transactional
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        Item newItem = toItem(itemDto, UserMapper.toUser(userService.getUserById(userId)));
        return toItemDto(itemRepositoryJpa.save(newItem));
    }

    /**
     * Изменить созданную вещь
     */
    @Transactional
    public ItemDto updateItem(Long idUser, Long id, ItemDto itemDto) {
        Item updateItem = itemRepositoryJpa.findById(id)
                .orElseThrow(() -> new MissingIdException("При запросе Item произошла ошибка"));
        checkOwner(idUser, updateItem);
        ofNullable(itemDto.getName()).ifPresent(updateItem::setName);
        ofNullable(itemDto.getDescription()).ifPresent(updateItem::setDescription);
        ofNullable(itemDto.getAvailable()).ifPresent(updateItem::setAvailable);
        return toItemDto(itemRepositoryJpa.save(updateItem));
    }

    /**
     * Получить вещь по id
     */
    @Override
    public ItemDto getItemById(Long idUser, Long id) {
        Item item = itemRepositoryJpa.findById(id)
                .orElseThrow(() -> new MissingIdException("При запросе Item произошла ошибка"));
        return setBookingAndCommentInfo(item, idUser);
    }

    /**
     * Получить все вещи пользователя
     */
    @Override
    public List<ItemDto> getAllUserItems(Long idUser) {
        return itemRepositoryJpa.findByOwner(idUser).stream()
                .map(item -> setBookingAndCommentInfo(item, idUser))
                .sorted(Comparator.comparing(ItemDto::getId))
                .collect(Collectors.toList());
    }

    /**
     * Поиск вещи по названию или описанию
     */
    @Override
    public List<ItemDto> findItems(String text) {
        if (text.isEmpty()) {
            return new ArrayList<>();
        } else {
            return itemRepositoryJpa.search(text).stream()
                    .map(ItemMapper::toItemDto)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Добавить комментарий для вещи
     */
    @Transactional
    public CommentDto addComment(Long idUser, Long idItem, CommentDto commentDto) {
        Comment comment = toComment(commentDto);
        Item item = itemRepositoryJpa.findById(idItem)
                .orElseThrow(() -> new MissingIdException("При запросе Item произошла ошибка"));
        LocalDateTime dateTime = LocalDateTime.now();
        List<Booking> bookings = bookingRepository.findBookingsByItem(item, Status.APPROVED, idUser, dateTime);
        if (!bookings.isEmpty()) {
            comment.setItem(item);
            comment.setAuthor(bookings.get(0).getBooker());
            comment.setCreated(dateTime);
            return toCommentDto(commentRepository.save(comment));
        } else {
            throw new RequestFailedException("Не выполнены условия для добавления комментария");
        }
    }

    /**
     * Проверяем является ли айди владельцем вещи
     */
    private void checkOwner(Long id, Item item) {
        if (!Objects.equals(item.getOwner(), id)) {
            throw new FailedOwnerException("Пользователь с id = " + id + " не является владельцем вещи");
        }
    }

    /**
     * Добавляем в ItemDto информацию о резервировании и комментариях
     */
    private ItemDto setBookingAndCommentInfo(Item item, Long idUser) {
        LocalDateTime dateTime = LocalDateTime.now();
        ItemDto itemDto = toItemDto(item);
        List<CommentDto> comments = commentRepository.findByItem(item).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
        itemDto.setComments(comments);
        if (item.getOwner().equals(idUser)) {
            List<Booking> lastBooking = bookingRepository
                    .findDByItemAndStatusAndStartBeforeOrderByEndDesc(item, Status.APPROVED, dateTime);
            itemDto.setLastBooking(lastBooking.isEmpty() ? null : toBookingInfoDto(lastBooking.get(0)));
            List<Booking> nextBooking = bookingRepository
                    .findDByItemAndStatusAndStartAfterOrderByStartAsc(item, Status.APPROVED, dateTime);
            itemDto.setNextBooking(nextBooking.isEmpty() ? null : toBookingInfoDto(nextBooking.get(0)));
        }
        return itemDto;
    }

}
