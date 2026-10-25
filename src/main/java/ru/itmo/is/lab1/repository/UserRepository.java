package ru.itmo.is.lab1.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import ru.itmo.is.lab1.model.AppUser;

import java.util.Locale;
import java.util.Optional;

@ApplicationScoped
public class UserRepository {

    @PersistenceContext(unitName = "moviesPU")
    private EntityManager em;

    /** Имена пользователей сравниваются без учёта регистра. */
    public Optional<AppUser> findByUsername(String username) {
        return em.createQuery("SELECT u FROM AppUser u WHERE lower(u.username) = :name", AppUser.class)
                .setParameter("name", username.toLowerCase(Locale.ROOT))
                .getResultStream()
                .findFirst();
    }

    public void save(AppUser user) {
        em.persist(user);
    }

    public void flush() {
        em.flush();
    }
}
