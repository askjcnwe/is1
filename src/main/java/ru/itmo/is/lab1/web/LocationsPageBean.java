package ru.itmo.is.lab1.web;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.PrimeFaces;
import ru.itmo.is.lab1.model.Location;
import ru.itmo.is.lab1.service.LocationService;
import ru.itmo.is.lab1.service.dto.LocationUsage;
import ru.itmo.is.lab1.service.exception.NotFoundException;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/** Страница «Места»: список, создание, изменение и удаление (отменяется, если место указано у людей). */
@Named("locationsPage")
@ViewScoped
public class LocationsPageBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String FORM = "locationDialogForm";

    @Inject
    private LocationService locations;

    private List<LocationUsage> rows = List.of();

    private Long editingId;
    private Integer x;
    private Integer y;
    private String name;

    @PostConstruct
    public void reload() {
        rows = locations.findAllWithUsage();
    }

    public void prepareCreate() {
        editingId = null;
        x = null;
        y = null;
        name = null;
        PrimeFaces.current().resetInputs(":" + FORM);
    }

    public void prepareEdit(Long id) {
        reload();
        Optional<LocationUsage> row = rows.stream().filter(item -> item.getLocation().getId().equals(id)).findFirst();
        if (row.isEmpty()) {
            Messages.error(NotFoundException.location(id).getMessage());
            return;
        }
        Location location = row.get().getLocation();
        editingId = id;
        x = location.getX();
        y = location.getY();
        name = location.getName();
        PrimeFaces.current().resetInputs(":" + FORM);
        PrimeFaces.current().ajax().addCallbackParam("ready", true);
    }

    public void save() {
        boolean saved = Actions.perform(FORM, () -> {
            if (editingId == null) {
                Location created = locations.create(x, y, name);
                Messages.info("Место №" + created.getId() + " создано");
            } else {
                locations.update(editingId, x, y, name);
                Messages.info("Место №" + editingId + " изменено");
            }
        });
        if (saved) {
            reload();
            PrimeFaces.current().ajax().addCallbackParam("saved", true);
        }
    }

    /** Вызывается после подтверждения; если место указано у людей, сервис отменяет удаление. */
    public void delete(Long id) {
        if (Actions.perform(() -> locations.delete(id))) {
            Messages.info("Место №" + id + " удалено");
        }
        reload();
    }

    public List<LocationUsage> getRows() {
        return rows;
    }

    public String getDialogTitle() {
        return editingId == null ? "Новое место" : "Изменение места №" + editingId;
    }

    public Integer getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
