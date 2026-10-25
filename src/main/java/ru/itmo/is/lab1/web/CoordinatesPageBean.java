package ru.itmo.is.lab1.web;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.PrimeFaces;
import ru.itmo.is.lab1.model.Coordinates;
import ru.itmo.is.lab1.service.CoordinatesService;
import ru.itmo.is.lab1.service.dto.CoordinatesUsage;
import ru.itmo.is.lab1.service.exception.NotFoundException;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/** Страница «Координаты»: список, создание, изменение и удаление (отменяется, если координаты используются). */
@Named("coordinatesPage")
@ViewScoped
public class CoordinatesPageBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String FORM = "coordinatesDialogForm";

    @Inject
    private CoordinatesService coordinates;

    private List<CoordinatesUsage> rows = List.of();

    private Long editingId;
    private Double x;
    private Integer y;

    @PostConstruct
    public void reload() {
        rows = coordinates.findAllWithUsage();
    }

    public void prepareCreate() {
        editingId = null;
        x = null;
        y = null;
        PrimeFaces.current().resetInputs(":" + FORM);
    }

    public void prepareEdit(Long id) {
        reload();
        Optional<CoordinatesUsage> row = rows.stream().filter(item -> item.getCoordinates().getId().equals(id)).findFirst();
        if (row.isEmpty()) {
            Messages.error(NotFoundException.coordinates(id).getMessage());
            return;
        }
        editingId = id;
        x = row.get().getCoordinates().getX();
        y = row.get().getCoordinates().getY();
        PrimeFaces.current().resetInputs(":" + FORM);
        PrimeFaces.current().ajax().addCallbackParam("ready", true);
    }

    public void save() {
        boolean saved = Actions.perform(FORM, () -> {
            if (editingId == null) {
                Coordinates created = coordinates.create(x, y);
                Messages.info("Координаты №" + created.getId() + " созданы");
            } else {
                coordinates.update(editingId, x, y);
                Messages.info("Координаты №" + editingId + " изменены");
            }
        });
        if (saved) {
            reload();
            PrimeFaces.current().ajax().addCallbackParam("saved", true);
        }
    }

    /** Вызывается после подтверждения; если координаты используются фильмами, сервис отменяет удаление. */
    public void delete(Long id) {
        if (Actions.perform(() -> coordinates.delete(id))) {
            Messages.info("Координаты №" + id + " удалены");
        }
        reload();
    }

    public List<CoordinatesUsage> getRows() {
        return rows;
    }

    public String getDialogTitle() {
        return editingId == null ? "Новые координаты" : "Изменение координат №" + editingId;
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }
}
