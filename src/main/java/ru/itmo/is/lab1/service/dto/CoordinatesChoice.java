package ru.itmo.is.lab1.service.dto;

import ru.itmo.is.lab1.model.Coordinates;

import java.io.Serializable;
import java.util.List;

/** Выбор координат для фильма: изменить текущие, взять существующие или создать новые. */
public class CoordinatesChoice implements Serializable {

    private static final long serialVersionUID = 1L;

    private final boolean currentAvailable;
    private RelationMode mode;
    private Long existingId;
    private Double x;
    private Integer y;

    private CoordinatesChoice(boolean currentAvailable, RelationMode mode) {
        this.currentAvailable = currentAvailable;
        this.mode = mode;
    }

    public static CoordinatesChoice createNew() {
        return new CoordinatesChoice(false, RelationMode.NEW);
    }

    public static CoordinatesChoice editCurrent(Coordinates current) {
        CoordinatesChoice choice = new CoordinatesChoice(true, RelationMode.CURRENT);
        choice.existingId = current.getId();
        choice.x = current.getX();
        choice.y = current.getY();
        return choice;
    }

    public List<RelationMode> getModes() {
        return currentAvailable
                ? List.of(RelationMode.CURRENT, RelationMode.EXISTING, RelationMode.NEW)
                : List.of(RelationMode.NEW, RelationMode.EXISTING);
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

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }
}
