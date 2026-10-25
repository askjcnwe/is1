package ru.itmo.is.lab1.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import ru.itmo.is.lab1.model.Coordinates;
import ru.itmo.is.lab1.model.Movie;
import ru.itmo.is.lab1.model.Person;
import ru.itmo.is.lab1.query.MovieField;
import ru.itmo.is.lab1.query.MovieQuery;
import ru.itmo.is.lab1.realtime.DataChangedEvent;
import ru.itmo.is.lab1.repository.CoordinatesRepository;
import ru.itmo.is.lab1.repository.MovieRepository;
import ru.itmo.is.lab1.service.dto.CoordinatesChoice;
import ru.itmo.is.lab1.service.dto.MovieDetails;
import ru.itmo.is.lab1.service.dto.MovieDraft;
import ru.itmo.is.lab1.service.exception.BusinessException;
import ru.itmo.is.lab1.service.exception.NotFoundException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ru.itmo.is.lab1.realtime.DataChangedEvent.Subject.COORDINATES;
import static ru.itmo.is.lab1.realtime.DataChangedEvent.Subject.LOCATIONS;
import static ru.itmo.is.lab1.realtime.DataChangedEvent.Subject.MOVIES;
import static ru.itmo.is.lab1.realtime.DataChangedEvent.Subject.PERSONS;

/** Создание, просмотр, изменение и удаление фильмов. Каждый метод выполняется в транзакции. */
@ApplicationScoped
@Transactional
public class MovieService {

    @Inject
    private MovieRepository movies;

    @Inject
    private CoordinatesRepository coordinates;

    @Inject
    private RelatedObjects related;

    @Inject
    private EntityValidator validator;

    @Inject
    private Event<DataChangedEvent> changes;

    public List<Movie> find(MovieQuery query) {
        return movies.find(query);
    }

    public long count(Map<MovieField, String> filters) {
        return movies.count(filters);
    }

    public Optional<Movie> findById(Long id) {
        return movies.findById(id);
    }

    /** Фильм и другие фильмы, использующие те же координаты и тех же людей. */
    public Optional<MovieDetails> findDetails(Long id) {
        return movies.findById(id).map(movie -> new MovieDetails(
                movie,
                withoutSelf(movies.findByCoordinates(movie.getCoordinates().getId()), movie),
                moviesWith(movie.getDirector(), movie),
                moviesWith(movie.getScreenwriter(), movie),
                moviesWith(movie.getOperator(), movie)));
    }

    public Movie create(MovieDraft draft) {
        Movie created = DataAccess.call(() -> {
            Movie movie = new Movie();
            applyAttributes(draft.getMovie(), movie);
            movie.setCoordinates(resolveCoordinates(draft.getCoordinates(), null));
            movie.setDirector(related.resolvePerson(draft.getDirector(), true, "Режиссёр"));
            movie.setScreenwriter(related.resolvePerson(draft.getScreenwriter(), true, "Сценарист"));
            movie.setOperator(related.resolvePerson(draft.getOperator(), false, "Оператор"));
            validator.check(movie, "creationDate");
            movies.save(movie);
            movies.flush();
            return movie;
        });
        changes.fire(DataChangedEvent.of(MOVIES, COORDINATES, PERSONS, LOCATIONS));
        return created;
    }

    public Movie update(Long id, MovieDraft draft) {
        Movie updated = DataAccess.call(() -> {
            Movie movie = movies.findById(id).orElseThrow(() -> NotFoundException.movie(id));
            applyAttributes(draft.getMovie(), movie);
            movie.setCoordinates(resolveCoordinates(draft.getCoordinates(), movie.getCoordinates()));
            movie.setDirector(related.resolvePerson(draft.getDirector(), true, "Режиссёр"));
            movie.setScreenwriter(related.resolvePerson(draft.getScreenwriter(), true, "Сценарист"));
            movie.setOperator(related.resolvePerson(draft.getOperator(), false, "Оператор"));
            validator.check(movie);
            movies.flush();
            return movie;
        });
        changes.fire(DataChangedEvent.of(MOVIES, COORDINATES, PERSONS, LOCATIONS));
        return updated;
    }

    /** На фильм не ссылаются другие объекты, поэтому его удаление не требует проверок связей. */
    public void delete(Long id) {
        DataAccess.run(() -> {
            Movie movie = movies.findById(id).orElseThrow(() -> NotFoundException.movie(id));
            movies.remove(movie);
            movies.flush();
        });
        changes.fire(DataChangedEvent.of(MOVIES));
    }

    private static void applyAttributes(Movie source, Movie target) {
        target.setName(source.getName() == null ? null : source.getName().strip());
        target.setOscarsCount(source.getOscarsCount());
        target.setBudget(source.getBudget());
        target.setTotalBoxOffice(source.getTotalBoxOffice());
        target.setMpaaRating(source.getMpaaRating());
        target.setLength(source.getLength());
        target.setGoldenPalmCount(source.getGoldenPalmCount());
        target.setUsaBoxOffice(source.getUsaBoxOffice());
        target.setTagline(Texts.trimToNull(source.getTagline()));
        target.setGenre(source.getGenre());
    }

    private Coordinates resolveCoordinates(CoordinatesChoice choice, Coordinates current) {
        if (choice == null || choice.getMode() == null) {
            throw new BusinessException("Не указано, какие координаты использовать");
        }
        return switch (choice.getMode()) {
            case CURRENT -> {
                if (current == null) {
                    throw new BusinessException("У фильма ещё нет координат — выберите существующие или создайте новые");
                }
                applyCoordinates(choice, current);
                yield current;
            }
            case EXISTING -> {
                if (choice.getExistingId() == null) {
                    throw new BusinessException("Выберите существующие координаты");
                }
                yield coordinates.findById(choice.getExistingId())
                        .orElseThrow(() -> NotFoundException.coordinates(choice.getExistingId()));
            }
            case NEW -> {
                Coordinates created = new Coordinates();
                applyCoordinates(choice, created);
                coordinates.save(created);
                yield created;
            }
            case NONE -> throw new BusinessException("Координаты обязательны");
        };
    }

    private void applyCoordinates(CoordinatesChoice choice, Coordinates target) {
        if (choice.getX() == null || choice.getY() == null) {
            throw new BusinessException("Укажите обе координаты: X и Y");
        }
        if (!Double.isFinite(choice.getX())) {
            throw new BusinessException("Координата X должна быть конечным числом");
        }
        target.setX(choice.getX());
        target.setY(choice.getY());
        validator.check(target);
    }

    private List<Movie> moviesWith(Person person, Movie self) {
        return person == null ? List.of() : withoutSelf(movies.findByPerson(person.getId()), self);
    }

    private static List<Movie> withoutSelf(List<Movie> list, Movie self) {
        return list.stream().filter(other -> !other.getId().equals(self.getId())).toList();
    }
}
