package ru.itmo.is.lab1.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import ru.itmo.is.lab1.service.exception.BusinessException;

import java.util.Set;
import java.util.stream.Collectors;

/** Проверка ограничений Bean Validation до обращения к БД. */
@ApplicationScoped
public class EntityValidator {

    @Inject
    private Validator validator;

    /**
     * @param generatedOnPersist свойства, которые заполняются автоматически при сохранении
     *                           (их проверит Hibernate непосредственно перед вставкой)
     */
    public void check(Object entity, String... generatedOnPersist) {
        Set<String> skipped = Set.of(generatedOnPersist);
        Set<ConstraintViolation<Object>> violations = validator.validate(entity);
        String message = violations.stream()
                .filter(violation -> !skipped.contains(violation.getPropertyPath().toString()))
                .map(ConstraintViolation::getMessage)
                .distinct()
                .sorted()
                .collect(Collectors.joining("; "));
        if (!message.isEmpty()) {
            throw new BusinessException(message);
        }
    }
}
