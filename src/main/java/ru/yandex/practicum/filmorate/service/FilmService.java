package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundObject;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public Film createFilm(Film film) {
        validate(film);
        log.info("Фильм добавлен: {}", film);
        return filmStorage.create(film);
    }

    public Film updateFilm(Film film) {
        validate(film);

        if (filmStorage.findById(film.getId()).isEmpty()) {
            log.error("Фильм с id {} не найден", film.getId());
            throw new NotFoundObject("Фильм с данным id не найден");
        }

        log.info("Фильм обновлен: {}", film);
        return filmStorage.update(film);
    }

    public void addLikes(int filmId, int userId) {
        User user = userService.getUserOrElseThrow(userId);
        Film film = getUserOrElseThrow(filmId);
        film.getLikes().add(userId);
        log.info("Пользователь {} поставил лайк фильму {}", user, film);
    }

    public void removeLikes(int filmId, int userId) {
        User user = userService.getUserOrElseThrow(userId);
        Film film = getUserOrElseThrow(filmId);
        getUserOrElseThrow(filmId).getLikes().remove(userId);
        log.info("Пользователь {} убрал лайк фильму {}", user, film);
    }

    public Collection<Film> getMostPopular(int size) {
        log.info("Получен список {} самых популярных фильмов", size);
        return filmStorage.findAll().stream()
                .sorted((film1, film2) -> film2.getLikes().size() - film1.getLikes().size())
                .limit(size)
                .collect(Collectors.toList());
    }

    public Collection<Film> getAll() {
        log.info("Получен список фильмов");
        return filmStorage.findAll();
    }

    public Film getUserOrElseThrow(int id) {
        log.info("Проверка фильма по списку");
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundObject("Фильм не найден: " + id));
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
