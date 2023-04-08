package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    public final BookingRepository bookingRepository;
    public final UserRepositoryJpa userRepositoryJpa;
    public final ItemRepositoryJpa itemRepositoryJpa;

    //Создание бронирования
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

    //Подтверждение бронирования
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

    // Получить бронирование по его ID
    public BookingDto getBookingById(Long idUser, Long idBooking) {
        Booking booking = bookingRepository.findById(idBooking)
                .orElseThrow(() -> new MissingIdException("При запросе вещи произошла ошибка"));
        if (booking.getItem().getOwner().equals(idUser) || booking.getBooker().getId().equals(idUser)) {
            return toBookingDto(booking);
        } else throw new MissingIdException("Ошибка запроса. " +
                "Запрашивать бронирование имеет право только владелец вещи или создатель бронирования.");
    }

    // Получить все бронирования для текущего пользователя
    public List<BookingDto> getAllBookings(Long idUser, String text) {
        State state = getState(text);
        LocalDateTime dateTime = LocalDateTime.now();
        List<Booking> booking;
        User user = checkUser(idUser);
        switch (state) {
            case ALL:
                booking = bookingRepository.getAllBookingsById(idUser);
                break;
            case CURRENT:
                booking = bookingRepository.findDByBookerAndStartBeforeAndEndAfterOrderByStartDesc(user, dateTime, dateTime);
                break;
            case FUTURE:
                booking = bookingRepository.findDByBookerAndStartAfterOrderByStartDesc(user, dateTime);
                break;
            case PAST:
                booking = bookingRepository.findDByBookerAndEndBeforeOrderByStartDesc(user, dateTime);
                break;
            case WAITING:
                booking = bookingRepository.findDByBookerAndStatusOrderByStartDesc(user, Status.WAITING);
                break;
            case REJECTED:
                booking = bookingRepository.findDByBookerAndStatusOrderByStartDesc(user, Status.REJECTED);
                break;
            default:
                throw new RequestFailedException("Статус указан некорректно");
        }
        return booking.stream()
                .map(MapperBooking::toBookingDto)
                .collect(Collectors.toList());
    }

    // Получить все бронирования для вещей пользователя
    public List<BookingDto> getAllOwnerBookings(Long idUser, String text) {
        State state = getState(text);
        LocalDateTime dateTime = LocalDateTime.now();
        List<Booking> booking;
        checkUser(idUser);
        List<Item> items = itemRepositoryJpa.findByOwner(idUser);
        if (items.size() > 0) {
            switch (state) {
                case ALL:
                    booking = bookingRepository.findDByItemInOrderByStartDesc(items);
                    break;
                case CURRENT:
                    booking = bookingRepository.findDByItemInAndStartBeforeAndEndAfterOrderByStartDesc(items, dateTime, dateTime);
                    break;
                case FUTURE:
                    booking = bookingRepository.findDByItemInAndStartAfterOrderByStartDesc(items, dateTime);
                    break;
                case PAST:
                    booking = bookingRepository.findDByItemInAndEndBeforeOrderByStartDesc(items, dateTime);
                    break;
                case WAITING:
                    booking = bookingRepository.findDByItemInAndStatusOrderByStartDesc(items, Status.WAITING);
                    break;
                case REJECTED:
                    booking = bookingRepository.findDByItemInAndStatusOrderByStartDesc(items, Status.REJECTED);
                    break;
                default:
                    throw new RequestFailedException("Статус указан некорректно");
            }
            return booking.stream()
                    .map(MapperBooking::toBookingDto)
                    .collect(Collectors.toList());
        } else throw new RequestFailedException("У пользователя нет ни одной вещи!");

    }


    //Проверка доступности вещи для бронирования
    private void checkAvailableItem(Item item) {
        if (!item.getAvailable()) {
            throw new RequestFailedException("Данная вещь уже занята");
        }
    }

    // Проверка дат бронирования.
    // Старт не может быть позднее или одновременно с окончанием бронирования.
    private void checkBookingDate(BookingShortDto bookingDto) {
        if (bookingDto.getStart().isAfter(bookingDto.getEnd()) ||
                bookingDto.getStart().isEqual(bookingDto.getEnd())) {
            throw new RequestFailedException("Ошибка со временем бронирования");
        }
    }

    // Запрос пользователя из БД и заодно проверка, что пользователь существует.
    private User checkUser(Long idUser) {
        return userRepositoryJpa.findById(idUser)
                .orElseThrow(() -> new MissingIdException("При запросе вещи произошла ошибка"));
    }

    // Провера передаваемого статуса для запроса бронирований
    private State getState(String text) {
        for (State state : State.values()) {
            if (state.toString().equals(text)) {
                return state;
            }
        }
        throw new UnsupportedStatus("Unknown state: " + text);
    }

    // Проверка, что пользователь является владельцем вещи.
    // Мне кажется не логично и даже не соответствует ТЗ("Запрос может быть создан любым пользователем"),
    // но в тестах владелец не может забронировать свою же вещь.
    private void checkOwner(Long idUser, Item item) {
        if (item.getOwner().equals(idUser)) {
            throw new MissingIdException("Пользователь не может бронировать свою же вещь");
        }
    }
}
