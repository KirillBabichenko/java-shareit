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
        assertThat(bookingDtoFromDB.getStart(), equalTo(bookingShortDto.getStart()));
        assertThat(bookingDtoFromDB.getEnd(), equalTo(bookingShortDto.getEnd()));
        assertThat(bookingDtoFromDB.getStatus(), equalTo(Status.WAITING));
        assertThat(bookingDtoFromDB.getBooker().getId(), equalTo(secondTestUser.getId()));
        assertThat(bookingDtoFromDB.getItem().getId(), equalTo(itemDtoFromDB.getId()));
        assertThat(bookingDtoFromDB.getItem().getName(), equalTo(itemDtoFromDB.getName()));
    }

    @Test
    void approveBookingTest() {
        BookingDto bookingDtoFromDB = bookingService.createBooking(secondTestUser.getId(), bookingShortDto);
        BookingDto approveBooking = bookingService.approveBooking(testUser.getId(), bookingDtoFromDB.getId(), true);

        assertThat(approveBooking.getId(), notNullValue());
        assertThat(approveBooking.getStart(), equalTo(bookingShortDto.getStart()));
        assertThat(approveBooking.getEnd(), equalTo(bookingShortDto.getEnd()));
        assertThat(approveBooking.getStatus(), equalTo(Status.APPROVED));
        assertThat(approveBooking.getBooker().getId(), equalTo(secondTestUser.getId()));
        assertThat(approveBooking.getItem().getId(), equalTo(itemDtoFromDB.getId()));
        assertThat(approveBooking.getItem().getName(), equalTo(itemDtoFromDB.getName()));
    }


    @Test
    void getBookingByIdTest() {
        BookingDto bookingDtoFromDB = bookingService.createBooking(secondTestUser.getId(), bookingShortDto);
        BookingDto approveBooking = bookingService.approveBooking(testUser.getId(), bookingDtoFromDB.getId(), true);
        BookingDto bookingById = bookingService.getBookingById(testUser.getId(), approveBooking.getId());

        assertThat(bookingById.getId(), notNullValue());
        assertThat(bookingById.getStart(), equalTo(bookingShortDto.getStart()));
        assertThat(bookingById.getEnd(), equalTo(bookingShortDto.getEnd()));
        assertThat(bookingById.getStatus(), equalTo(Status.APPROVED));
        assertThat(bookingById.getBooker().getId(), equalTo(secondTestUser.getId()));
        assertThat(bookingById.getItem().getId(), equalTo(itemDtoFromDB.getId()));
        assertThat(bookingById.getItem().getName(), equalTo(itemDtoFromDB.getName()));

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
        assertThat(waitingBooking.getStatus(), equalTo(Status.WAITING));
        assertThat(waitingBooking.getBooker().getId(), equalTo(secondTestUser.getId()));
        assertThat(waitingBooking.getItem().getId(), equalTo(itemDtoFromDB.getId()));
        assertThat(waitingBooking.getItem().getName(), equalTo(itemDtoFromDB.getName()));
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
        assertThat(waitingBooking.getStatus(), equalTo(Status.WAITING));
        assertThat(waitingBooking.getBooker().getId(), equalTo(secondTestUser.getId()));
        assertThat(waitingBooking.getItem().getId(), equalTo(itemDtoFromDB.getId()));
        assertThat(waitingBooking.getItem().getName(), equalTo(itemDtoFromDB.getName()));
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
}
