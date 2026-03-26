package ru.yandex.practicum.filmorate.exceptions;

public class NotFoundObject extends RuntimeException {
    public NotFoundObject(String massage) {
        super(massage);
    }
}
