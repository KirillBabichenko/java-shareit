package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.MissingIdException;
import ru.practicum.shareit.user.dto.UserDto;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.validation.ConstraintViolationException;
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
public class UserServiceImplTest {
    private final UserService userService;
    private final EntityManager em;
    private UserDto userDto;
    private UserDto secondUserDto;

    @BeforeEach
    public void setUp() {
        userDto = UserDto.builder()
                .email("test@test.com")
                .name("testName")
                .build();

        secondUserDto = UserDto.builder()
                .email("second@test.com")
                .name("secondName")
                .build();
    }

    @Test
    void createUserTest() {
        userService.createUser(userDto);

        TypedQuery<User> query = em.createQuery("Select u from User u where u.email = :email", User.class);
        User testUser = query.setParameter("email", userDto.getEmail())
                .getSingleResult();

        assertThat(testUser.getId(), equalTo(1L));
        assertThat(testUser.getName(), equalTo(userDto.getName()));
        assertThat(testUser.getEmail(), equalTo(userDto.getEmail()));
    }


    @Test
    void updateUserTest() {
        userService.createUser(userDto);
        UserDto updateUser = UserDto.builder()
                .email("update@test.com")
                .name("updateName")
                .build();
        userService.updateUser(1L, updateUser);

        TypedQuery<User> query = em.createQuery("Select u from User u where u.email = :email", User.class);
        User testUser = query.setParameter("email", updateUser.getEmail())
                .getSingleResult();

        assertThat(testUser.getId(), equalTo(1L));
        assertThat(testUser.getName(), equalTo(updateUser.getName()));
        assertThat(testUser.getEmail(), equalTo(updateUser.getEmail()));
    }

    @Test
    void getUserByIdTest() {
        UserDto testUser = userService.createUser(userDto);
        UserDto userFromDB = userService.getUserById(testUser.getId());

        assertThat(userFromDB.getId(), equalTo(1L));
        assertThat(userFromDB.getName(), equalTo(userDto.getName()));
        assertThat(userFromDB.getEmail(), equalTo(userDto.getEmail()));
    }

    @Test
    void getAllUsersTest() {
        List<UserDto> testList = List.of(userDto, secondUserDto);
        for (UserDto dto : testList) {
            userService.createUser(dto);
        }

        List<UserDto> dtoFromDB = userService.getAllUsers();

        assertThat(dtoFromDB.size(), equalTo(testList.size()));
        for (UserDto user : testList) {
            assertThat(dtoFromDB, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("name", equalTo(user.getName())),
                    hasProperty("email", equalTo(user.getEmail())))));
        }
    }

    @Test
    void deleteUserTest() {
        userService.createUser(userDto);
        UserDto userFromDB = userService.getAllUsers().get(0);

        userService.deleteUser(userFromDB.getId());
        List<UserDto> dtoFromDB = userService.getAllUsers();

        assertThat(dtoFromDB.size(), equalTo(0));
        final MissingIdException exception = Assertions.assertThrows(MissingIdException.class,
                () -> userService.getUserById(userFromDB.getId()));
        Assertions.assertEquals("При запросе пользователя произошла ошибка", exception.getMessage());
    }

    @Test
    void createUserInvalidEmailTest() {
        UserDto userDtoBadEmail = userDto = UserDto.builder()
                .email("bademail.com")
                .name("testName")
                .build();

        Assertions.assertThrows(ConstraintViolationException.class, () -> userService.createUser(userDtoBadEmail));
    }
}



