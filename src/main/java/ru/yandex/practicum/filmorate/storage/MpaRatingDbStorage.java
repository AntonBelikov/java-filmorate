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
    private static final String FIND_ALL =  "SELECT * FROM mpa_ratings ORDER BY id";
    private static final String FIND_BY_ID = "SELECT * FROM mpa_ratings WHERE id = ?";

    @Override
    public List<MpaRating> findAll() {
        return jdbc.query(FIND_ALL, mapper);
    }

    @Override
    public Optional<MpaRating> findById(int id) {
        return jdbc.query(FIND_BY_ID, mapper, id)
                .stream()
                .findFirst();
    }
}
