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
 * Дробное число: принимает и точку, и запятую, не принимает экспоненту, NaN и бесконечность;
 * выводит число без экспоненциальной записи.
 */
@FacesConverter("decimal")
public class DecimalConverter implements Converter<Double> {

    private static final Pattern DECIMAL = Pattern.compile("[+-]?(\\d+(\\.\\d*)?|\\.\\d+)");

    @Override
    public Double getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.strip().replace(',', '.');
        if (DECIMAL.matcher(normalized).matches()) {
            double parsed = Double.parseDouble(normalized);
            if (Double.isFinite(parsed)) {
                return parsed;
            }
        }
        throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Введите число, например 1250.75", null));
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Double value) {
        return value == null ? "" : BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }
}
