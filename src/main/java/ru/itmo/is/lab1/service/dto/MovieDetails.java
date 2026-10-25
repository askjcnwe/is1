package ru.itmo.is.lab1.service.dto;

import ru.itmo.is.lab1.model.Movie;

import java.io.Serializable;
import java.util.List;

/** Фильм и другие фильмы, которые используют те же координаты и тех же людей. */
public class MovieDetails implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Movie movie;
    private final List<Movie> sharingCoordinates;
    private final List<Movie> withDirector;
    private final List<Movie> withScreenwriter;
    private final List<Movie> withOperator;

    public MovieDetails(Movie movie, List<Movie> sharingCoordinates, List<Movie> withDirector,
                        List<Movie> withScreenwriter, List<Movie> withOperator) {
        this.movie = movie;
        this.sharingCoordinates = sharingCoordinates;
        this.withDirector = withDirector;
        this.withScreenwriter = withScreenwriter;
        this.withOperator = withOperator;
    }

    public Movie getMovie() {
        return movie;
    }

    public List<Movie> getSharingCoordinates() {
        return sharingCoordinates;
    }

    /** Другие фильмы, в которых режиссёр этого фильма указан в любой роли. */
    public List<Movie> getWithDirector() {
        return withDirector;
    }

    public List<Movie> getWithScreenwriter() {
        return withScreenwriter;
    }

    public List<Movie> getWithOperator() {
        return withOperator;
    }
}
