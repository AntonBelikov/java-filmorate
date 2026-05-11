package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.mapper.MpaRatingRowMapper;
import ru.yandex.practicum.filmorate.storage.MpaRatingDbStorage;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({MpaRatingDbStorage.class, MpaRatingRowMapper.class})
public class MpaRatingDbStorageTest {

    @Autowired
    private MpaRatingDbStorage mpaRatingDbStorage;

    @Test
    void findAllShouldReturnFive() {
        assertThat(mpaRatingDbStorage.findAll()).hasSize(5);
    }

    @Test
    void findByIdShouldReturnCorrectRating() {
        assertThat(mpaRatingDbStorage.findById(1))
                .isPresent()
                .hasValueSatisfying(r ->
                        assertThat(r).hasFieldOrPropertyWithValue("id", 1)
                );
    }
}
