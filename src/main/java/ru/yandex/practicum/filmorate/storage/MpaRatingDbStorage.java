package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.mapper.MpaRatingRowMapper;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MpaRatingDbStorage implements MpaRatingStorage {
    private final JdbcTemplate jdbc;
    private final MpaRatingRowMapper mapper;

    @Override
    public List<MpaRating> findAll() {
        String sql =  "SELECT * FROM mpa_ratings ORDER BY id";
        return jdbc.query(sql, mapper);
    }

    @Override
    public Optional<MpaRating> findById(int id) {
        String sql = "SELECT * FROM mpa_ratings WHERE id = ?";
        return jdbc.query(sql, mapper, id)
                .stream()
                .findFirst();
    }
}
