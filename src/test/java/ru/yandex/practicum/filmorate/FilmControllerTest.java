package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exceptions.NotFoundObject;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

public class FilmControllerTest {
    private FilmController filmController;

    @BeforeEach
    void beforeEach() {
        filmController = new FilmController();
    }

    @Test
    void collectionTestAndPostTest() {
        Assertions.assertEquals(0, filmController.findAll().size());

        Film film = new Film("Зеленый фонарь", "Лучшая работа Рельнольдса", LocalDate.of(2011, 6, 16), 120);
        filmController.create(film);

        Assertions.assertEquals(1, filmController.findAll().size());
    }

    @Test
    void shouldThrowExceptionWhenNameIsInvalid() {
        Film film = new Film("", "Лучшая работа Рельнольдса", LocalDate.of(2011, 6, 16), 120);
        Assertions.assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void shouldThrowExceptionWhenDescriptionLengthMoreThan200() {
        Film film = new Film("Зеленый фонарь", "Лучшая работа Рельнольдса, лучшая работа Рельнольдса," +
                "лучшая работа Рельнольдса, лучшая работа Рельнольдса," +
                "лучшая работа Рельнольдса, лучшая работа Рельнольдса," +
                "лучшая работа Рельнольдса, лучшая работа Рельнольдса", LocalDate.of(2011, 6, 16), 120);
        Assertions.assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void shouldThrowExceptionWhenReleaseDateIsBeforeDate() {
        Film film = new Film("Зеленый фонарь", "Лучшая работа Рельнольдса", LocalDate.of(1849, 6, 16), 120);
        Assertions.assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void shouldThrowExceptionWhenHaveMinusDuration() {
        Film film = new Film("Зеленый фонарь", "Лучшая работа Рельнольдса", LocalDate.of(2011, 6, 16), -120);
        Assertions.assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void shouldChangeFilmAndThrowExceptionWhenIdIsAbsent() {
        Film film = new Film("Зеленый фонарь", "Лучшая работа Рельнольдса", LocalDate.of(2011, 6, 16), 120);
        filmController.create(film);

        Film film1 = new Film(1, "Зеленый фонарь", "Могло быть и лучше", LocalDate.of(2011, 6, 16), 120);
        filmController.update(film1);

        Film film2 = new Film(2, "Зеленый фонарь", "Могло быть и лучше", LocalDate.of(2011, 6, 16), 120);

        Assertions.assertFalse(filmController.findAll().contains(film));
        Assertions.assertTrue(filmController.findAll().contains(film1));
        Assertions.assertThrows(NotFoundObject.class, () -> filmController.update(film2));
    }
}
