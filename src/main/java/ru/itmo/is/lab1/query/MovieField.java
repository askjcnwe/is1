package ru.itmo.is.lab1.query;

import java.util.Arrays;
import java.util.Optional;

/** Колонки таблицы фильмов: путь к свойству и признак строковой колонки (по ней разрешена фильтрация). */
public enum MovieField {
    ID("id", false),
    NAME("name", true),
    COORDINATES_X("coordinates.x", false),
    COORDINATES_Y("coordinates.y", false),
    CREATION_DATE("creationDate", false),
    OSCARS_COUNT("oscarsCount", false),
    BUDGET("budget", false),
    TOTAL_BOX_OFFICE("totalBoxOffice", false),
    MPAA_RATING("mpaaRating", true),
    DIRECTOR("director.name", true),
    SCREENWRITER("screenwriter.name", true),
    OPERATOR("operator.name", true),
    LENGTH("length", false),
    GOLDEN_PALM_COUNT("goldenPalmCount", false),
    USA_BOX_OFFICE("usaBoxOffice", false),
    TAGLINE("tagline", true),
    GENRE("genre", true);

    private final String property;
    private final boolean textual;

    MovieField(String property, boolean textual) {
        this.property = property;
        this.textual = textual;
    }

    public String getProperty() {
        return property;
    }

    public boolean isTextual() {
        return textual;
    }

    public static Optional<MovieField> fromProperty(String property) {
        return Arrays.stream(values()).filter(field -> field.property.equals(property)).findFirst();
    }
}
