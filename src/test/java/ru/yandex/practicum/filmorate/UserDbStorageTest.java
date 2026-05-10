package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbStorage.class, UserRowMapper.class})
public class UserDbStorageTest {
    @Autowired
    private UserDbStorage userDbStorage;

    private User makeUser() {
        return new User(
                1,
                "prog@mail.com",
                "proga",
                "ser",
                LocalDate.of(2000, 1, 1)
        );
    }

    @Test
    void createAndFindByIdShouldWork() {
        User created = userDbStorage.create(makeUser());
        Optional<User> fromDb = userDbStorage.findById(created.getId());

        assertThat(fromDb)
                .isPresent()
                .hasValueSatisfying(u ->
                        assertThat(u).hasFieldOrPropertyWithValue("email", "prog@mail.com")
                );
    }

    @Test
    void findAllShouldReturnList() {
        userDbStorage.create(makeUser());
        userDbStorage.create(new User(
                2, "2proga@a", "awda", "User2",
                LocalDate.of(1991,1,1)
        ));

        assertThat(userDbStorage.findAll()).hasSize(2);
    }

    @Test
    void updateShouldModifyUser() {
        User u = userDbStorage.create(makeUser());
        u.setName("Updated");

        userDbStorage.update(u);

        Optional<User> updated = userDbStorage.findById(u.getId());
        assertThat(updated)
                .isPresent()
                .hasValueSatisfying(x ->
                        assertThat(x).hasFieldOrPropertyWithValue("name", "Updated")
                );
    }
}
