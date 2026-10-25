package ru.itmo.is.lab1.service;

import ru.itmo.is.lab1.model.Movie;
import ru.itmo.is.lab1.model.Person;

import java.util.List;
import java.util.stream.Collectors;

/** Тексты сообщений об отказе в удалении: перечень объектов, которые ссылаются на удаляемый. */
final class Usages {

    private static final int SHOWN = 5;

    private Usages() {
    }

    static String movies(List<Movie> movies) {
        return describe(movies.stream().map(movie -> "ID " + movie.getId() + " «" + movie.getName() + "»").toList());
    }

    static String persons(List<Person> persons) {
        return describe(persons.stream().map(person -> "№" + person.getId() + " «" + person.getName() + "»").toList());
    }

    private static String describe(List<String> items) {
        String shown = items.stream().limit(SHOWN).collect(Collectors.joining(", "));
        return items.size() > SHOWN ? shown + " и ещё " + (items.size() - SHOWN) : shown;
    }
}
