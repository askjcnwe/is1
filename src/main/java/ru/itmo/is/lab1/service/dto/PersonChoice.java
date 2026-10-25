package ru.itmo.is.lab1.service.dto;

import ru.itmo.is.lab1.model.Person;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Выбор человека для роли в фильме (режиссёр, сценарист, оператор): взять существующего,
 * создать нового или не указывать. Атрибуты нового человека хранятся в несохраняемом экземпляре
 * {@link Person}, поэтому поля формы проверяются ограничениями сущности.
 */
public class PersonChoice implements Serializable {

    private static final long serialVersionUID = 1L;

    private final boolean optional;
    private RelationMode mode;
    private Long existingId;
    private final Person person = new Person();
    private final LocationChoice location = LocationChoice.none();

    private PersonChoice(boolean optional, RelationMode mode) {
        this.optional = optional;
        this.mode = mode;
    }

    /** Для нового фильма: необязательная роль по умолчанию не указывается, обязательная — создаётся. */
    public static PersonChoice createNew(boolean optional) {
        return new PersonChoice(optional, optional ? RelationMode.NONE : RelationMode.NEW);
    }

    /** Для изменения фильма; current может быть null, если роль не была указана. */
    public static PersonChoice of(Person current, boolean optional) {
        if (current == null) {
            return createNew(optional);
        }
        PersonChoice choice = new PersonChoice(optional, RelationMode.EXISTING);
        choice.existingId = current.getId();
        return choice;
    }

    public List<RelationMode> getModes() {
        List<RelationMode> modes = new ArrayList<>(List.of(RelationMode.EXISTING, RelationMode.NEW));
        if (optional) {
            modes.add(RelationMode.NONE);
        }
        return modes;
    }

    public boolean isOptional() {
        return optional;
    }

    public RelationMode getMode() {
        return mode;
    }

    public void setMode(RelationMode mode) {
        this.mode = mode;
    }

    public Long getExistingId() {
        return existingId;
    }

    public void setExistingId(Long existingId) {
        this.existingId = existingId;
    }

    public Person getPerson() {
        return person;
    }

    public LocationChoice getLocation() {
        return location;
    }
}
