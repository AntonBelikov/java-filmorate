package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface GenreStorage {
    List<Genre> findAll();

    Optional<Genre> findById(int id);

    List<Genre> findGenresByFilmId(int filmId);

    List<Genre> findByIds(Set<Integer> ids);

    Map<Integer, List<Genre>> findGenresForFilms(Set<Integer> filmIds);
}
