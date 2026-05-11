package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundObject;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(int userId, int friendId) {
        User user1 = getUserOrElseThrow(userId);
        User user2 = getUserOrElseThrow(friendId);

        userStorage.addFriend(userId, friendId);
        log.info("Пользователь {} добавлен в друзья пользователя {}", user1, user2);
    }

    public User createUser(User user) {
        validate(user);
        log.info("Пользователь добавлен: {}", user);
        return userStorage.create(user);
    }

    public User updateUser(User user) {
        validate(user);

        if (userStorage.findById(user.getId()).isEmpty()) {
            log.error("Пользователь с id {} не найден", user.getId());
            throw new NotFoundObject("Пользователь с данным id не найден");
        }

        log.info("Пользователь обновлен: {}", user);
        return userStorage.update(user);
    }

    public void removeFriend(int userId, int friendId) {
        User user1 = getUserOrElseThrow(userId);
        User user2 = getUserOrElseThrow(friendId);

        userStorage.removeFriend(userId, friendId);
        log.info("Пользователь {} удален из друзей пользователя {}", user1, user2);
    }

    public Collection<User> getUserFrinds(int id) {
        User user = getUserOrElseThrow(id);
        log.info("Получен списко друзей пользователя {}", user);
        return userStorage.getFriends(id);
    }

    public Collection<User> getSameFriends(int userId, int anotherUserId) {
        User user1 = getUserOrElseThrow(userId);
        User user2 = getUserOrElseThrow(anotherUserId);
        log.info("Получен списко совместных друзей пользователей {} и {}", user1, user2);
        return userStorage.getFriends(userId).stream()
                .filter(userStorage.getFriends(anotherUserId)::contains)
                .collect(Collectors.toList());
    }

    public Collection<User> getAll() {
        log.info("Получен список пользователей");
        return userStorage.findAll();
    }

    public User getUserOrElseThrow(int id) {
        log.info("Проверка наличия пользователя в списке");
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundObject("Пользователь не найден: " + id));
    }

    private void validate(User user) {
        if (user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.error("Ошибка в Email");
            throw new ValidationException("Email пустой или не содержит @");
        }

        if (user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.error("Ошибка в login");
            throw new ValidationException("Логин пустой или содержит пробелы");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Имя взято из логина");
            user.setName(user.getLogin());
        }

        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Дата рождения в будущем");
            throw new ValidationException("Дата рождения должна быть раньше текущей даты");
        }
    }
}
