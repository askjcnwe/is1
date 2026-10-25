package ru.itmo.is.lab1.web;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import ru.itmo.is.lab1.model.Movie;
import ru.itmo.is.lab1.model.MovieGenre;
import ru.itmo.is.lab1.model.Person;
import ru.itmo.is.lab1.service.SpecialOperationsService;

/** Страница специальных операций. */
@Named("specialOperations")
@ViewScoped
public class SpecialOperationsBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private SpecialOperationsService operations;

    private boolean minSearched;
    private Movie minMovie;

    private Float usaThreshold;
    private Long usaCount;

    private String taglineSubstring;
    private List<Movie> taglineResults;

    private List<Person> screenwriters;

    private MovieGenre genre;
    private List<Movie> oscarMovies;
    private long oscarsTotal;

    public void findMinUsaBoxOffice() {
        Actions.perform(() -> {
            Optional<Movie> found = operations.findMovieWithMinUsaBoxOffice();
            minMovie = found.orElse(null);
            minSearched = true;
            if (found.isEmpty()) {
                Messages.info("Фильмов пока нет");
            }
        });
    }

    public void countUsaBoxOfficeGreater() {
        Actions.perform(() -> usaCount = operations.countWithUsaBoxOfficeGreaterThan(usaThreshold));
    }

    public void findByTagline() {
        Actions.perform(() -> {
            taglineResults = operations.findByTaglineSubstring(taglineSubstring);
            Messages.info("Найдено фильмов: " + taglineResults.size());
        });
    }

    public void findScreenwriters() {
        Actions.perform(() -> {
            screenwriters = operations.findScreenwritersWithoutOscars();
            Messages.info("Найдено сценаристов: " + screenwriters.size());
        });
    }

    public void findOscars() {
        Actions.perform(() -> {
            oscarMovies = operations.findOscarsOfDirectorsByGenre(genre);
            oscarsTotal = oscarMovies.stream().mapToLong(Movie::getOscarsCount).sum();
            Messages.info("Найдено фильмов: " + oscarMovies.size() + ", «Оскаров» всего: " + oscarsTotal);
        });
    }

    public boolean isMinSearched() {
        return minSearched;
    }

    public Movie getMinMovie() {
        return minMovie;
    }

    public Float getUsaThreshold() {
        return usaThreshold;
    }

    public void setUsaThreshold(Float usaThreshold) {
        this.usaThreshold = usaThreshold;
    }

    public Long getUsaCount() {
        return usaCount;
    }

    public String getTaglineSubstring() {
        return taglineSubstring;
    }

    public void setTaglineSubstring(String taglineSubstring) {
        this.taglineSubstring = taglineSubstring;
    }

    public List<Movie> getTaglineResults() {
        return taglineResults;
    }

    public List<Person> getScreenwriters() {
        return screenwriters;
    }

    public MovieGenre getGenre() {
        return genre;
    }

    public void setGenre(MovieGenre genre) {
        this.genre = genre;
    }

    public List<Movie> getOscarMovies() {
        return oscarMovies;
    }

    public long getOscarsTotal() {
        return oscarsTotal;
    }
}