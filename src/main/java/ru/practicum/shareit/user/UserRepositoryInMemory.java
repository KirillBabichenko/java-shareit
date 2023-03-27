package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.MissingIdException;
import ru.practicum.shareit.exception.MissingValidationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;

@Repository
public class UserRepositoryInMemory implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private Long idUser = 0L;

    @Override
    public User createUser(User user) {
        User validUser = emailValidation(user);
        validUser.setId(++idUser);
        users.put(idUser, validUser);
        return users.get(idUser);
    }

    @Override
    public User updateUser(Long id, User user) {
        checkUser(id);
        emailValidationExceptId(id, user);
        User updateUsers = users.get(id);
        ofNullable(user.getName()).ifPresent(updateUsers::setName);
        ofNullable(user.getEmail()).ifPresent(updateUsers::setEmail);
        users.put(id, updateUsers);
        return users.get(id);
    }

    @Override
    public User getUserById(Long id) {
        checkUser(id);
        return users.get(id);
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void deleteUser(Long id) {
        checkUser(id);
        users.remove(id);
    }

    private User emailValidation(User verifiedUser) {
        if (users.values().stream().anyMatch(user -> user.getEmail().equals(verifiedUser.getEmail()))) {
            throw new MissingValidationException("Пользователь с email = " +
                    verifiedUser.getEmail() + " уже существует");
        } else {
            return verifiedUser;
        }
    }

    private void emailValidationExceptId(Long id, User verifiedUser) {
        if (users.values().stream()
                .filter(user -> !user.getId().equals(id))
                .anyMatch(user -> user.getEmail().equals(verifiedUser.getEmail()))) {
            throw new MissingValidationException("Пользователь с email = " +
                    verifiedUser.getEmail() + " уже существует");
        }
    }

    private void checkUser(Long id) {
        if (!users.containsKey(id)) {
            throw new MissingIdException("Пользователя с таким id нет");
        }
    }
}
