package ru.itmo.is.lab1.service.dto;

import ru.itmo.is.lab1.model.Person;

import java.io.Serializable;

/** Человек и число фильмов, в которых он указан режиссёром, сценаристом или оператором. */
public class PersonUsage implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Person person;
    private final long movies;

    public PersonUsage(Person person, long movies) {
        this.person = person;
        this.movies = movies;
    }

    public Person getPerson() {
        return person;
    }

    public long getMovies() {
        return movies;
    }
}
