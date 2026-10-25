package ru.itmo.is.lab1.service;

import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import ru.itmo.is.lab1.model.Movie;
import ru.itmo.is.lab1.model.MovieGenre;
import ru.itmo.is.lab1.model.Person;
import ru.itmo.is.lab1.repository.SpecialOperationsRepository;
import ru.itmo.is.lab1.service.exception.BusinessException;

/** Специальные операции; сами операции реализованы функциями БД. */
@ApplicationScoped
@Transactional
public class SpecialOperationsService {

    @Inject
    private SpecialOperationsRepository operations;

    public Optional<Movie> findMovieWithMinUsaBoxOffice() {
        return DataAccess.call(operations::findMovieWithMinUsaBoxOffice);
    }

    public long countWithUsaBoxOfficeGreaterThan(Float value) {
        if (value == null || !Float.isFinite(value)) {
            throw new BusinessException("Введите число, с которым сравнивать сборы в США");
        }
        return DataAccess.call(() -> operations.countWithUsaBoxOfficeGreaterThan(value));
    }

    public List<Movie> findByTaglineSubstring(String substring) {
        if (substring == null || substring.isEmpty()) {
            throw new BusinessException("Введите подстроку для поиска в слогане");
        }
        return DataAccess.call(() -> operations.findByTaglineSubstring(substring));
    }

    public List<Person> findScreenwritersWithoutOscars() {
        return DataAccess.call(operations::findScreenwritersWithoutOscars);
    }

    /** Фильмы всех режиссёров, снявших хотя бы один фильм в жанре genre (данные не изменяются). */
    public List<Movie> findOscarsOfDirectorsByGenre(MovieGenre genre) {
        if (genre == null) {
            throw new BusinessException("Выберите жанр");
        }
        return DataAccess.call(() -> operations.findOscarsOfDirectorsByGenre(genre));
    }
}