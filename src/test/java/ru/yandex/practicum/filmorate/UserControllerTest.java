package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exceptions.NotFoundObject;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

public class UserControllerTest {
    private UserController userController;

    @BeforeEach
    void beforeEach() {
        userController = new UserController();
    }

    @Test
    void collectionTestAndLoginAndNameTest() {
        Assertions.assertEquals(0, userController.findAll().size());

        User user = new User("Kate@yandex.ru", "KateKate", "", LocalDate.of(1999, 12, 12));
        userController.create(user);

        Assertions.assertEquals(user.getLogin(), user.getName());
        Assertions.assertEquals(1, userController.findAll().size());
    }

    @Test
    void shouldThrowExceptionWhenEmailIsInvalid() {
        User user = new User("Kateyandex.ru", "KateKate", "", LocalDate.of(1999, 12, 12));
        Assertions.assertThrows(ValidationException.class, () -> userController.create(user));

        User user1 = new User("", "KateKate", "", LocalDate.of(1999, 12, 12));
        Assertions.assertThrows(ValidationException.class, () -> userController.create(user1));
    }

    @Test
    void shouldThrowExceptionWhenLoginIsInvalid() {
        User user = new User("Kate@yandex.ru", "", "", LocalDate.of(1999, 12, 12));
        Assertions.assertThrows(ValidationException.class, () -> userController.create(user));

        User user1 = new User("Kate@yandex.ru", "Kate Kate", "", LocalDate.of(1999, 12, 12));
        Assertions.assertThrows(ValidationException.class, () -> userController.create(user1));
    }

    @Test
    void shouldThrowExceptionWhenBirthdayIsAfterNow() {
        User user = new User("Kate@yandex.ru", "KateKate", "", LocalDate.of(2999, 12, 12));
        Assertions.assertThrows(ValidationException.class, () -> userController.create(user));

        User user1 = new User("Kate@yandex.ru", "KateKate", "", LocalDate.now().plusDays(1));
        Assertions.assertThrows(ValidationException.class, () -> userController.create(user1));
    }

    @Test
    void shouldChangeUserAndThrowExceptionWhenIdIsAbsent() {
        User user = new User("Kate@yandex.ru", "KateKate", "", LocalDate.of(1999, 12, 12));
        userController.create(user);

        User user1 = new User(1, "Kate@yandex.ru", "Katerina", "", LocalDate.of(1999, 10, 12));
        userController.update(user1);

        User user2 = new User(2, "Kate@yandex.ru", "Katerina", "", LocalDate.of(1999, 10, 12));

        Assertions.assertFalse(userController.findAll().contains(user));
        Assertions.assertTrue(userController.findAll().contains(user1));
        Assertions.assertThrows(NotFoundObject.class, () -> userController.update(user2));
    }
}
