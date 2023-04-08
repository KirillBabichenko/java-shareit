package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.MissingIdException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static ru.practicum.shareit.user.dto.UserMapper.toUser;
import static ru.practicum.shareit.user.dto.UserMapper.toUserDto;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepositoryJpa repositoryJpa;

    //Создание пользователя
    @Override
    public UserDto createUser(UserDto userDto) {
        User user = toUser(userDto);
        return toUserDto(repositoryJpa.save(user));
    }

    //Обновление информации о пользователе
    @Override
    public UserDto updateUser(Long id, UserDto userDto) {
        User updateUser = repositoryJpa.findById(id)
                .orElseThrow(() -> new MissingIdException("При обновлении пользователя произошла ошибка"));
        ofNullable(userDto.getName()).ifPresent(updateUser::setName);
        ofNullable(userDto.getEmail()).ifPresent(updateUser::setEmail);
        return toUserDto(repositoryJpa.save(updateUser));
    }

    //Запрос пользователя по id
    @Override
    public UserDto getUserById(Long id) {
        User user = repositoryJpa.findById(id)
                .orElseThrow(() -> new MissingIdException("При запросе пользователя произошла ошибка"));
        return toUserDto(user);
    }

    //Запрос всех пользователей
    @Override
    public List<UserDto> getAllUsers() {
        return repositoryJpa.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    //Удаление пользователя
    @Override
    public void deleteUser(Long id) {
        repositoryJpa.deleteById(id);
    }

}
