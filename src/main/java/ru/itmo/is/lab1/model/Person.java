package ru.itmo.is.lab1.model;

import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "person")
public class Person implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @NotBlank(message = "Имя не может быть пустым")
    @Size(max = 255, message = "Имя должно быть не длиннее 255 символов")
    @Column(name = "name", nullable = false, length = 255,
            check = @CheckConstraint(name = "person_name_not_blank", constraint = "name ~ '\\S'"))
    private String name;

    /** Может быть null. */
    @Enumerated(EnumType.STRING)
    @Column(name = "eye_color", length = 8)
    private Color eyeColor;

    @NotNull(message = "Цвет волос обязателен")
    @Enumerated(EnumType.STRING)
    @Column(name = "hair_color", nullable = false, length = 8)
    private Color hairColor;

    /** Может быть null. */
    @Valid
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "location_id")
    private Location location;

    /** Может быть null. */
    @Column(name = "birthday")
    private LocalDate birthday;

    @NotNull(message = "Рост обязателен")
    @Positive(message = "Рост должен быть больше 0")
    @Column(name = "height", nullable = false,
            check = @CheckConstraint(name = "person_height_positive", constraint = "height > 0"))
    private Integer height;

    @Positive(message = "Вес должен быть больше 0")
    @Column(name = "weight", nullable = false,
            check = @CheckConstraint(name = "person_weight_positive", constraint = "weight > 0"))
    private int weight;

    @NotBlank(message = "Номер паспорта не может быть пустым")
    @Size(max = 46, message = "Номер паспорта должен быть не длиннее 46 символов")
    @Column(name = "passport_id", nullable = false, length = 46,
            check = @CheckConstraint(name = "person_passport_id_not_blank", constraint = "passport_id ~ '\\S'"))
    private String passportID;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Color getEyeColor() {
        return eyeColor;
    }

    public void setEyeColor(Color eyeColor) {
        this.eyeColor = eyeColor;
    }

    public Color getHairColor() {
        return hairColor;
    }

    public void setHairColor(Color hairColor) {
        this.hairColor = hairColor;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getPassportID() {
        return passportID;
    }

    public void setPassportID(String passportID) {
        this.passportID = passportID;
    }

    @Override
    public String toString() {
        return name;
    }
}
