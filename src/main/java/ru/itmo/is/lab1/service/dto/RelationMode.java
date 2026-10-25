package ru.itmo.is.lab1.service.dto;

/** Как фильм (или человек) связывается со вспомогательным объектом. */
public enum RelationMode {
    /** Изменить значения объекта, уже связанного с фильмом. */
    CURRENT("Изменить текущий"),
    /** Связать с объектом, который уже есть в системе. */
    EXISTING("Выбрать существующий"),
    /** Создать новый объект. */
    NEW("Создать новый"),
    /** Не связывать (только для необязательных связей). */
    NONE("Не указывать");

    private final String label;

    RelationMode(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
