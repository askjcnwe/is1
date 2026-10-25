package ru.itmo.is.lab1.web;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import ru.itmo.is.lab1.model.Color;
import ru.itmo.is.lab1.model.MovieGenre;
import ru.itmo.is.lab1.model.MpaaRating;

/** Значения перечислений для выпадающих списков и фильтров. */
@Named("enums")
@ApplicationScoped
public class EnumOptions {

    public MpaaRating[] getMpaaRatings() {
        return MpaaRating.values();
    }

    public MovieGenre[] getGenres() {
        return MovieGenre.values();
    }

    public Color[] getColors() {
        return Color.values();
    }
}
