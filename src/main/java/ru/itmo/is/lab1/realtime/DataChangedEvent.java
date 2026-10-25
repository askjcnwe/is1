package ru.itmo.is.lab1.realtime;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/** Событие «данные изменились»; рассылается всем клиентам после успешной фиксации транзакции. */
public final class DataChangedEvent {

    public enum Subject {
        MOVIES,
        COORDINATES,
        PERSONS,
        LOCATIONS
    }

    private final Set<Subject> subjects;

    private DataChangedEvent(Set<Subject> subjects) {
        this.subjects = subjects;
    }

    public static DataChangedEvent of(Subject first, Subject... rest) {
        return new DataChangedEvent(EnumSet.of(first, rest));
    }

    public Set<Subject> getSubjects() {
        return subjects;
    }

    /** Текст сообщения для клиента, например {@code movies,persons}. */
    public String toMessage() {
        return subjects.stream()
                .map(subject -> subject.name().toLowerCase(Locale.ROOT))
                .collect(Collectors.joining(","));
    }
}
