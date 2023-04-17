package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.exception.FailedOwnerException;
import ru.practicum.shareit.exception.MissingIdException;
import ru.practicum.shareit.exception.RequestFailedException;
import ru.practicum.shareit.item.dto.CommentDto;
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
public class ItemServiceImplTest {
    private final ItemService itemService;
    private final UserService userService;
    private final BookingService bookingService;
    private ItemDto itemDto;
    private UserDto secondUserDto;
    private ItemDto updateItemDto;
    private UserDto testUser;
    private UserDto secondUserFromDB;


    @BeforeEach
    public void setUp() {
        itemDto = ItemDto.builder()
                .name("Дрель")
                .description("Простая дрель")
                .available(true)
                .build();

        updateItemDto = ItemDto.builder()
                .name("Дрель+")
                .description("Аккумуляторная дрель")
                .available(true)
                .build();

        UserDto userDto = UserDto.builder()
                .email("test@test.com")
                .name("testName")
                .build();

        secondUserDto = UserDto.builder()
                .email("second@test.com")
                .name("secondName")
                .build();

        testUser = userService.createUser(userDto);
        secondUserFromDB = userService.createUser(secondUserDto);
    }

    @Test
    void createItemTest() {
        ItemDto itemDtoFromDB = itemService.createItem(testUser.getId(), itemDto);

        assertThat(itemDtoFromDB.getId(), notNullValue());
        assertThat(itemDtoFromDB.getName(), equalTo(itemDto.getName()));
        assertThat(itemDtoFromDB.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(itemDtoFromDB.getAvailable(), equalTo(itemDto.getAvailable()));
    }

    @Test
    void updateItemTest() {
        ItemDto itemDtoFromDB = itemService.createItem(testUser.getId(), itemDto);

        ItemDto updateItemFromDB = itemService.updateItem(testUser.getId(), itemDtoFromDB.getId(), updateItemDto);

        assertThat(updateItemFromDB.getId(), notNullValue());
        assertThat(updateItemFromDB.getName(), equalTo(updateItemDto.getName()));
        assertThat(updateItemFromDB.getDescription(), equalTo(updateItemDto.getDescription()));
        assertThat(updateItemFromDB.getAvailable(), equalTo(updateItemDto.getAvailable()));
    }

    @Test
    void getItemByIdTest() {
        ItemDto itemDtoFromDB = itemService.createItem(testUser.getId(), itemDto);

        ItemDto itemByIdFromDB = itemService.getItemById(testUser.getId(), itemDtoFromDB.getId());

        assertThat(itemByIdFromDB.getId(), notNullValue());
        assertThat(itemByIdFromDB.getName(), equalTo(itemDto.getName()));
        assertThat(itemByIdFromDB.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(itemByIdFromDB.getAvailable(), equalTo(itemDto.getAvailable()));
    }


    @Test
    void getAllUserItemsTest() {
        List<ItemDto> testList = List.of(itemDto, updateItemDto);
        for (ItemDto dto : testList) {
            itemService.createItem(testUser.getId(), dto);
        }

        List<ItemDto> itemsFromDB = itemService.getAllUserItems(testUser.getId(), 0, 3);

        assertThat(itemsFromDB.size(), equalTo(testList.size()));
        for (ItemDto dto : testList) {
            assertThat(itemsFromDB, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("name", equalTo(dto.getName())),
                    hasProperty("description", equalTo(dto.getDescription())),
                    hasProperty("available", equalTo(dto.getAvailable())))));
        }
    }

    @Test
    void findItemsTest() {
        String textSearch = "аккУМУляторная";
        List<ItemDto> testList = List.of(itemDto, updateItemDto);
        for (ItemDto dto : testList) {
            itemService.createItem(testUser.getId(), dto);
        }

        List<ItemDto> itemsFromSearch = itemService.findItems(textSearch, 0, 3);

        assertThat(itemsFromSearch.size(), equalTo(1));
        assertThat(itemsFromSearch.get(0).getId(), notNullValue());
        assertThat(itemsFromSearch.get(0).getName(), equalTo(updateItemDto.getName()));
        assertThat(itemsFromSearch.get(0).getDescription(), equalTo(updateItemDto.getDescription()));
        assertThat(itemsFromSearch.get(0).getAvailable(), equalTo(updateItemDto.getAvailable()));
    }

    @Test
    void addCommentTest() {
        CommentDto comment = CommentDto.builder()
                .text("Добавляем комментарий")
                .build();
        ItemDto itemDtoFromDB = itemService.createItem(testUser.getId(), itemDto);
        BookingShortDto bookingShortDto = BookingShortDto.builder()
                .start(LocalDateTime.now().plusNanos(1))
                .end(LocalDateTime.now().plusNanos(2))
                .itemId(itemDtoFromDB.getId())
                .build();

        BookingDto bookingDtoFromDB = bookingService.createBooking(secondUserFromDB.getId(), bookingShortDto);
        bookingService.approveBooking(testUser.getId(), bookingDtoFromDB.getId(), true);
        CommentDto commentFromDb = itemService.addComment(secondUserFromDB.getId(), itemDtoFromDB.getId(), comment);
        ItemDto itemWithComment = itemService.getItemById(testUser.getId(), itemDtoFromDB.getId());

        assertThat(commentFromDb.getId(), notNullValue());
        assertThat(commentFromDb.getText(), equalTo(comment.getText()));
        assertThat(commentFromDb.getAuthorName(), equalTo(secondUserDto.getName()));
        assertThat(commentFromDb.getCreated(), notNullValue());
        assertThat(itemWithComment.getComments().size(), equalTo(1));
        assertThat(itemWithComment.getComments().get(0).getText(), equalTo(comment.getText()));
        assertThat(itemWithComment.getComments().get(0).getAuthorName(), equalTo(secondUserDto.getName()));
        final MissingIdException exception = Assertions.assertThrows(MissingIdException.class,
                () -> bookingService.createBooking(testUser.getId(), bookingShortDto));
        Assertions.assertEquals("Пользователь не может бронировать свою же вещь", exception.getMessage());
    }

    @Test
    void addCommentExceptionTest() {
        CommentDto comment = CommentDto.builder()
                .text("Добавляем комментарий")
                .build();
        ItemDto itemDtoFromDB = itemService.createItem(testUser.getId(), itemDto);
        BookingShortDto bookingShortDto = BookingShortDto.builder()
                .start(LocalDateTime.now().plusNanos(1))
                .end(LocalDateTime.now().plusNanos(2))
                .itemId(itemDtoFromDB.getId())
                .build();
        bookingService.createBooking(secondUserFromDB.getId(), bookingShortDto);

        final RequestFailedException exception = Assertions.assertThrows(RequestFailedException.class,
                () -> itemService.addComment(secondUserFromDB.getId(), itemDtoFromDB.getId(), comment));
        Assertions.assertEquals("Не выполнены условия для добавления комментария", exception.getMessage());
    }

    @Test
    void updateItemWrongOwnerTest() {
        ItemDto itemDtoFromDB = itemService.createItem(testUser.getId(), itemDto);

        final FailedOwnerException exception = Assertions.assertThrows(FailedOwnerException.class,
                () -> itemService.updateItem(secondUserFromDB.getId(), itemDtoFromDB.getId(), updateItemDto));
        Assertions.assertEquals("Пользователь с id = " + secondUserFromDB.getId() +
                " не является владельцем вещи", exception.getMessage());
    }

}
