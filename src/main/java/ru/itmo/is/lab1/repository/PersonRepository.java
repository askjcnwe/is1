package ru.itmo.is.lab1.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import ru.itmo.is.lab1.model.Person;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class PersonRepository {

    @PersistenceContext(unitName = "moviesPU")
    private EntityManager em;

    public Optional<Person> findById(Long id) {
        return Optional.ofNullable(em.find(Person.class, id));
    }

    public List<Person> findAll() {
        return em.createQuery("SELECT p FROM Person p ORDER BY p.id", Person.class).getResultList();
    }

    public List<Person> findByLocation(Long locationId) {
        return em.createQuery("SELECT p FROM Person p WHERE p.location.id = :id ORDER BY p.id", Person.class)
                .setParameter("id", locationId)
                .getResultList();
    }

    /** id места → число людей, у которых оно указано. */
    public Map<Long, Long> countByLocation() {
        Map<Long, Long> counts = new HashMap<>();
        List<Object[]> rows = em.createQuery(
                "SELECT l.id, COUNT(p) FROM Person p JOIN p.location l GROUP BY l.id", Object[].class).getResultList();
        for (Object[] row : rows) {
            counts.put((Long) row[0], (Long) row[1]);
        }
        return counts;
    }

    public void save(Person person) {
        em.persist(person);
    }

    public void remove(Person person) {
        em.remove(person);
    }

    public void flush() {
        em.flush();
    }
}
