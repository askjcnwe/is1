package ru.itmo.is.lab1.web.converter;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Дробное число типа float для полей, которые должны быть больше 0: принимает точку и запятую,
 * не принимает экспоненту, NaN, бесконечность и числа вне диапазона float. Значение null или 0
 * показывается пустым полем; проверка «больше 0» выполняется ограничением @Positive сущности.
 */
@FacesConverter("positiveFloat")
public class PositiveFloatConverter implements Converter<Float> {

    private static final Pattern DECIMAL = Pattern.compile("[+-]?(\\d+(\\.\\d*)?|\\.\\d+)");

    @Override
    public Float getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.strip().replace(',', '.');
        if (DECIMAL.matcher(normalized).matches()) {
            double parsed = Double.parseDouble(normalized);
            if (Double.isFinite(parsed) && Math.abs(parsed) <= Float.MAX_VALUE) {
                return (float) parsed;
            }
        }
        throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Введите число, например 1250.75", null));
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Float value) {
        if (value == null || value <= 0) {
            return "";
        }
        return new BigDecimal(Float.toString(value)).stripTrailingZeros().toPlainString();
    }
}
