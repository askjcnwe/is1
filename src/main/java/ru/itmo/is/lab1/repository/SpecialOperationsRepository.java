package ru.itmo.is.lab1.repository;

import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import ru.itmo.is.lab1.model.Movie;
import ru.itmo.is.lab1.model.MovieGenre;
import ru.itmo.is.lab1.model.Person;

/** Вызов функций БД, реализующих специальные операции (см. sql/init.sql). */
@ApplicationScoped
public class SpecialOperationsRepository {

    @PersistenceContext(unitName = "moviesPU")
    private EntityManager em;

    @SuppressWarnings("unchecked")
    public Optional<Movie> findMovieWithMinUsaBoxOffice() {
        List<Movie> result = em.createNativeQuery("SELECT * FROM get_movie_with_min_usa_box_office()", Movie.class)
                .getResultList();
        return result.stream().findFirst();
    }

    public long countWithUsaBoxOfficeGreaterThan(float value) {
        return ((Number) em.createNativeQuery("SELECT count_movies_with_usa_box_office_greater(CAST(:value AS real))")
                .setParameter("value", value)
                .getSingleResult()).longValue();
    }

    @SuppressWarnings("unchecked")
    public List<Movie> findByTaglineSubstring(String substring) {
        return em.createNativeQuery("SELECT * FROM find_movies_by_tagline_substring(CAST(:substring AS varchar))",
                        Movie.class)
                .setParameter("substring", substring)
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Person> findScreenwritersWithoutOscars() {
        return em.createNativeQuery("SELECT * FROM find_screenwriters_without_oscars()", Person.class)
                .getResultList();
    }

    /** Все фильмы режиссёров, снявших хотя бы один фильм в указанном жанре. */
    @SuppressWarnings("unchecked")
    public List<Movie> findOscarsOfDirectorsByGenre(MovieGenre genre) {
        return em.createNativeQuery(
                        "SELECT * FROM find_oscars_of_directors_by_genre(CAST(:genre AS varchar))", Movie.class)
                .setParameter("genre", genre.name())
                .getResultList();
    }
}