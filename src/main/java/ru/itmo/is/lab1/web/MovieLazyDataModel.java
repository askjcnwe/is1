package ru.itmo.is.lab1.web;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import ru.itmo.is.lab1.model.Movie;
import ru.itmo.is.lab1.query.MovieField;
import ru.itmo.is.lab1.query.MovieQuery;
import ru.itmo.is.lab1.service.MovieService;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Модель таблицы фильмов: страница, фильтры и сортировка передаются на сервер,
 * в браузер попадают только строки текущей страницы.
 */
public class MovieLazyDataModel extends LazyDataModel<Movie> {

    private static final long serialVersionUID = 1L;

    private final MovieService service;

    public MovieLazyDataModel(MovieService service) {
        this.service = service;
    }

    @Override
    public int count(Map<String, FilterMeta> filterBy) {
        return (int) Math.min(Integer.MAX_VALUE, service.count(toFilters(filterBy)));
    }

    @Override
    public List<Movie> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
        MovieField sortField = null;
        boolean ascending = true;
        for (SortMeta meta : sortBy.values()) {
            if (meta.getOrder() == null || meta.getOrder().isUnsorted()) {
                continue;
            }
            Optional<MovieField> field = MovieField.fromProperty(meta.getField());
            if (field.isPresent()) {
                sortField = field.get();
                ascending = meta.getOrder().isAscending();
                break;
            }
        }
        return service.find(new MovieQuery(toFilters(filterBy), sortField, ascending, first, pageSize));
    }

    @Override
    public String getRowKey(Movie movie) {
        return String.valueOf(movie.getId());
    }

    @Override
    public Movie getRowData(String rowKey) {
        List<Movie> page = getWrappedData();
        if (page == null) {
            return null;
        }
        return page.stream().filter(item -> getRowKey(item).equals(rowKey)).findFirst().orElse(null);
    }

    /** Фильтровать можно только по строковым колонкам; значение сравнивается целиком (полное совпадение). */
    private static Map<MovieField, String> toFilters(Map<String, FilterMeta> filterBy) {
        Map<MovieField, String> filters = new EnumMap<>(MovieField.class);
        for (FilterMeta meta : filterBy.values()) {
            Object value = meta.getFilterValue();
            if (value == null || value.toString().isBlank()) {
                continue;
            }
            MovieField.fromProperty(meta.getField())
                    .filter(MovieField::isTextual)
                    .ifPresent(field -> filters.put(field, value.toString()));
        }
        return filters;
    }
}
