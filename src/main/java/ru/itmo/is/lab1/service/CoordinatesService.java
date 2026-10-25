package ru.itmo.is.lab1.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import ru.itmo.is.lab1.model.Coordinates;
import ru.itmo.is.lab1.model.Movie;
import ru.itmo.is.lab1.realtime.DataChangedEvent;
import ru.itmo.is.lab1.repository.CoordinatesRepository;
import ru.itmo.is.lab1.repository.MovieRepository;
import ru.itmo.is.lab1.service.dto.CoordinatesUsage;
import ru.itmo.is.lab1.service.exception.BusinessException;
import ru.itmo.is.lab1.service.exception.NotFoundException;

import java.util.List;
import java.util.Map;

import static ru.itmo.is.lab1.realtime.DataChangedEvent.Subject.COORDINATES;
import static ru.itmo.is.lab1.realtime.DataChangedEvent.Subject.MOVIES;

@ApplicationScoped
@Transactional
public class CoordinatesService {

    @Inject
    private CoordinatesRepository coordinates;

    @Inject
    private MovieRepository movies;

    @Inject
    private EntityValidator validator;

    @Inject
    private Event<DataChangedEvent> changes;

    public List<Coordinates> findAll() {
        return coordinates.findAll();
    }

    public List<CoordinatesUsage> findAllWithUsage() {
        Map<Long, Long> counts = movies.countByCoordinates();
        return coordinates.findAll().stream()
                .map(item -> new CoordinatesUsage(item, counts.getOrDefault(item.getId(), 0L)))
                .toList();
    }

    public List<Movie> findMovies(Long coordinatesId) {
        return movies.findByCoordinates(coordinatesId);
    }

    public Coordinates create(Double x, Integer y) {
        Coordinates created = DataAccess.call(() -> {
            Coordinates item = new Coordinates();
            apply(item, x, y);
            coordinates.save(item);
            coordinates.flush();
            return item;
        });
        changes.fire(DataChangedEvent.of(COORDINATES));
        return created;
    }

    public Coordinates update(Long id, Double x, Integer y) {
        Coordinates updated = DataAccess.call(() -> {
            Coordinates item = coordinates.findById(id).orElseThrow(() -> NotFoundException.coordinates(id));
            apply(item, x, y);
            coordinates.flush();
            return item;
        });
        changes.fire(DataChangedEvent.of(COORDINATES, MOVIES));
        return updated;
    }

    /** Удаляет координаты; если на них ссылаются фильмы, удаление отменяется. */
    public void delete(Long id) {
        DataAccess.run(() -> {
            Coordinates item = coordinates.findById(id).orElseThrow(() -> NotFoundException.coordinates(id));
            List<Movie> dependents = movies.findByCoordinates(id);
            if (!dependents.isEmpty()) {
                throw new BusinessException("Удаление отменено: координаты №" + id
                        + " используются фильмами: " + Usages.movies(dependents));
            }
            coordinates.remove(item);
            coordinates.flush();
        });
        changes.fire(DataChangedEvent.of(COORDINATES));
    }

    private void apply(Coordinates item, Double x, Integer y) {
        if (x == null || y == null) {
            throw new BusinessException("Укажите обе координаты: X и Y");
        }
        if (!Double.isFinite(x)) {
            throw new BusinessException("Координата X должна быть конечным числом");
        }
        item.setX(x);
        item.setY(y);
        validator.check(item);
    }
}
