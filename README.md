# Сервис для работы с фильмами

**Данная программа позволяет оценивать фильмы и получать топ-фильмов по запросу**

Используемые стек: Java 11, Spring Boot, Maven, H2

Реализовал CRUD-операции для моделей: Film, User, Genre; удалить/поставить лайк фильму, получение популярных фильмов, добавление в друзья, получение списка общих друзей.

Данные хранятся в БД. Схема БД представлена ниже.
https://drawsql.app/yandex-7/diagrams/filmorate/embed

Примеры Endpoint запросов (программа написана на Java):

```java

    @PostMapping(value = "/users")
    public User createUser(@Valid @RequestBody User user) {
        return userService.createUser(user);
    }
....
    @PutMapping(value = "/films/{id}/like/{userId}")
    public Integer putLikeFilm(
            @PathVariable @Min(1) Long id,
            @PathVariable @Min(1) Long userId) {
        return filmService.putLike(id, userId);
    }
}
```

Примеры запросов в БД (используемый язык: Java и SQL):

```java

    @Override
    public Genre getGenreById(Long genreId) {
        if (genreId < 1) {
            throw new InvalidValueException("Введен некорректный идентификатор жанра.");
        }
        String sql =
                "SELECT * " +
                        "FROM GENRES " +
                        "WHERE GENRE_ID = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeGenre(rs), genreId)
                .stream().findAny().orElse(null);
    }
```
