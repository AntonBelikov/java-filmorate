package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundObject;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.MpaRatingDbStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MpaRatingService {
    private final MpaRatingDbStorage storage;

    public List<MpaRating> findAll() {
        return storage.findAll();
    }

    public MpaRating findById(int id) {
        return storage.findById(id)
                .orElseThrow(() -> new NotFoundObject("MPA с id = " + id + " не найден"));
    }
}
