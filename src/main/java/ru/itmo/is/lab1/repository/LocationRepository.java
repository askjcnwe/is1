package ru.itmo.is.lab1.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import ru.itmo.is.lab1.model.Location;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class LocationRepository {

    @PersistenceContext(unitName = "moviesPU")
    private EntityManager em;

    public Optional<Location> findById(Long id) {
        return Optional.ofNullable(em.find(Location.class, id));
    }

    public List<Location> findAll() {
        return em.createQuery("SELECT l FROM Location l ORDER BY l.id", Location.class).getResultList();
    }

    public void save(Location location) {
        em.persist(location);
    }

    public void remove(Location location) {
        em.remove(location);
    }

    public void flush() {
        em.flush();
    }
}
