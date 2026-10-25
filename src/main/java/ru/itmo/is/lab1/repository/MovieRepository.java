package ru.itmo.is.lab1.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import ru.itmo.is.lab1.model.Coordinates;
import ru.itmo.is.lab1.model.Movie;
import ru.itmo.is.lab1.model.MovieGenre;
import ru.itmo.is.lab1.model.MpaaRating;
import ru.itmo.is.lab1.model.Person;
import ru.itmo.is.lab1.query.MovieField;
import ru.itmo.is.lab1.query.MovieQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class MovieRepository {

    @PersistenceContext(unitName = "moviesPU")
    private EntityManager em;

    public Optional<Movie> findById(Long id) {
        return Optional.ofNullable(em.find(Movie.class, id));
    }

    public void save(Movie movie) {
        em.persist(movie);
    }

    public void remove(Movie movie) {
        em.remove(movie);
    }

    public void flush() {
        em.flush();
    }

    /** Страница таблицы: фильтрация, сортировка и пагинация выполняются в БД. */
    public List<Movie> find(MovieQuery query) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Movie> cq = cb.createQuery(Movie.class);
        Root<Movie> root = cq.from(Movie.class);
        Paths paths = new Paths(root);

        List<Order> order = new ArrayList<>();
        if (query.getSortField() != null) {
            Expression<?> sortBy = paths.get(query.getSortField());
            order.add(query.isAscending() ? cb.asc(sortBy) : cb.desc(sortBy));
        }
        order.add(cb.asc(root.get("id")));

        cq.select(root)
                .where(filters(cb, paths, query.getFilters()))
                .orderBy(order);
        return em.createQuery(cq)
                .setFirstResult(query.getFirst())
                .setMaxResults(query.getPageSize())
                .getResultList();
    }

    public long count(Map<MovieField, String> filters) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Movie> root = cq.from(Movie.class);
        cq.select(cb.count(root)).where(filters(cb, new Paths(root), filters));
        return em.createQuery(cq).getSingleResult();
    }

    public List<Movie> findByCoordinates(Long coordinatesId) {
        return em.createQuery("SELECT m FROM Movie m WHERE m.coordinates.id = :id ORDER BY m.id", Movie.class)
                .setParameter("id", coordinatesId)
                .getResultList();
    }

    /** Фильмы, в которых человек указан режиссёром, сценаристом или оператором. */
    public List<Movie> findByPerson(Long personId) {
        return em.createQuery("""
                        SELECT m FROM Movie m
                        LEFT JOIN m.director d
                        LEFT JOIN m.screenwriter s
                        WHERE d.id = :id OR s.id = :id OR m.operator.id = :id
                        ORDER BY m.id""", Movie.class)
                .setParameter("id", personId)
                .getResultList();
    }

    /** id координат → число фильмов, которые на них ссылаются. */
    public Map<Long, Long> countByCoordinates() {
        return toCountMap("SELECT m.coordinates.id, COUNT(m) FROM Movie m GROUP BY m.coordinates.id");
    }

    /** id человека → число фильмов, в которых он указан в любой роли. */
    public Map<Long, Long> countByPerson() {
        return toCountMap("""
                SELECT p.id, COUNT(DISTINCT m.id) FROM Movie m, Person p
                WHERE m.director = p OR m.screenwriter = p OR m.operator = p
                GROUP BY p.id""");
    }

    private Map<Long, Long> toCountMap(String jpql) {
        Map<Long, Long> counts = new HashMap<>();
        for (Object[] row : em.createQuery(jpql, Object[].class).getResultList()) {
            counts.put((Long) row[0], (Long) row[1]);
        }
        return counts;
    }

    /** Фильтрация по полному совпадению значения колонки. */
    private Predicate[] filters(CriteriaBuilder cb, Paths paths, Map<MovieField, String> filters) {
        List<Predicate> predicates = new ArrayList<>();
        filters.forEach((field, value) -> {
            String expected = value.strip();
            switch (field) {
                case MPAA_RATING -> predicates.add(Arrays.stream(MpaaRating.values())
                        .filter(rating -> rating.name().equals(expected))
                        .findFirst()
                        .map(rating -> cb.equal(paths.root.get("mpaaRating"), rating))
                        .orElseGet(cb::disjunction));
                case GENRE -> predicates.add(Arrays.stream(MovieGenre.values())
                        .filter(genre -> genre.name().equals(expected))
                        .findFirst()
                        .map(genre -> cb.equal(paths.root.get("genre"), genre))
                        .orElseGet(cb::disjunction));
                default -> predicates.add(cb.equal(paths.get(field), expected));
            }
        });
        return predicates.toArray(Predicate[]::new);
    }

    /** Пути к колонкам таблицы; соединения создаются только для используемых колонок. */
    private static final class Paths {

        private final Root<Movie> root;
        private Join<Movie, Coordinates> coordinates;
        private Join<Movie, Person> director;
        private Join<Movie, Person> screenwriter;
        private Join<Movie, Person> operator;

        private Paths(Root<Movie> root) {
            this.root = root;
        }

        Expression<?> get(MovieField field) {
            return switch (field) {
                case ID -> root.get("id");
                case NAME -> root.get("name");
                case COORDINATES_X -> coordinates().get("x");
                case COORDINATES_Y -> coordinates().get("y");
                case CREATION_DATE -> root.get("creationDate");
                case OSCARS_COUNT -> root.get("oscarsCount");
                case BUDGET -> root.get("budget");
                case TOTAL_BOX_OFFICE -> root.get("totalBoxOffice");
                case MPAA_RATING -> root.get("mpaaRating");
                case DIRECTOR -> director().get("name");
                case SCREENWRITER -> screenwriter().get("name");
                case OPERATOR -> operator().get("name");
                case LENGTH -> root.get("length");
                case GOLDEN_PALM_COUNT -> root.get("goldenPalmCount");
                case USA_BOX_OFFICE -> root.get("usaBoxOffice");
                case TAGLINE -> root.get("tagline");
                case GENRE -> root.get("genre");
            };
        }

        private Join<Movie, Coordinates> coordinates() {
            if (coordinates == null) {
                coordinates = root.join("coordinates", JoinType.INNER);
            }
            return coordinates;
        }

        private Join<Movie, Person> director() {
            if (director == null) {
                director = root.join("director", JoinType.LEFT);
            }
            return director;
        }

        private Join<Movie, Person> screenwriter() {
            if (screenwriter == null) {
                screenwriter = root.join("screenwriter", JoinType.LEFT);
            }
            return screenwriter;
        }

        private Join<Movie, Person> operator() {
            if (operator == null) {
                operator = root.join("operator", JoinType.INNER);
            }
            return operator;
        }
    }
}
