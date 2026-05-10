package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, FilmRowMapper.class})
class FilmDbStorageTest {
    @Autowired
    private FilmDbStorage filmDbStorage;

    private Film makeFilm() {
        return new Film(
                1,
                "Film",
                "desc",
                LocalDate.of(2000, 1, 1),
                120,
                new MpaRating(1, "G"),
                new HashSet<>()
        );
    }

    @Test
    void createAndFindByIdShouldWork() {
        Film created = filmDbStorage.create(makeFilm());
        Optional<Film> fromDb = filmDbStorage.findById(created.getId());

        assertThat(fromDb)
                .isPresent()
                .hasValueSatisfying(f ->
                        assertThat(f).hasFieldOrPropertyWithValue("name", "Film")
                );
    }

    @Test
    void findAllShouldReturnList() {
        filmDbStorage.create(makeFilm());
        filmDbStorage.create(new Film(2, "F2", "d",
                LocalDate.of(2001, 1, 1), 100,
                new MpaRating(1, "G"),
                new HashSet<>()
        ));

        assertThat(filmDbStorage.findAll()).hasSize(2);
    }

    @Test
    void updateShouldModifyFilm() {
        Film f = filmDbStorage.create(makeFilm());
        f.setName("Updated");

        filmDbStorage.update(f);

        Optional<Film> updated = filmDbStorage.findById(f.getId());
        assertThat(updated)
                .isPresent()
                .hasValueSatisfying(x ->
                        assertThat(x).hasFieldOrPropertyWithValue("name", "Updated")
                );
    }
}
