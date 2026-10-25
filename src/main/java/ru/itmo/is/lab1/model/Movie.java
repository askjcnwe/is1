package ru.itmo.is.lab1.model;

import java.io.Serializable;
import java.time.LocalDate;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Фильм. Ограничения полей заданы на трёх уровнях: Bean Validation (проверяется и в интерфейсе,
 * и Hibernate перед записью), отображение JPA ({@code nullable}, {@code check}) и ограничения
 * таблицы в БД (sql/init.sql).
 */
@Entity
@Table(name = "movie")
public class Movie implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Генерируется БД (identity), уникален, больше 0. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Positive
    @Column(name = "id", nullable = false, updatable = false,
            check = @CheckConstraint(name = "movie_id_positive", constraint = "id > 0"))
    private Long id;

    @NotBlank(message = "Название не может быть пустым")
    @Size(max = 255, message = "Название должно быть не длиннее 255 символов")
    @Column(name = "name", nullable = false, length = 255,
            check = @CheckConstraint(name = "movie_name_not_blank", constraint = "name ~ '\\S'"))
    private String name;

    @NotNull(message = "Координаты обязательны")
    @Valid
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "coordinates_id", nullable = false)
    private Coordinates coordinates;

    /** Генерируется автоматически при сохранении. */
    @NotNull
    @Column(name = "creation_date", nullable = false, updatable = false)
    private LocalDate creationDate;

    /** Не может быть null и отрицательным; 0 допустим (фильм без «Оскаров»). */
    @NotNull(message = "Число «Оскаров» обязательно")
    @PositiveOrZero(message = "Число «Оскаров» не может быть отрицательным")
    @Column(name = "oscars_count", nullable = false,
            check = @CheckConstraint(name = "movie_oscars_count_not_negative", constraint = "oscars_count >= 0"))
    private Integer oscarsCount;

    @Positive(message = "Бюджет должен быть больше 0")
    @Column(name = "budget", nullable = false,
            check = @CheckConstraint(name = "movie_budget_positive", constraint = "budget > 0"))
    private long budget;

    @NotNull(message = "Общие сборы обязательны")
    @Positive(message = "Общие сборы должны быть больше 0")
    @Column(name = "total_box_office", nullable = false,
            check = @CheckConstraint(name = "movie_total_box_office_positive", constraint = "total_box_office > 0"))
    private Long totalBoxOffice;

    @NotNull(message = "Рейтинг MPAA обязателен")
    @Enumerated(EnumType.STRING)
    @Column(name = "mpaa_rating", nullable = false, length = 8)
    private MpaaRating mpaaRating;

    /** Может быть null. */
    @Valid
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "director_id")
    private Person director;

    /** Может быть null. */
    @Valid
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "screenwriter_id")
    private Person screenwriter;

    @NotNull(message = "Оператор обязателен")
    @Valid
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "operator_id", nullable = false)
    private Person operator;

    @NotNull(message = "Длительность обязательна")
    @Positive(message = "Длительность должна быть больше 0")
    @Column(name = "length", nullable = false,
            check = @CheckConstraint(name = "movie_length_positive", constraint = "length > 0"))
    private Integer length;

    @Positive(message = "Число «Золотых пальмовых ветвей» должно быть больше 0")
    @Column(name = "golden_palm_count", nullable = false,
            check = @CheckConstraint(name = "movie_golden_palm_count_positive", constraint = "golden_palm_count > 0"))
    private int goldenPalmCount;

    @NotNull(message = "Сборы в США обязательны")
    @Positive(message = "Сборы в США должны быть больше 0")
    @Column(name = "usa_box_office", nullable = false,
            check = @CheckConstraint(name = "movie_usa_box_office_positive", constraint = "usa_box_office > 0"))
    private Float usaBoxOffice;

    /** Может быть null. */
    @Size(max = 205, message = "Слоган должен быть не длиннее 205 символов")
    @Column(name = "tagline", length = 205)
    private String tagline;

    /** Может быть null. */
    @Enumerated(EnumType.STRING)
    @Column(name = "genre", length = 16)
    private MovieGenre genre;

    @PrePersist
    void generateCreationDate() {
        creationDate = LocalDate.now();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public Integer getOscarsCount() {
        return oscarsCount;
    }

    public void setOscarsCount(Integer oscarsCount) {
        this.oscarsCount = oscarsCount;
    }

    public long getBudget() {
        return budget;
    }

    public void setBudget(long budget) {
        this.budget = budget;
    }

    public Long getTotalBoxOffice() {
        return totalBoxOffice;
    }

    public void setTotalBoxOffice(Long totalBoxOffice) {
        this.totalBoxOffice = totalBoxOffice;
    }

    public MpaaRating getMpaaRating() {
        return mpaaRating;
    }

    public void setMpaaRating(MpaaRating mpaaRating) {
        this.mpaaRating = mpaaRating;
    }

    public Person getDirector() {
        return director;
    }

    public void setDirector(Person director) {
        this.director = director;
    }

    public Person getScreenwriter() {
        return screenwriter;
    }

    public void setScreenwriter(Person screenwriter) {
        this.screenwriter = screenwriter;
    }

    public Person getOperator() {
        return operator;
    }

    public void setOperator(Person operator) {
        this.operator = operator;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public int getGoldenPalmCount() {
        return goldenPalmCount;
    }

    public void setGoldenPalmCount(int goldenPalmCount) {
        this.goldenPalmCount = goldenPalmCount;
    }

    public Float getUsaBoxOffice() {
        return usaBoxOffice;
    }

    public void setUsaBoxOffice(Float usaBoxOffice) {
        this.usaBoxOffice = usaBoxOffice;
    }

    public String getTagline() {
        return tagline;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
    }

    public MovieGenre getGenre() {
        return genre;
    }

    public void setGenre(MovieGenre genre) {
        this.genre = genre;
    }
}