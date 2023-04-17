package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.exception.MissingIdException;
import ru.practicum.shareit.exception.RequestFailedException;
import ru.practicum.shareit.exception.UnsupportedStatus;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.notNullValue;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class BookingServiceImplTest {
    private final ItemService itemService;
    private final UserService userService;
    private final BookingService bookingService;
    private UserDto testUser;
    private UserDto secondTestUser;
    private ItemDto itemDtoFromDB;
    private BookingShortDto bookingShortDto;
    private BookingShortDto secondBookingShortDto;

    @BeforeEach
    public void setUp() {
        ItemDto itemDto = ItemDto.builder()
                .name("Дрель")
                .description("Простая дрель")
                .available(true)
                .build();

        UserDto userDto = UserDto.builder()
                .email("test@test.com")
                .name("testName")
                .build();

        UserDto secondUserDto = UserDto.builder()
                .email("second@test.com")
                .name("secondName")
                .build();

        testUser = userService.createUser(userDto);
        secondTestUser = userService.createUser(secondUserDto);
        itemDtoFromDB = itemService.createItem(testUser.getId(), itemDto);

        bookingShortDto = BookingShortDto.builder()
                .start(LocalDateTime.now().plusNanos(1))
                .end(LocalDateTime.now().plusNanos(2))
                .itemId(itemDtoFromDB.getId())
                .build();
        secondBookingShortDto = BookingShortDto.builder()
                .start(LocalDateTime.now().plusHours(3))
                .end(LocalDateTime.now().plusHours(4))
                .itemId(itemDtoFromDB.getId())
                .build();
    }

    @Test
    void createBookingTest() {
        BookingDto bookingDtoFromDB = bookingService.createBooking(secondTestUser.getId(), bookingShortDto);

        assertThat(bookingDtoFromDB.getId(), notNullValue());
        checkBookingsAreTheSame(bookingDtoFromDB, bookingShortDto, secondTestUser, itemDtoFromDB, Status.WAITING);
    }

    @Test
    void approveBookingTest() {
        BookingDto bookingDtoFromDB = bookingService.createBooking(secondTestUser.getId(), bookingShortDto);
        BookingDto approveBooking = bookingService.approveBooking(testUser.getId(), bookingDtoFromDB.getId(), true);

        checkBookingsAreTheSame(approveBooking, bookingShortDto, secondTestUser, itemDtoFromDB, Status.APPROVED);
    }


    @Test
    void getBookingByIdTest() {
        BookingDto bookingDtoFromDB = bookingService.createBooking(secondTestUser.getId(), bookingShortDto);
        BookingDto approveBooking = bookingService.approveBooking(testUser.getId(), bookingDtoFromDB.getId(), true);
        BookingDto bookingById = bookingService.getBookingById(testUser.getId(), approveBooking.getId());

        checkBookingsAreTheSame(bookingById, bookingShortDto, secondTestUser, itemDtoFromDB, Status.APPROVED);

        final MissingIdException exception = Assertions.assertThrows(MissingIdException.class,
                () -> bookingService.getBookingById(999L, approveBooking.getId()));
        Assertions.assertEquals("Ошибка запроса. Запрашивать бронирование имеет право" +
                " только владелец вещи или создатель бронирования.", exception.getMessage());
    }

    @Test
    void getAllBookingsTest() {
        List<BookingShortDto> bookingDtos = List.of(bookingShortDto, secondBookingShortDto);
        BookingDto firstBooking = bookingService.createBooking(secondTestUser.getId(), bookingShortDto);
        bookingService.approveBooking(testUser.getId(), firstBooking.getId(), true);
        BookingDto secondBooking = bookingService.createBooking(secondTestUser.getId(), secondBookingShortDto);
        List<BookingDto> bookings = bookingService.getAllBookings(secondTestUser.getId(), "ALL", 0, 3);

        assertThat(bookings.size(), equalTo(bookingDtos.size()));
        for (BookingShortDto dto : bookingDtos) {
            assertThat(bookings, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("start", equalTo(dto.getStart())),
                    hasProperty("end", equalTo(dto.getEnd())))));
        }

        List<BookingDto> approvedBookings = bookingService.getAllBookings(secondTestUser.getId(), "WAITING", 0, 3);
        BookingDto waitingBooking = approvedBookings.get(0);

        assertThat(approvedBookings.size(), equalTo(1));
        assertThat(waitingBooking.getId(), equalTo(secondBooking.getId()));
        checkBookingsAreTheSame(waitingBooking, secondBookingShortDto, secondTestUser, itemDtoFromDB, Status.WAITING);
    }

    @Test
    void getAllOwnerBookingsTest() {
        List<BookingShortDto> bookingDtos = List.of(bookingShortDto, secondBookingShortDto);
        BookingDto firstBooking = bookingService.createBooking(secondTestUser.getId(), bookingShortDto);
        bookingService.approveBooking(testUser.getId(), firstBooking.getId(), true);
        BookingDto secondBooking = bookingService.createBooking(secondTestUser.getId(), secondBookingShortDto);

        List<BookingDto> bookings = bookingService.getAllOwnerBookings(testUser.getId(), "ALL", 0, 3);

        assertThat(bookings.size(), equalTo(bookingDtos.size()));
        for (BookingShortDto dto : bookingDtos) {
            assertThat(bookings, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("start", equalTo(dto.getStart())),
                    hasProperty("end", equalTo(dto.getEnd())))));
        }

        List<BookingDto> approvedBookings = bookingService.getAllOwnerBookings(testUser.getId(), "WAITING", 0, 3);
        BookingDto waitingBooking = approvedBookings.get(0);

        assertThat(approvedBookings.size(), equalTo(1));
        assertThat(waitingBooking.getId(), equalTo(secondBooking.getId()));
        checkBookingsAreTheSame(waitingBooking, secondBookingShortDto, secondTestUser, itemDtoFromDB, Status.WAITING);
    }

    @Test
    void approveBookingWrongOwnerTest() {
        BookingDto bookingDtoFromDB = bookingService.createBooking(secondTestUser.getId(), bookingShortDto);

        final MissingIdException exception = Assertions.assertThrows(MissingIdException.class,
                () -> bookingService.approveBooking(secondTestUser.getId(), bookingDtoFromDB.getId(), true));
        Assertions.assertEquals("ID пользователя не совпадает с ID владельца вещи", exception.getMessage());
    }

    @Test
    void approveBookingTwiceErrorTest() {
        BookingDto bookingDtoFromDB = bookingService.createBooking(secondTestUser.getId(), bookingShortDto);
        bookingService.approveBooking(testUser.getId(), bookingDtoFromDB.getId(), true);

        final RequestFailedException exception = Assertions.assertThrows(RequestFailedException.class,
                () -> bookingService.approveBooking(testUser.getId(), bookingDtoFromDB.getId(), true));
        Assertions.assertEquals("Статус бронирования уже был утвержден.", exception.getMessage());
    }

    @Test
    void getAllBookingsNonExistentStateTest() {
        String nonExistentState = "nonExistentState";
        bookingService.createBooking(secondTestUser.getId(), bookingShortDto);

        final UnsupportedStatus exception = Assertions.assertThrows(UnsupportedStatus.class,
                () -> bookingService.getAllBookings(secondTestUser.getId(), nonExistentState, 0, 3));
        Assertions.assertEquals("Unknown state: " + nonExistentState, exception.getMessage());
    }

    @Test
    void getAllBookingsRejectedStateTest() {
        BookingDto firstBooking = bookingService.createBooking(secondTestUser.getId(), bookingShortDto);
        bookingService.approveBooking(testUser.getId(), firstBooking.getId(), false);

        List<BookingDto> rejectedBookings = bookingService.getAllBookings(secondTestUser.getId(), "REJECTED", 0, 3);
        BookingDto rejectedBooking = rejectedBookings.get(0);

        assertThat(rejectedBookings.size(), equalTo(1));
        checkBookingsAreTheSame(rejectedBooking, bookingShortDto, secondTestUser, itemDtoFromDB, Status.REJECTED);
    }

    @Test
    void getAllBookingsCurrentStateTest() {
        BookingShortDto bookingDto = BookingShortDto.builder()
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .itemId(itemDtoFromDB.getId())
                .build();
        List<BookingShortDto> bookingDtos = List.of(bookingDto);
        BookingDto firstBooking = bookingService.createBooking(secondTestUser.getId(), bookingDto);
        bookingService.approveBooking(testUser.getId(), firstBooking.getId(), true);

        List<BookingDto> currentBookings = bookingService.getAllBookings(secondTestUser.getId(), "CURRENT", 0, 3);
        BookingDto currentBooking = currentBookings.get(0);

        assertThat(currentBookings.size(), equalTo(bookingDtos.size()));
        checkBookingsAreTheSame(currentBooking, bookingDto, secondTestUser, itemDtoFromDB, Status.APPROVED);
    }

    @Test
    void getAllBookingsFutureStateTest() {
        BookingShortDto bookingDto = BookingShortDto.builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .itemId(itemDtoFromDB.getId())
                .build();
        List<BookingShortDto> bookingDtos = List.of(bookingDto);
        BookingDto firstBooking = bookingService.createBooking(secondTestUser.getId(), bookingDto);

        List<BookingDto> futureBookings = bookingService.getAllBookings(secondTestUser.getId(), "FUTURE", 0, 3);
        BookingDto futureBooking = futureBookings.get(0);

        assertThat(futureBookings.size(), equalTo(bookingDtos.size()));
        assertThat(futureBooking.getId(), equalTo(firstBooking.getId()));
        checkBookingsAreTheSame(futureBooking, bookingDto, secondTestUser, itemDtoFromDB, Status.WAITING);
    }

    @Test
    void getAllBookingsPastStateTest() {
        BookingShortDto bookingDto = BookingShortDto.builder()
                .start(LocalDateTime.now().minusHours(2))
                .end(LocalDateTime.now().minusHours(1))
                .itemId(itemDtoFromDB.getId())
                .build();
        List<BookingShortDto> bookingDtos = List.of(bookingDto);
        BookingDto firstBooking = bookingService.createBooking(secondTestUser.getId(), bookingDto);
        bookingService.approveBooking(testUser.getId(), firstBooking.getId(), true);

        List<BookingDto> pastBookings = bookingService.getAllBookings(secondTestUser.getId(), "PAST", 0, 3);
        BookingDto pastBooking = pastBookings.get(0);

        assertThat(pastBookings.size(), equalTo(bookingDtos.size()));
        assertThat(pastBooking.getId(), equalTo(firstBooking.getId()));
        checkBookingsAreTheSame(pastBooking, bookingDto, secondTestUser, itemDtoFromDB, Status.APPROVED);
    }

    @Test
    void getAllOwnerBookingsCurrentStateTest() {
        BookingShortDto bookingDto = BookingShortDto.builder()
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .itemId(itemDtoFromDB.getId())
                .build();
        List<BookingShortDto> bookingDtos = List.of(bookingDto);
        BookingDto firstBooking = bookingService.createBooking(secondTestUser.getId(), bookingDto);
        bookingService.approveBooking(testUser.getId(), firstBooking.getId(), true);

        List<BookingDto> currentBookings = bookingService.getAllOwnerBookings(testUser.getId(), "CURRENT", 0, 3);
        BookingDto currentBooking = currentBookings.get(0);

        assertThat(currentBookings.size(), equalTo(bookingDtos.size()));
        assertThat(currentBooking.getId(), equalTo(firstBooking.getId()));
        checkBookingsAreTheSame(currentBooking, bookingDto, secondTestUser, itemDtoFromDB, Status.APPROVED);
    }

    @Test
    void getAllOwnerBookingsFutureStateTest() {
        BookingShortDto bookingDto = BookingShortDto.builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .itemId(itemDtoFromDB.getId())
                .build();
        List<BookingShortDto> bookingDtos = List.of(bookingDto);
        BookingDto firstBooking = bookingService.createBooking(secondTestUser.getId(), bookingDto);
        bookingService.approveBooking(testUser.getId(), firstBooking.getId(), true);

        List<BookingDto> futureBookings = bookingService.getAllOwnerBookings(testUser.getId(), "FUTURE", 0, 3);
        BookingDto futureBooking = futureBookings.get(0);

        assertThat(futureBookings.size(), equalTo(bookingDtos.size()));
        assertThat(futureBooking.getId(), equalTo(firstBooking.getId()));
        checkBookingsAreTheSame(futureBooking, bookingDto, secondTestUser, itemDtoFromDB, Status.APPROVED);
    }

    @Test
    void getAllOwnerBookingsPastStateTest() {
        BookingShortDto bookingDto = BookingShortDto.builder()
                .start(LocalDateTime.now().minusHours(2))
                .end(LocalDateTime.now().minusHours(1))
                .itemId(itemDtoFromDB.getId())
                .build();
        List<BookingShortDto> bookingDtos = List.of(bookingDto);
        BookingDto firstBooking = bookingService.createBooking(secondTestUser.getId(), bookingDto);
        bookingService.approveBooking(testUser.getId(), firstBooking.getId(), true);

        List<BookingDto> pastBookings = bookingService.getAllOwnerBookings(testUser.getId(), "PAST", 0, 3);
        BookingDto pastBooking = pastBookings.get(0);

        assertThat(pastBookings.size(), equalTo(bookingDtos.size()));
        assertThat(pastBooking.getId(), equalTo(firstBooking.getId()));
        checkBookingsAreTheSame(pastBooking, bookingDto, secondTestUser, itemDtoFromDB, Status.APPROVED);
    }

    @Test
    void getAllOwnerBookingsUserHasNothingTest() {
        final RequestFailedException exception = Assertions.assertThrows(RequestFailedException.class,
                () -> bookingService.getAllOwnerBookings(secondTestUser.getId(), "ALL", 0, 3));
        Assertions.assertEquals("У пользователя нет ни одной вещи!", exception.getMessage());
    }

    @Test
    void createBookingItemStartLaterThanFinishTest() {
        BookingShortDto bookingDto = BookingShortDto.builder()
                .start(LocalDateTime.now().plusHours(2))
                .end(LocalDateTime.now().plusHours(1))
                .itemId(itemDtoFromDB.getId())
                .build();

        final RequestFailedException exception = Assertions.assertThrows(RequestFailedException.class,
                () -> bookingService.createBooking(secondTestUser.getId(), bookingDto));
        Assertions.assertEquals("Ошибка со временем бронирования", exception.getMessage());
    }

    private void checkBookingsAreTheSame(
            BookingDto booking, BookingShortDto secondBooking, UserDto user, ItemDto item, Status status) {
        assertThat(booking.getId(), notNullValue());
        assertThat(booking.getStatus(), equalTo(status));
        assertThat(booking.getStart(), equalTo(secondBooking.getStart()));
        assertThat(booking.getEnd(), equalTo(secondBooking.getEnd()));
        assertThat(booking.getBooker().getId(), equalTo(user.getId()));
        assertThat(booking.getItem().getId(), equalTo(item.getId()));
        assertThat(booking.getItem().getName(), equalTo(item.getName()));
    }
}
