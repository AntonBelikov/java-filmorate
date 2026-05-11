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
    private static final String FILM_CREATE = "INSERT INTO films(name, description, release_date, duration, mpa_id) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String FILM_DELETE = "DELETE FROM films WHERE id = ?";
    private static final String FILM_UPDATE = "UPDATE films SET name = ?, description = ?, release_date = ?, " +
            "duration = ?, mpa_id = ? WHERE id = ?";
    private static final String FIND_ALL_FILM = """
                SELECT f.*, m.name AS mpa_name
                FROM films f
                JOIN mpa_ratings m ON f.mpa_id = m.id
                """;
    private static final String FIND_BY_ID = "SELECT f.*, m.name AS mpa_name FROM films f " +
            "JOIN mpa_ratings m ON f.mpa_id = m.id WHERE f.id = ?";
    private static final String REMOVE_GENRE = "DELETE FROM film_genres WHERE film_id = ?";
    private static final String FILM_GENRE = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
    private static final String ADD_LIKE = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
    private static final String REMOVE_LIKE = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
    private static final String FIND_POPULAR = "SELECT f.*, m.name AS mpa_name \n" +
            "FROM films f \n" +
            "LEFT JOIN likes l ON f.id = l.film_id -- Используй LEFT JOIN\n" +
            "JOIN mpa_ratings m ON f.mpa_id = m.id \n" +
            "GROUP BY f.id, m.name \n" +
            "ORDER BY COUNT(l.user_id) DESC, f.id ASC \n" +
            "LIMIT ?";

    @Override
    public Film create(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(FILM_CREATE, Statement.RETURN_GENERATED_KEYS);
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
        jdbc.update(FILM_DELETE, id);
    }

    @Override
    public Film update(Film film) {
        jdbc.update(FILM_UPDATE, film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpa().getId(), film.getId());

        jdbc.update(REMOVE_GENRE, film.getId());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                jdbc.update(FILM_GENRE, film.getId(), genre.getId());
            }
        }

        return film;
    }

    @Override
    public Collection<Film> findAll() {
        List<Film> films = jdbc.query(FIND_ALL_FILM, mapper);

        if (films.isEmpty()) {
            return films;
        }

        Set<Integer> ids = films.stream()
                .map(Film::getId)
                .collect(Collectors.toSet());

        Map<Integer, List<Genre>> genresByFilm = genreDbStorage.findGenresForFilms(ids);

        for (Film film : films) {
            film.setGenres(new LinkedHashSet<>(
                    genresByFilm.getOrDefault(film.getId(), List.of())
            ));
        }

        return films;
    }

    @Override
    public Optional<Film> findById(int id) {
        return jdbc.query(FIND_BY_ID, mapper, id).stream().findFirst();
    }

    @Override
    public void addLike(int filmId, int userId) {
        jdbc.update(ADD_LIKE, filmId, userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        jdbc.update(REMOVE_LIKE, filmId, userId);
    }

    @Override
    public List<Film> getPopular(int count) {
        return jdbc.query(FIND_POPULAR, mapper, count);
    }
}
