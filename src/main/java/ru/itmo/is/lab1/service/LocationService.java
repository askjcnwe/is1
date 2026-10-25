package ru.itmo.is.lab1.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import ru.itmo.is.lab1.model.Location;
import ru.itmo.is.lab1.model.Person;
import ru.itmo.is.lab1.realtime.DataChangedEvent;
import ru.itmo.is.lab1.repository.LocationRepository;
import ru.itmo.is.lab1.repository.PersonRepository;
import ru.itmo.is.lab1.service.dto.LocationUsage;
import ru.itmo.is.lab1.service.exception.BusinessException;
import ru.itmo.is.lab1.service.exception.NotFoundException;

import java.util.List;
import java.util.Map;

import static ru.itmo.is.lab1.realtime.DataChangedEvent.Subject.LOCATIONS;
import static ru.itmo.is.lab1.realtime.DataChangedEvent.Subject.MOVIES;
import static ru.itmo.is.lab1.realtime.DataChangedEvent.Subject.PERSONS;

@ApplicationScoped
@Transactional
public class LocationService {

    @Inject
    private LocationRepository locations;

    @Inject
    private PersonRepository persons;

    @Inject
    private EntityValidator validator;

    @Inject
    private Event<DataChangedEvent> changes;

    public List<Location> findAll() {
        return locations.findAll();
    }

    public List<LocationUsage> findAllWithUsage() {
        Map<Long, Long> counts = persons.countByLocation();
        return locations.findAll().stream()
                .map(item -> new LocationUsage(item, counts.getOrDefault(item.getId(), 0L)))
                .toList();
    }

    public List<Person> findPersons(Long locationId) {
        return persons.findByLocation(locationId);
    }

    public Location create(Integer x, Integer y, String name) {
        Location created = DataAccess.call(() -> {
            Location item = new Location(x, y, Texts.trimToNull(name));
            validator.check(item);
            locations.save(item);
            locations.flush();
            return item;
        });
        changes.fire(DataChangedEvent.of(LOCATIONS));
        return created;
    }

    public Location update(Long id, Integer x, Integer y, String name) {
        Location updated = DataAccess.call(() -> {
            Location item = locations.findById(id).orElseThrow(() -> NotFoundException.location(id));
            item.setX(x);
            item.setY(y);
            item.setName(Texts.trimToNull(name));
            validator.check(item);
            locations.flush();
            return item;
        });
        changes.fire(DataChangedEvent.of(LOCATIONS, PERSONS, MOVIES));
        return updated;
    }

    /** Удаляет место; если оно указано у кого-либо из людей, удаление отменяется. */
    public void delete(Long id) {
        DataAccess.run(() -> {
            Location item = locations.findById(id).orElseThrow(() -> NotFoundException.location(id));
            List<Person> dependents = persons.findByLocation(id);
            if (!dependents.isEmpty()) {
                throw new BusinessException("Удаление отменено: место №" + id
                        + " указано у людей: " + Usages.persons(dependents));
            }
            locations.remove(item);
            locations.flush();
        });
        changes.fire(DataChangedEvent.of(LOCATIONS));
    }
}
