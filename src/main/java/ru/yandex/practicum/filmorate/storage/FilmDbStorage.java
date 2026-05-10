package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbc;
    private final FilmRowMapper mapper;
    private final GenreDbStorage genreDbStorage;

    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO films(name, description, release_date, duration, mpa_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setObject(3, film.getReleaseDate());
            ps.setInt(4, film.getDuration());
            ps.setLong(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        film.setId(keyHolder.getKey().intValue());
        genreDbStorage.addGenresToFilm(film.getId(), film.getGenres());
        return film;
    }

    @Override
    public void delete(int id) {
        jdbc.update("DELETE FROM films WHERE id = ?", id);
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, " +
                "duration = ?, mpa_id = ? WHERE id = ?";

        jdbc.update(sql, film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpa().getId(), film.getId());

        jdbc.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String sqlGenres = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            for (Genre genre : film.getGenres()) {
                jdbc.update(sqlGenres, film.getId(), genre.getId());
            }
        }

        return film;
    }

    @Override
    public Collection<Film> findAll() {
        String sql = """
                SELECT f.*, m.name AS mpa_name
                FROM films f
                JOIN mpa_ratings m ON f.mpa_id = m.id
                """;

        List<Film> films = jdbc.query(sql, mapper);

        if (films.isEmpty()) {
            return films;
        }

        Set<Integer> ids = films.stream()
                .map(Film::getId)
                .collect(Collectors.toSet());

        Map<Integer, List<Genre>> genresByFilm = genreDbStorage.findGenresForFilms(ids);

        Map<Integer, Set<Integer>> likesByFilm = findLikesForFilms(ids);

        for (Film film : films) {
            film.setGenres(new LinkedHashSet<>(
                    genresByFilm.getOrDefault(film.getId(), List.of())
            ));
        }

        return films;
    }

    @Override
    public Optional<Film> findById(int id) {
        String sql = "SELECT f.*, m.name AS mpa_name FROM films f " +
                "JOIN mpa_ratings m ON f.mpa_id = m.id WHERE f.id = ?";
        return jdbc.query(sql, mapper, id).stream().findFirst();
    }

    @Override
    public void addLike(int filmId, int userId) {
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbc.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbc.update(sql, filmId, userId);
    }

    @Override
    public List<Film> getPopular(int count) {
        String sql = "SELECT f.*, m.name AS mpa_name \n" +
                "FROM films f \n" +
                "LEFT JOIN likes l ON f.id = l.film_id -- Используй LEFT JOIN\n" +
                "JOIN mpa_ratings m ON f.mpa_id = m.id \n" +
                "GROUP BY f.id, m.name \n" +
                "ORDER BY COUNT(l.user_id) DESC, f.id ASC \n" +
                "LIMIT ?";
        return jdbc.query(sql, mapper, count);
    }

    private Map<Integer, Set<Integer>> findLikesForFilms(Set<Integer> filmIds) {
        if (filmIds.isEmpty()) {
            return Map.of();
        }

        String placeholders = filmIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(","));

        String sql = """
                SELECT film_id, user_id
                FROM likes
                WHERE film_id IN (%s)
                """.formatted(placeholders);

        Map<Integer, Set<Integer>> result = new HashMap<>();

        jdbc.query(sql, rs -> {
            int filmId = rs.getInt("film_id");
            int userId = rs.getInt("user_id");
            result.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
        }, filmIds.toArray());

        return result;
    }
}
