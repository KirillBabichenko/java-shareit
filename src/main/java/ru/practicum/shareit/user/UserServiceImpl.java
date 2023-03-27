package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    public User createUser(User user) {
        return repository.createUser(user);
    }

    @Override
    public User updateUser(Long id, User user) {
        return repository.updateUser(id, user);
    }

    @Override
    public User getUserById(Long id) {
        return repository.getUserById(id);
    }

    @Override
    public List<User> getAllUsers() {
        return repository.getAllUsers();
    }

    @Override
    public void deleteUser(Long id) {
        repository.deleteUser(id);
    }

}
