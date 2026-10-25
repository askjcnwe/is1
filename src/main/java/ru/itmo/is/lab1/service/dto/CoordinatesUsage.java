package ru.itmo.is.lab1.service.dto;

import ru.itmo.is.lab1.model.Coordinates;

import java.io.Serializable;

/** Координаты и число фильмов, которые на них ссылаются. */
public class CoordinatesUsage implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Coordinates coordinates;
    private final long movies;

    public CoordinatesUsage(Coordinates coordinates, long movies) {
        this.coordinates = coordinates;
        this.movies = movies;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public long getMovies() {
        return movies;
    }
}
