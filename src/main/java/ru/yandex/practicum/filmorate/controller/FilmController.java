package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.NotFoundObject;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private int idCounter = 1;
    private final Map<Integer, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получен список фильмов");
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        validate(film);
        film.setId(idCounter++);
        films.put(film.getId(), film);
        log.info("Фильм добавлен: {}", film);
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        if (!films.containsKey(film.getId())) {
            log.error("Фильм с id {} не найден", film.getId());
            throw new NotFoundObject("Фильм с данным id не найден");
        }

        validate(film);
        films.put(film.getId(), film);
        log.info("Фильм обновлен: {}", film);
        return film;
    }

    private void validate(Film film) {
        if (film.getName().isBlank()) {
            log.error("У фильма пустое название");
            throw new ValidationException("Название не может быть пустым");
        }

        if (film.getDescription().length() > 200) {
            log.error("В описании фильма много символов");
            throw new ValidationException("Описание не должно быть длиннее 200 символов");
        }

        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("Очень ранний фильм");
            throw new ValidationException("Дата должна быть после 28 декабря 1895 года");
        }

        if (film.getDuration() < 0) {
            log.error("Продолжительность отрицательная");
            throw new ValidationException("Продолжительность не может быть меньше 0");
        }
    }
}
