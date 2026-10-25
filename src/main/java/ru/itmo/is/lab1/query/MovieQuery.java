package ru.itmo.is.lab1.query;

import java.io.Serializable;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Страница таблицы фильмов: фильтры по полному совпадению значений строковых колонок,
 * сортировка по одной колонке и границы страницы.
 */
public final class MovieQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Map<MovieField, String> filters;
    private final MovieField sortField;
    private final boolean ascending;
    private final int first;
    private final int pageSize;

    public MovieQuery(Map<MovieField, String> filters, MovieField sortField,
                      boolean ascending, int first, int pageSize) {
        this.filters = Collections.unmodifiableMap(filters.isEmpty()
                ? new EnumMap<>(MovieField.class) : new EnumMap<>(filters));
        this.sortField = sortField;
        this.ascending = ascending;
        this.first = Math.max(first, 0);
        this.pageSize = Math.max(pageSize, 1);
    }

    public Map<MovieField, String> getFilters() {
        return filters;
    }

    /** null — сортировка по умолчанию (по id). */
    public MovieField getSortField() {
        return sortField;
    }

    public boolean isAscending() {
        return ascending;
    }

    public int getFirst() {
        return first;
    }

    public int getPageSize() {
        return pageSize;
    }
}
