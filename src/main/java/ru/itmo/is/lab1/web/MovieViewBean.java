package ru.itmo.is.lab1.web;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import ru.itmo.is.lab1.model.Movie;
import ru.itmo.is.lab1.service.MovieService;
import ru.itmo.is.lab1.service.dto.MovieDetails;

import java.io.Serializable;

/** Страница «Фильм по ID»: сведения о фильме и связанных с ним объектах. */
@Named("movieView")
@ViewScoped
public class MovieViewBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private MovieService movies;

    /** Параметр ?id= из адреса страницы. */
    private String idParam;
    private Long id;
    private Long searchId;
    private MovieDetails details;

    /** Вызывается при открытии страницы (f:viewAction). */
    public void load() {
        if (idParam == null || idParam.isBlank()) {
            return;
        }
        try {
            id = Long.valueOf(idParam.strip());
        } catch (NumberFormatException e) {
            Messages.error("Некорректный ID «" + idParam + "»: ожидается целое число больше 0");
            return;
        }
        if (id <= 0) {
            Messages.error("ID фильма должен быть больше 0");
            id = null;
            return;
        }
        searchId = id;
        reload();
    }

    /** Перечитывает данные (в том числе по уведомлению об изменениях от других пользователей). */
    public void reload() {
        details = id == null ? null : movies.findDetails(id).orElse(null);
    }

    public String search() {
        return "/movie?faces-redirect=true&id=" + searchId;
    }

    public boolean isMissing() {
        return id != null && details == null;
    }

    public Movie getMovie() {
        return details == null ? null : details.getMovie();
    }

    public MovieDetails getDetails() {
        return details;
    }

    public String getIdParam() {
        return idParam;
    }

    public void setIdParam(String idParam) {
        this.idParam = idParam;
    }

    public Long getId() {
        return id;
    }

    public Long getSearchId() {
        return searchId;
    }

    public void setSearchId(Long searchId) {
        this.searchId = searchId;
    }
}
