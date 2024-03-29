package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.dto.MapperBooking;
import ru.practicum.shareit.exception.MissingIdException;
import ru.practicum.shareit.exception.RequestFailedException;
import ru.practicum.shareit.exception.UnsupportedStatus;
import ru.practicum.shareit.item.ItemRepositoryJpa;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepositoryJpa;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.dto.MapperBooking.toBooking;
import static ru.practicum.shareit.booking.dto.MapperBooking.toBookingDto;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    public final BookingRepository bookingRepository;
    public final UserRepositoryJpa userRepositoryJpa;
    public final ItemRepositoryJpa itemRepositoryJpa;

    /**
     * Создание бронирования
     */
    @Transactional
    public BookingDto createBooking(Long idUser, BookingShortDto bookingDto) {
        checkBookingDate(bookingDto);
        User user = checkUser(idUser);
        Item item = itemRepositoryJpa.findById(bookingDto.getItemId())
                .orElseThrow(() -> new MissingIdException("При запросе вещи произошла ошибка"));
        checkOwner(idUser, item);
        checkAvailableItem(item);
        Booking booking = toBooking(bookingDto, user, item, Status.WAITING);
        return toBookingDto(bookingRepository.save(booking));
    }

    /**
     * Подтверждение бронирования
     */
    @Transactional
    public BookingDto approveBooking(Long idUser, Long idBooking, Boolean approved) {
        Booking booking = bookingRepository.findById(idBooking)
                .orElseThrow(() -> new MissingIdException("При запросе вещи произошла ошибка"));
        if (booking.getStatus().equals(Status.WAITING)) {
            if (booking.getItem().getOwner().equals(idUser)) {
                if (approved) {
                    booking.setStatus(Status.APPROVED);
                } else {
                    booking.setStatus(Status.REJECTED);
                }
            } else {
                throw new MissingIdException("ID пользователя не совпадает с ID владельца вещи");
            }
        } else {
            throw new RequestFailedException("Статус бронирования уже был утвержден.");
        }
        return toBookingDto(bookingRepository.save(booking));
    }

    /**
     * Получить бронирование по его ID
     */
    public BookingDto getBookingById(Long idUser, Long idBooking) {
        Booking booking = bookingRepository.findById(idBooking)
                .orElseThrow(() -> new MissingIdException("При запросе вещи произошла ошибка"));
        if (booking.getItem().getOwner().equals(idUser) || booking.getBooker().getId().equals(idUser)) {
            return toBookingDto(booking);
        } else throw new MissingIdException("Ошибка запроса. " +
                "Запрашивать бронирование имеет право только владелец вещи или создатель бронирования.");
    }

    /**
     * Получить все бронирования для текущего пользователя
     */
    public List<BookingDto> getAllBookings(Long idUser, String text, Integer from, Integer size) {
        State state = State.getStateFromText(text);
        LocalDateTime dateTime = LocalDateTime.now();
        List<Booking> booking;
        User user = checkUser(idUser);
        int start = from / size;
        PageRequest page = PageRequest.of(start, size);
        switch (state) {
            case ALL:
                booking = bookingRepository.getAllBookingsById(idUser, page);
                break;
            case CURRENT:
                booking = bookingRepository.findDByBookerAndStartBeforeAndEndAfterOrderByStartDesc(user, dateTime, dateTime, page);
                break;
            case FUTURE:
                booking = bookingRepository.findDByBookerAndStartAfterOrderByStartDesc(user, dateTime, page);
                break;
            case PAST:
                booking = bookingRepository.findDByBookerAndEndBeforeOrderByStartDesc(user, dateTime, page);
                break;
            case WAITING:
                booking = bookingRepository.findDByBookerAndStatusOrderByStartDesc(user, Status.WAITING, page);
                break;
            case REJECTED:
                booking = bookingRepository.findDByBookerAndStatusOrderByStartDesc(user, Status.REJECTED, page);
                break;
            default:
                throw new RequestFailedException("Статус указан некорректно");
        }
        return booking.stream()
                .map(MapperBooking::toBookingDto)
                .collect(Collectors.toList());
    }

    /**
     * Получить все бронирования для вещей пользователя
     */
    public List<BookingDto> getAllOwnerBookings(Long idUser, String text, Integer start, Integer size) {
        State state = State.getStateFromText(text);
        LocalDateTime dateTime = LocalDateTime.now();
        List<Booking> booking;
        checkUser(idUser);
        int from = start / size;
        PageRequest page = PageRequest.of(from, size);
        List<Item> items = itemRepositoryJpa.findByOwner(idUser);
        if (items.size() > 0) {
            switch (state) {
                case ALL:
                    booking = bookingRepository.findDByItemInOrderByStartDesc(items, page);
                    break;
                case CURRENT:
                    booking = bookingRepository.findDByItemInAndStartBeforeAndEndAfterOrderByStartDesc(items, dateTime, dateTime, page);
                    break;
                case FUTURE:
                    booking = bookingRepository.findDByItemInAndStartAfterOrderByStartDesc(items, dateTime, page);
                    break;
                case PAST:
                    booking = bookingRepository.findDByItemInAndEndBeforeOrderByStartDesc(items, dateTime, page);
                    break;
                case WAITING:
                    booking = bookingRepository.findDByItemInAndStatusOrderByStartDesc(items, Status.WAITING, page);
                    break;
                case REJECTED:
                    booking = bookingRepository.findDByItemInAndStatusOrderByStartDesc(items, Status.REJECTED, page);
                    break;
                default:
                    throw new UnsupportedStatus("Unknown state: " + state);
            }
            return booking.stream()
                    .map(MapperBooking::toBookingDto)
                    .collect(Collectors.toList());
        } else throw new RequestFailedException("У пользователя нет ни одной вещи!");
    }


    /**
     * Проверка доступности вещи для бронирования
     */
    private void checkAvailableItem(Item item) {
        if (!item.getAvailable()) {
            throw new RequestFailedException("Данная вещь уже занята");
        }
    }

    /**
     * Проверка дат бронирования.
     * Старт не может быть позднее или одновременно с окончанием бронирования.
     */
    private void checkBookingDate(BookingShortDto bookingDto) {
        if (bookingDto.getStart().isAfter(bookingDto.getEnd()) ||
                bookingDto.getStart().isEqual(bookingDto.getEnd())) {
            throw new RequestFailedException("Ошибка со временем бронирования");
        }
    }

    /**
     * Запрос пользователя из БД и заодно проверка, что пользователь существует.
     */
    private User checkUser(Long idUser) {
        return userRepositoryJpa.findById(idUser)
                .orElseThrow(() -> new MissingIdException("При запросе вещи произошла ошибка"));
    }

    /**
     * Проверка, что пользователь является владельцем вещи.
     */
    private void checkOwner(Long idUser, Item item) {
        if (item.getOwner().equals(idUser)) {
            throw new MissingIdException("Пользователь не может бронировать свою же вещь");
        }
    }
}
