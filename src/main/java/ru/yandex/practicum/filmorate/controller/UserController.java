package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.NotFoundObject;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private int idCounter = 1;
    private final Map<Integer, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        log.info("Получен список пользователей");
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        if (user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.error("Ошибка в Email");
            throw new ValidationException("Email пустой или не содержит @");
        }

        if (user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.error("Ошибка в login");
            throw new ValidationException("Логин пустой или не содержит пробелы");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Имя взято из логина");
            user.setName(user.getLogin());
        }

        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Дата рождения в будущем");
            throw new ValidationException("Дата рождения должна быть раньше текущей даты");
        }

        user.setId(idCounter++);
        users.put(user.getId(), user);
        log.info("Пользователь добавлен: {}", user);
        return user;
    }

    @PutMapping
    public User update(@RequestBody User user) {
        if (!users.containsKey(user.getId())) {
            log.error("Пользователь с id {} не найден", user.getId());
            throw new NotFoundObject("Пользователь с данным id не найден");
        }

        if (user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.error("Ошибка в Email");
            throw new ValidationException("Email пустой или не содержит @");
        }

        if (user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.error("Ошибка в login");
            throw new ValidationException("Логин пустой или не содержит пробелы");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Имя взято из логина");
            user.setName(user.getLogin());
        }

        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Дата рождения в будущем");
            throw new ValidationException("Дата рождения должна быть раньше текущей даты");
        }

        users.put(user.getId(), user);
        log.info("Пользователь обновлен: {}", user);
        return user;
    }
}
