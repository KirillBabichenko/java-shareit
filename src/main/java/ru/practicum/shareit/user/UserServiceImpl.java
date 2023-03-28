package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.user.dto.UserMapper.toUser;
import static ru.practicum.shareit.user.dto.UserMapper.toUserDto;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    public UserDto createUser(UserDto userDto) {
        User user = toUser(userDto);
        return toUserDto(repository.createUser(user));
    }

    @Override
    public UserDto updateUser(Long id, UserDto userDto) {
        User user = toUser(userDto);
        return toUserDto(repository.updateUser(id, user));
    }

    @Override
    public UserDto getUserById(Long id) {
        return toUserDto(repository.getUserById(id));
    }

    @Override
    public List<UserDto> getAllUsers() {
        return repository.getAllUsers().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long id) {
        repository.deleteUser(id);
    }

}
