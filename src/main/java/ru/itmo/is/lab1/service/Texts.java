package ru.itmo.is.lab1.service;

final class Texts {

    private Texts() {
    }

    /** Обрезает пробелы; пустую строку превращает в null. */
    static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.strip();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
