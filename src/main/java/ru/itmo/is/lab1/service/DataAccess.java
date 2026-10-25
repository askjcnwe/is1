package ru.itmo.is.lab1.service;

import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import ru.itmo.is.lab1.service.exception.BusinessException;

import java.sql.SQLException;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** Переводит ошибки Hibernate и PostgreSQL в понятные пользователю сообщения. */
final class DataAccess {

    private static final Pattern SEVERITY_PREFIX = Pattern.compile("^(ERROR|ОШИБКА|FATAL|ВАЖНО)\\s*:\\s*");

    private DataAccess() {
    }

    static <T> T call(Supplier<T> action) {
        try {
            return action.get();
        } catch (PersistenceException | ConstraintViolationException e) {
            throw new BusinessException(describe(e), e);
        }
    }

    static void run(Runnable action) {
        call(() -> {
            action.run();
            return null;
        });
    }

    static String describe(Throwable error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException violations) {
                return violations.getConstraintViolations().stream()
                        .map(ConstraintViolation::getMessage)
                        .distinct()
                        .sorted()
                        .collect(Collectors.joining("; "));
            }
            if (cause instanceof SQLException sql && sql.getMessage() != null) {
                String firstLine = sql.getMessage().lines().findFirst().orElse("").strip();
                return SEVERITY_PREFIX.matcher(firstLine).replaceFirst("");
            }
        }
        return "Ошибка базы данных: " + error.getMessage();
    }
}
