package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.exception.InvalidValueException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDaoStorage;

import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class FilmDbStorage implements FilmDaoStorage {

    private final JdbcTemplate jdbcTemplate;
    private final UserDaoStorage userDaoStorage;

    public FilmDbStorage(JdbcTemplate jdbcTemplate, UserDaoStorage userDaoStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.userDaoStorage = userDaoStorage;
    }

    @Override
    public Film getFilmById(Long id) {
        if (id < 1) {
            throw new InvalidValueException("Введен некорректный идентификатор фильма.");
        }
        String sql =
                "SELECT f.FILM_ID, f.FILM_NAME, f.DESCRIPTION, f.RELEASE_DATE, " +
                        "f.DURATION, f.MPA_ID, m.MPA_NAME " +
                        "FROM FILMS f " +
                        "JOIN MPA AS m ON f.MPA_ID = m.MPA_ID " +
                        "WHERE f.FILM_ID = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), id)
                .stream().findAny().orElse(null);
    }

    @Override
    public List<Film> getAllFilms() {
        String sql =
                "SELECT f.FILM_ID, f.FILM_NAME, f.DESCRIPTION, f.RELEASE_DATE, " +
                        "f.DURATION, f.MPA_ID, m.MPA_NAME " +
                        "FROM FILMS f " +
                        "JOIN MPA AS m ON f.MPA_ID = m.MPA_ID " +
                        "ORDER BY f.FILM_ID";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs));
    }

    @Override
    public Film createFilm(Film film) {
        if (film == null) {
            throw new EntityNotFoundException("Передан пустой фильм.");
        }
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql =
                "INSERT INTO FILMS (FILM_NAME, DESCRIPTION, RELEASE_DATE, DURATION, MPA_ID) " +
                        "VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"FILM_ID"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setInt(4, film.getDuration());
            stmt.setInt(5, film.getMpa().getId());
            return stmt;
        }, keyHolder);
        film.setId(keyHolder.getKey().longValue());
        return film;
    }

    @Override
    public void createGenreByFilm(Film film) {
        String sql = "INSERT INTO GENRE_FILM (FILM_ID, GENRE_ID) VALUES(?, ?)";
        Set<Genre> genres = film.getGenres();
        if (genres == null) {
            return;
        }
        for (Genre genre : genres ) {
            jdbcTemplate.update(sql, film.getId(), genre.getId());
        }
    }

    @Override
    public Film updateFilm(Film film) {
        if (getFilmById(film.getId()) == null) {
            throw new EntityNotFoundException("Фильм не найден для обновления.");
        }
        if (film.getId() < 1) {
            throw new InvalidValueException("Введен некорректный идентификатор фильма.");
        }
        String sql =
                "UPDATE FILMS " +
                        "SET FILM_NAME = ?, DESCRIPTION = ?, RELEASE_DATE = ?, " +
                        "DURATION = ?, MPA_ID =? " +
                        "WHERE FILM_ID = ?";
        jdbcTemplate.update(sql, film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpa().getId(), film.getId());
        return film;
    }

    @Override
    public void deleteFilm(Film film) {
        if (film == null) {
            throw new EntityNotFoundException("Фильм не найден для удаления.");
        }
        if (film.getId() < 1) {
            throw new InvalidValueException("Введен некорректный идентификатор фильма.");
        }
        String sql =
                "DELETE " +
                        "FROM FILMS " +
                        "WHERE FILM_ID = ?";
        jdbcTemplate.update(sql, film.getId());
    }

    public Integer putLike(Long filmId, Long userId) {
        if (getFilmById(filmId).getLike().contains(userId)) {
            throw new ValidationException("Данный пользователь уже оценивал этот фильм.");
        }
        Film film = getFilmById(filmId);
        User user = userDaoStorage.getUserById(userId);

        getFilmById(filmId).getLike().add(userId);
        return getFilmById(filmId).getLike().size();
    }
//    @Override
//    public void updateGenre(Film film) {
//        String sql = "DELETE FROM GENRE_FILM WHERE FILM_ID = ?";
//        jdbcTemplate.update(sql, film.getId());
//        createGenreByFilm(film);
//    }

    private Film makeFilm(ResultSet rs) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("FILM_ID"));
        film.setName(rs.getString("FILM_NAME"));
        film.setDescription(rs.getString("DESCRIPTION"));
        film.setReleaseDate(rs.getDate("RELEASE_DATE").toLocalDate());
        film.setDuration(rs.getInt("DURATION"));
        film.setMpa(new Mpa(rs.getInt("MPA_ID"), rs.getString("MPA_NAME")));

        return film;
                /*new Film(
                rs.getString("FILM_NAME"),
                rs.getString("DESCRIPTION"),
                rs.getLong("FILM_ID"),
                rs.getDate("RELEASE_DATE").toLocalDate(),
                rs.getLong("DURATION"),
                new Mpa(rs.getInt("MPA_ID"), rs.getString("MPA_NAME")));*/
    }
}


/*        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("FILMS")
                .usingGeneratedKeyColumns("FILM_ID");

        Map<String, Object> values = new HashMap<>();
        values.put("NAME", film.getName());
        values.put("DESCRIPTION", film.getDescription());
        values.put("RELEASE_DATE", film.getReleaseDate());
        values.put("DURATION", film.getDuration());
        values.put("RATING_ID", film.getMpa().getId());

        film.setId(simpleJdbcInsert.executeAndReturnKey(values).longValue());
        return film;*/