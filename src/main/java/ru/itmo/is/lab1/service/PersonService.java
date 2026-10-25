package ru.itmo.is.lab1.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import ru.itmo.is.lab1.model.Movie;
import ru.itmo.is.lab1.model.Person;
import ru.itmo.is.lab1.realtime.DataChangedEvent;
import ru.itmo.is.lab1.repository.MovieRepository;
import ru.itmo.is.lab1.repository.PersonRepository;
import ru.itmo.is.lab1.service.dto.LocationChoice;
import ru.itmo.is.lab1.service.dto.PersonUsage;
import ru.itmo.is.lab1.service.exception.BusinessException;
import ru.itmo.is.lab1.service.exception.NotFoundException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ru.itmo.is.lab1.realtime.DataChangedEvent.Subject.LOCATIONS;
import static ru.itmo.is.lab1.realtime.DataChangedEvent.Subject.MOVIES;
import static ru.itmo.is.lab1.realtime.DataChangedEvent.Subject.PERSONS;

@ApplicationScoped
@Transactional
public class PersonService {

    @Inject
    private PersonRepository persons;

    @Inject
    private MovieRepository movies;

    @Inject
    private RelatedObjects related;

    @Inject
    private EntityValidator validator;

    @Inject
    private Event<DataChangedEvent> changes;

    public List<Person> findAll() {
        return persons.findAll();
    }

    public Optional<Person> findById(Long id) {
        return persons.findById(id);
    }

    public List<PersonUsage> findAllWithUsage() {
        Map<Long, Long> counts = movies.countByPerson();
        return persons.findAll().stream()
                .map(item -> new PersonUsage(item, counts.getOrDefault(item.getId(), 0L)))
                .toList();
    }

    public List<Movie> findMovies(Long personId) {
        return movies.findByPerson(personId);
    }

    /** @param values атрибуты из формы (место задаётся отдельно выбором location) */
    public Person create(Person values, LocationChoice location) {
        Person created = DataAccess.call(() -> {
            Person person = new Person();
            related.applyAttributes(values, person);
            person.setLocation(related.resolveLocation(location));
            validator.check(person);
            persons.save(person);
            persons.flush();
            return person;
        });
        changes.fire(DataChangedEvent.of(PERSONS, LOCATIONS));
        return created;
    }

    public Person update(Long id, Person values, LocationChoice location) {
        Person updated = DataAccess.call(() -> {
            Person person = persons.findById(id).orElseThrow(() -> NotFoundException.person(id));
            related.applyAttributes(values, person);
            person.setLocation(related.resolveLocation(location));
            validator.check(person);
            persons.flush();
            return person;
        });
        changes.fire(DataChangedEvent.of(PERSONS, LOCATIONS, MOVIES));
        return updated;
    }

    /** Удаляет человека; если он указан в каком-либо фильме, удаление отменяется. */
    public void delete(Long id) {
        DataAccess.run(() -> {
            Person person = persons.findById(id).orElseThrow(() -> NotFoundException.person(id));
            List<Movie> dependents = movies.findByPerson(id);
            if (!dependents.isEmpty()) {
                throw new BusinessException("Удаление отменено: «" + person.getName()
                        + "» указан в фильмах: " + Usages.movies(dependents));
            }
            persons.remove(person);
            persons.flush();
        });
        changes.fire(DataChangedEvent.of(PERSONS, LOCATIONS));
    }
}
