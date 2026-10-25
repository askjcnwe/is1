package ru.itmo.is.lab1.web;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.PrimeFaces;
import ru.itmo.is.lab1.model.Movie;
import ru.itmo.is.lab1.service.MovieService;
import ru.itmo.is.lab1.service.exception.NotFoundException;

import java.io.Serializable;
import java.util.Optional;

/** Диалоговое окно удаления фильма (доступно на всех страницах). */
@Named("movieDelete")
@ViewScoped
public class MovieDeleteBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private MovieService movies;

    private Movie target;

    public void prepare(Long id) {
        Optional<Movie> found = movies.findById(id);
        if (found.isEmpty()) {
            Messages.error(NotFoundException.movie(id).getMessage());
            return;
        }
        target = found.get();
        PrimeFaces.current().ajax().addCallbackParam("ready", true);
    }

    public void delete() {
        if (target == null) {
            return;
        }
        Long id = target.getId();
        String name = target.getName();
        if (Actions.perform("movieDeleteDialogForm", () -> movies.delete(id))) {
            Messages.info("Фильм «" + name + "» (ID " + id + ") удалён");
            PrimeFaces.current().ajax().addCallbackParam("deleted", true);
        }
    }

    public Movie getTarget() {
        return target;
    }
}
