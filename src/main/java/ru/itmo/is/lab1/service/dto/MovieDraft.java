package ru.itmo.is.lab1.service.dto;

import ru.itmo.is.lab1.model.Movie;

import java.io.Serializable;

/**
 * Данные формы создания/изменения фильма.
 * Собственные атрибуты хранятся в несохраняемом экземпляре {@link Movie}, чтобы поля формы
 * проверялись теми же ограничениями Bean Validation, что объявлены в сущности.
 */
public class MovieDraft implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Movie movie;
    private final CoordinatesChoice coordinates;
    private final PersonChoice director;
    private final PersonChoice screenwriter;
    private final PersonChoice operator;

    private MovieDraft(Movie movie, CoordinatesChoice coordinates,
                       PersonChoice director, PersonChoice screenwriter, PersonChoice operator) {
        this.movie = movie;
        this.coordinates = coordinates;
        this.director = director;
        this.screenwriter = screenwriter;
        this.operator = operator;
    }

    public static MovieDraft empty() {
        return new MovieDraft(new Movie(), CoordinatesChoice.createNew(),
                PersonChoice.createNew(true), PersonChoice.createNew(true), PersonChoice.createNew(false));
    }

    public static MovieDraft of(Movie source) {
        Movie values = new Movie();
        values.setName(source.getName());
        values.setOscarsCount(source.getOscarsCount());
        values.setBudget(source.getBudget());
        values.setTotalBoxOffice(source.getTotalBoxOffice());
        values.setMpaaRating(source.getMpaaRating());
        values.setLength(source.getLength());
        values.setGoldenPalmCount(source.getGoldenPalmCount());
        values.setUsaBoxOffice(source.getUsaBoxOffice());
        values.setTagline(source.getTagline());
        values.setGenre(source.getGenre());
        return new MovieDraft(values,
                CoordinatesChoice.editCurrent(source.getCoordinates()),
                PersonChoice.of(source.getDirector(), true),
                PersonChoice.of(source.getScreenwriter(), true),
                PersonChoice.of(source.getOperator(), false));
    }

    public Movie getMovie() {
        return movie;
    }

    public CoordinatesChoice getCoordinates() {
        return coordinates;
    }

    public PersonChoice getDirector() {
        return director;
    }

    public PersonChoice getScreenwriter() {
        return screenwriter;
    }

    public PersonChoice getOperator() {
        return operator;
    }
}
