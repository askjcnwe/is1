package ru.itmo.is.lab1.service.dto;

import ru.itmo.is.lab1.model.Location;

import java.io.Serializable;
import java.util.List;

/**
 * Выбор места для человека: взять существующее, создать новое или не указывать.
 * Значения нового места хранятся в несохраняемом экземпляре {@link Location},
 * поэтому поля формы проверяются ограничениями сущности.
 */
public class LocationChoice implements Serializable {

    private static final long serialVersionUID = 1L;

    private RelationMode mode;
    private Long existingId;
    private final Location location = new Location();

    private LocationChoice(RelationMode mode) {
        this.mode = mode;
    }

    public static LocationChoice none() {
        return new LocationChoice(RelationMode.NONE);
    }

    /** current может быть null, если место не указано. */
    public static LocationChoice of(Location current) {
        if (current == null) {
            return none();
        }
        LocationChoice choice = new LocationChoice(RelationMode.EXISTING);
        choice.existingId = current.getId();
        return choice;
    }

    public List<RelationMode> getModes() {
        return List.of(RelationMode.NONE, RelationMode.EXISTING, RelationMode.NEW);
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

    public Location getLocation() {
        return location;
    }
}
