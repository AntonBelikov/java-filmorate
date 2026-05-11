package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbc;
    private final GenreRowMapper mapper;
    private static final String FIND_ALL = "SELECT * FROM genres ORDER BY id";
    private static final String FIND_BY_ID = "SELECT * FROM genres WHERE id = ?";
    private static final String FIND_GENRE = "SELECT g.* FROM genres g " +
            "JOIN film_genres fg ON g.id = fg.genre_id " +
            "WHERE fg.film_id = ? " +
            "ORDER BY g.id";
    private static final String ADD_GENRE = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
    private static final String FIND_BY_IDS = """
                SELECT * FROM genres
                WHERE id IN (%s)
                ORDER BY id
                """;
    private static final String FIND_GENRES_FOR_FILM = """
                SELECT fg.film_id, g.id, g.name
                FROM film_genres fg
                JOIN genres g ON g.id = fg.genre_id
                WHERE fg.film_id IN (%s)
                ORDER BY g.id
                """;

    @Override
    public List<Genre> findAll() {
        return jdbc.query(FIND_ALL, mapper);
    }

    @Override
    public Optional<Genre> findById(int id) {
        return jdbc.query(FIND_BY_ID, mapper, id)
                .stream()
                .findFirst();
    }

    public List<Genre> findGenresByFilmId(int filmId) {
        return jdbc.query(FIND_GENRE, mapper, filmId);
    }

    public void addGenresToFilm(int filmId, Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return;
        }

        genres.stream()
                .map(Genre::getId)
                .distinct()
                .forEach(id -> jdbc.update(ADD_GENRE, filmId, id));
    }

    @Override
    public List<Genre> findByIds(Set<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        String placeholders = ids.stream()
                .map(id -> "?")
                .collect(Collectors.joining(","));

        return jdbc.query(FIND_BY_IDS.formatted(placeholders), mapper, ids.toArray());
    }

    @Override
    public Map<Integer, List<Genre>> findGenresForFilms(Set<Integer> filmIds) {
        if (filmIds.isEmpty()) return Map.of();

        String placeholders = filmIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(","));

        Map<Integer, List<Genre>> result = new HashMap<>();

        jdbc.query(FIND_GENRES_FOR_FILM.formatted(placeholders), rs -> {
            int filmId = rs.getInt("film_id");
            Genre genre = new Genre(rs.getInt("id"), rs.getString("name"));

            result.computeIfAbsent(filmId, k -> new ArrayList<>()).add(genre);
        }, filmIds.toArray());

        return result;
    }
}
