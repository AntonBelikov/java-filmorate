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

    @Override
    public List<Genre> findAll() {
        String sql = "SELECT * FROM genres ORDER BY id";
        return jdbc.query(sql, mapper);
    }

    @Override
    public Optional<Genre> findById(int id) {
        String sql =  "SELECT * FROM genres WHERE id = ?";
        return jdbc.query(sql, mapper, id)
                .stream()
                .findFirst();
    }

    public List<Genre> findGenresByFilmId(int filmId) {
        String sql = "SELECT g.* FROM genres g " +
                "JOIN film_genres fg ON g.id = fg.genre_id " +
                "WHERE fg.film_id = ? " +
                "ORDER BY g.id";
        return jdbc.query(sql, mapper, filmId);
    }

    public void addGenresToFilm(int filmId, Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

        genres.stream()
                .map(Genre::getId)
                .distinct()
                .forEach(id -> jdbc.update(sql, filmId, id));
    }

    @Override
    public List<Genre> findByIds(Set<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        String placeholders = ids.stream()
                .map(id -> "?")
                .collect(Collectors.joining(","));

        String sql = """
                SELECT * FROM genres
                WHERE id IN (%s)
                ORDER BY id
                """.formatted(placeholders);

        return jdbc.query(sql, mapper, ids.toArray());
    }

    @Override
    public Map<Integer, List<Genre>> findGenresForFilms(Set<Integer> filmIds) {
        if (filmIds.isEmpty()) return Map.of();

        String placeholders = filmIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(","));

        String sql = """
                SELECT fg.film_id, g.id, g.name
                FROM film_genres fg
                JOIN genres g ON g.id = fg.genre_id
                WHERE fg.film_id IN (%s)
                ORDER BY g.id
                """.formatted(placeholders);

        Map<Integer, List<Genre>> result = new HashMap<>();

        jdbc.query(sql, rs -> {
            int filmId = rs.getInt("film_id");
            Genre genre = new Genre(rs.getInt("id"), rs.getString("name"));

            result.computeIfAbsent(filmId, k -> new ArrayList<>()).add(genre);
        }, filmIds.toArray());

        return result;
    }
}
