package ru.itmo.is.lab1.service.dto;

import ru.itmo.is.lab1.model.Location;

import java.io.Serializable;

/** Место и число людей, у которых оно указано. */
public class LocationUsage implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Location location;
    private final long persons;

    public LocationUsage(Location location, long persons) {
        this.location = location;
        this.persons = persons;
    }

    public Location getLocation() {
        return location;
    }

    public long getPersons() {
        return persons;
    }
}
