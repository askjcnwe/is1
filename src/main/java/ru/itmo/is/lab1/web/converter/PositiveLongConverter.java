package ru.itmo.is.lab1.web.converter;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;

import java.util.regex.Pattern;

/**
 * Целое число (long) для полей, которые должны быть больше 0. Значение 0 (у нового фильма)
 * показывается пустым полем; сама проверка «больше 0» выполняется ограничением @Positive сущности.
 */
@FacesConverter("positiveLong")
public class PositiveLongConverter implements Converter<Long> {

    private static final Pattern INTEGER = Pattern.compile("[+-]?\\d+");

    @Override
    public Long getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.strip();
        if (INTEGER.matcher(normalized).matches()) {
            try {
                return Long.valueOf(normalized);
            } catch (NumberFormatException tooLarge) {
                // обработано ниже
            }
        }
        throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Введите целое число от 1 до " + Long.MAX_VALUE, null));
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Long value) {
        return value == null || value <= 0 ? "" : value.toString();
    }
}
