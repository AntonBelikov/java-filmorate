package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundObject;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaRatingStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final GenreStorage genreStorage;
    private final MpaRatingStorage mpaRatingStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserService userService, GenreStorage genreStorage,
                       MpaRatingStorage mpaRatingStorage) {
        this.filmStorage = filmStorage;
        this.userService = userService;
        this.genreStorage = genreStorage;
        this.mpaRatingStorage = mpaRatingStorage;
    }

    public Film createFilm(Film film) {
        validate(film);

        mpaRatingStorage.findById(film.getMpa().getId())
                .orElseThrow(() -> new NotFoundObject("MPA не найден"));

        film.setGenres(validateGenres(film.getGenres()));

        filmStorage.create(film);
        return getFilmOrElseThrow(film.getId());
    }

    public Film updateFilm(Film film) {
        validate(film);

        filmStorage.findById(film.getId())
                .orElseThrow(() -> new NotFoundObject("Фильм не найден"));

        mpaRatingStorage.findById(film.getMpa().getId())
                .orElseThrow(() -> new NotFoundObject("MPA не найден"));

        film.setGenres(validateGenres(film.getGenres()));

        filmStorage.update(film);
        return getFilmOrElseThrow(film.getId());
    }

    public void addLikes(int filmId, int userId) {
        User user = userService.getUserOrElseThrow(userId);
        Film film = getFilmOrElseThrow(filmId);
        filmStorage.addLike(filmId, userId);
        log.info("Пользователь {} поставил лайк фильму {}", user, film);
    }

    public void removeLikes(int filmId, int userId) {
        User user = userService.getUserOrElseThrow(userId);
        Film film = getFilmOrElseThrow(filmId);
        filmStorage.removeLike(filmId, userId);
        log.info("Пользователь {} убрал лайк фильму {}", user, film);
    }

    public Collection<Film> getMostPopular(int size) {
        log.info("Получен список {} самых популярных фильмов", size);
        return filmStorage.getPopular(size);
    }

    public Collection<Film> getAll() {
        log.info("Получен список фильмов");
        Collection<Film> films = filmStorage.findAll();

        Set<Integer> ids = films.stream()
                .map(Film::getId)
                .collect(Collectors.toSet());

        Map<Integer, List<Genre>> genresByFilm = genreStorage.findGenresForFilms(ids);

        films.forEach(film -> {
            TreeSet<Genre> sorted = new TreeSet<>(Comparator.comparingLong(Genre::getId));
            sorted.addAll(genresByFilm.getOrDefault(film.getId(), List.of()));
            film.setGenres(sorted);
        });

        return films;
    }

    public Film getFilmOrElseThrow(int id) {
        log.info("Проверка фильма по списку");
        Film film = filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundObject("Фильм не найден: " + id));

        List<Genre> genres = genreStorage.findGenresByFilmId(id);

        TreeSet<Genre> sorted = new TreeSet<>(Comparator.comparingLong(Genre::getId));
        sorted.addAll(genres);

        film.setGenres(sorted);

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

    private Set<Genre> validateGenres(Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return Set.of();
        }

        Set<Integer> ids = genres.stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());

        List<Genre> existing = genreStorage.findByIds(ids);

        if (existing.size() != ids.size()) {
            throw new NotFoundObject("Некоторые жанры не существуют");
        }

        return existing.stream()
                .collect(Collectors.toCollection(
                        () -> new TreeSet<>(Comparator.comparingLong(Genre::getId))
                ));
    }
}
