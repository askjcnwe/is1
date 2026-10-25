package ru.itmo.is.lab1.web;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.PrimeFaces;
import ru.itmo.is.lab1.model.Location;
import ru.itmo.is.lab1.model.Person;
import ru.itmo.is.lab1.service.LocationService;
import ru.itmo.is.lab1.service.PersonService;
import ru.itmo.is.lab1.service.dto.LocationChoice;
import ru.itmo.is.lab1.service.dto.PersonUsage;
import ru.itmo.is.lab1.service.exception.NotFoundException;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/** Страница «Люди»: список, создание, изменение и удаление (отменяется, если человек указан в фильмах). */
@Named("personsPage")
@ViewScoped
public class PersonsPageBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String FORM = "personDialogForm";

    @Inject
    private PersonService persons;

    @Inject
    private LocationService locations;

    private List<PersonUsage> rows = List.of();
    private List<Location> locationOptions = List.of();

    private Long editingId;
    private Person values = new Person();
    private LocationChoice location = LocationChoice.none();

    @PostConstruct
    public void reload() {
        rows = persons.findAllWithUsage();
    }

    public void prepareCreate() {
        editingId = null;
        values = new Person();
        location = LocationChoice.none();
        open();
    }

    public void prepareEdit(Long id) {
        Optional<Person> found = persons.findById(id);
        if (found.isEmpty()) {
            Messages.error(NotFoundException.person(id).getMessage());
            return;
        }
        Person person = found.get();
        editingId = id;
        values = new Person();
        values.setName(person.getName());
        values.setPassportID(person.getPassportID());
        values.setEyeColor(person.getEyeColor());
        values.setHairColor(person.getHairColor());
        values.setBirthday(person.getBirthday());
        values.setHeight(person.getHeight());
        values.setWeight(person.getWeight());
        location = LocationChoice.of(person.getLocation());
        open();
    }

    private void open() {
        locationOptions = locations.findAll();
        PrimeFaces.current().resetInputs(":" + FORM);
        PrimeFaces.current().ajax().addCallbackParam("ready", true);
    }

    public void save() {
        boolean saved = Actions.perform(FORM, () -> {
            if (editingId == null) {
                Person created = persons.create(values, location);
                Messages.info("Человек «" + created.getName() + "» добавлен, №" + created.getId());
            } else {
                persons.update(editingId, values, location);
                Messages.info("Данные человека №" + editingId + " сохранены");
            }
        });
        if (saved) {
            reload();
            PrimeFaces.current().ajax().addCallbackParam("saved", true);
        }
    }

    /** Вызывается после подтверждения; если человек указан в фильмах, сервис отменяет удаление. */
    public void delete(Long id) {
        if (Actions.perform(() -> persons.delete(id))) {
            Messages.info("Человек №" + id + " удалён");
        }
        reload();
    }

    public List<PersonUsage> getRows() {
        return rows;
    }

    public List<Location> getLocationOptions() {
        return locationOptions;
    }

    public String getDialogTitle() {
        return editingId == null ? "Новый человек" : "Изменение данных человека №" + editingId;
    }

    public Person getValues() {
        return values;
    }

    public LocationChoice getLocation() {
        return location;
    }
}
