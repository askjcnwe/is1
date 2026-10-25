package ru.itmo.is.lab1.web;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.PrimeFaces;
import ru.itmo.is.lab1.model.Coordinates;
import ru.itmo.is.lab1.model.Location;
import ru.itmo.is.lab1.model.Movie;
import ru.itmo.is.lab1.model.Person;
import ru.itmo.is.lab1.service.CoordinatesService;
import ru.itmo.is.lab1.service.LocationService;
import ru.itmo.is.lab1.service.MovieService;
import ru.itmo.is.lab1.service.PersonService;
import ru.itmo.is.lab1.service.dto.MovieDetails;
import ru.itmo.is.lab1.service.dto.MovieDraft;
import ru.itmo.is.lab1.service.exception.NotFoundException;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/** Диалоговое окно создания и изменения фильма (доступно на всех страницах). */
@Named("movieForm")
@ViewScoped
public class MovieFormBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String FORM = "movieFormDialogForm";

    @Inject
    private MovieService movies;

    @Inject
    private CoordinatesService coordinates;

    @Inject
    private PersonService persons;

    @Inject
    private LocationService locations;

    private Long editingId;
    private MovieDraft draft;
    private List<Coordinates> coordinatesOptions = List.of();
    private List<Person> personOptions = List.of();
    private List<Location> locationOptions = List.of();
    private int coordinatesSharedWith;

    public void prepareCreate() {
        editingId = null;
        draft = MovieDraft.empty();
        coordinatesSharedWith = 0;
        open();
    }

    public void prepareEdit(Long id) {
        Optional<MovieDetails> found = movies.findDetails(id);
        if (found.isEmpty()) {
            Messages.error(NotFoundException.movie(id).getMessage());
            return;
        }
        editingId = id;
        draft = MovieDraft.of(found.get().getMovie());
        coordinatesSharedWith = found.get().getSharingCoordinates().size();
        open();
    }

    private void open() {
        coordinatesOptions = coordinates.findAll();
        personOptions = persons.findAll();
        locationOptions = locations.findAll();
        PrimeFaces.current().resetInputs(":" + FORM);
        PrimeFaces.current().ajax().addCallbackParam("ready", true);
    }

    public void save() {
        boolean creating = editingId == null;
        boolean saved = Actions.perform(FORM, () -> {
            if (creating) {
                Movie created = movies.create(draft);
                Messages.info("Фильм «" + created.getName() + "» создан, ID " + created.getId());
            } else {
                movies.update(editingId, draft);
                Messages.info("Изменения фильма ID " + editingId + " сохранены");
            }
        });
        if (saved) {
            PrimeFaces.current().ajax().addCallbackParam("saved", true);
        }
    }

    public String getTitle() {
        return editingId == null ? "Новый фильм" : "Изменение фильма ID " + editingId;
    }

    public boolean isEditing() {
        return editingId != null;
    }

    public MovieDraft getDraft() {
        return draft;
    }

    public List<Coordinates> getCoordinatesOptions() {
        return coordinatesOptions;
    }

    public List<Person> getPersonOptions() {
        return personOptions;
    }

    public List<Location> getLocationOptions() {
        return locationOptions;
    }

    /** Сколько других фильмов используют текущие координаты (их изменение затронет и эти фильмы). */
    public int getCoordinatesSharedWith() {
        return coordinatesSharedWith;
    }
}
