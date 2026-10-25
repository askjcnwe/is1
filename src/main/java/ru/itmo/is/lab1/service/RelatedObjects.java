package ru.itmo.is.lab1.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import ru.itmo.is.lab1.model.Location;
import ru.itmo.is.lab1.model.Person;
import ru.itmo.is.lab1.repository.LocationRepository;
import ru.itmo.is.lab1.repository.PersonRepository;
import ru.itmo.is.lab1.service.dto.LocationChoice;
import ru.itmo.is.lab1.service.dto.PersonChoice;
import ru.itmo.is.lab1.service.exception.BusinessException;
import ru.itmo.is.lab1.service.exception.NotFoundException;

/**
 * Связывание с людьми и местами: найти существующий объект или создать новый.
 * Вызывается из транзакций сервисов фильмов и людей.
 */
@ApplicationScoped
public class RelatedObjects {

    @Inject
    private PersonRepository persons;

    @Inject
    private LocationRepository locations;

    @Inject
    private EntityValidator validator;

    /**
     * @param role название роли для сообщений («Режиссёр», «Оператор»…)
     * @return человек или null, если роль необязательна и не указана
     */
    public Person resolvePerson(PersonChoice choice, boolean optional, String role) {
        if (choice == null || choice.getMode() == null) {
            throw new BusinessException(role + ": не указано, кого выбрать");
        }
        return switch (choice.getMode()) {
            case NONE -> {
                if (!optional) {
                    throw new BusinessException(role + " обязателен");
                }
                yield null;
            }
            case EXISTING -> {
                if (choice.getExistingId() == null) {
                    throw new BusinessException(role + ": выберите человека из списка");
                }
                yield persons.findById(choice.getExistingId())
                        .orElseThrow(() -> NotFoundException.person(choice.getExistingId()));
            }
            case NEW -> {
                Person created = new Person();
                applyAttributes(choice.getPerson(), created);
                created.setLocation(resolveLocation(choice.getLocation()));
                validator.check(created);
                persons.save(created);
                yield created;
            }
            case CURRENT -> throw new BusinessException(role + ": изменить человека можно на странице «Люди»");
        };
    }

    /** @return место или null, если оно не указано */
    public Location resolveLocation(LocationChoice choice) {
        if (choice == null || choice.getMode() == null) {
            return null;
        }
        return switch (choice.getMode()) {
            case NONE -> null;
            case EXISTING -> {
                if (choice.getExistingId() == null) {
                    throw new BusinessException("Выберите место из списка");
                }
                yield locations.findById(choice.getExistingId())
                        .orElseThrow(() -> NotFoundException.location(choice.getExistingId()));
            }
            case NEW -> {
                Location values = choice.getLocation();
                Location created = new Location(values.getX(), values.getY(), Texts.trimToNull(values.getName()));
                validator.check(created);
                locations.save(created);
                yield created;
            }
            case CURRENT -> throw new BusinessException("Изменить место можно на странице «Места»");
        };
    }

    /** Копирует атрибуты человека (кроме места) из данных формы. */
    public void applyAttributes(Person source, Person target) {
        target.setName(source.getName() == null ? null : source.getName().strip());
        target.setEyeColor(source.getEyeColor());
        target.setHairColor(source.getHairColor());
        target.setBirthday(source.getBirthday());
        target.setHeight(source.getHeight());
        target.setWeight(source.getWeight());
        target.setPassportID(source.getPassportID() == null ? null : source.getPassportID().strip());
    }
}
