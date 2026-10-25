package ru.itmo.is.lab1.web;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.model.LazyDataModel;
import ru.itmo.is.lab1.model.Movie;
import ru.itmo.is.lab1.service.MovieService;

import java.io.Serializable;

/** Главная таблица фильмов. */
@Named("movieTable")
@ViewScoped
public class MovieTableBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private MovieService movies;

    private LazyDataModel<Movie> model;

    @PostConstruct
    void init() {
        model = new MovieLazyDataModel(movies);
    }

    public LazyDataModel<Movie> getModel() {
        return model;
    }
}
