package ru.yandex.practicum.filmorate.exceptions;

public class NotFoundObject extends RuntimeException {
    public NotFoundObject(String message) {
        super(message);
    }
}
