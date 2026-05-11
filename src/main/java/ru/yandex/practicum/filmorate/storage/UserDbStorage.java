package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbc;
    private final UserRowMapper mapper;
    private static final String USER_CREATE = "INSERT INTO users(email, login, name, birthday) VALUES (?, ?, ?, ?)";
    private static final String USER_UPDATE = "UPDATE users SET email=?, login=?, name=?, birthday=? WHERE id=?";
    private static final String USER_DELETE = "DELETE FROM users WHERE id = ?";
    private static final String FIND_ALL = "SELECT * FROM users";
    private static final String FIND_BY_ID = "SELECT * FROM users WHERE id = ?";
    private static final String ADD_FRIENDS = "INSERT INTO friends (user_id, friend_id, status) VALUES (?, ?, ?)";
    private static final String REMOVE_FRIEND = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
    private static final String GET_FRINDS = "SELECT u.* FROM users u " +
            "JOIN friends f ON u.id = f.friend_id " +
            "WHERE f.user_id = ?";

    @Override
    public User create(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(USER_CREATE, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setObject(4, user.getBirthday());
            return ps;
        }, keyHolder);

        user.setId(keyHolder.getKey().intValue());
        return user;
    }

    @Override
    public void delete(int id) {
        jdbc.update(USER_DELETE, id);
    }

    @Override
    public User update(User user) {
        jdbc.update(
                USER_UPDATE,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()
        );

        return user;
    }

    @Override
    public Collection<User> findAll() {
        return jdbc.query(FIND_ALL, mapper);
    }

    @Override
    public Optional<User> findById(int id) {
        return jdbc.query(FIND_BY_ID, mapper, id)
                .stream()
                .findFirst();
    }

    @Override
    public void addFriend(int userId, int friendId) {
        jdbc.update(ADD_FRIENDS, userId, friendId, false);
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        jdbc.update(REMOVE_FRIEND, userId, friendId);
    }

    @Override
    public Collection<User> getFriends(int id) {
        return jdbc.query(GET_FRINDS, mapper, id);
    }
}
