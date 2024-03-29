package ru.yandex.practicum.filmorate.storage.like;

import ru.yandex.practicum.filmorate.model.Film;

public interface LikeDaoStorage {

    void saveLikes(Long id, Long userId);

    void removeLikes(Long id, Long userId);
}
