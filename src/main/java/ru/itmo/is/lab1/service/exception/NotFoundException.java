package ru.itmo.is.lab1.service.exception;

public class NotFoundException extends BusinessException {

    private static final long serialVersionUID = 1L;

    public NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException movie(Long id) {
        return new NotFoundException("Фильм с ID " + id + " не найден (возможно, его удалил другой пользователь)");
    }

    public static NotFoundException coordinates(Long id) {
        return new NotFoundException("Координаты №" + id + " не найдены (возможно, их удалил другой пользователь)");
    }

    public static NotFoundException person(Long id) {
        return new NotFoundException("Человек №" + id + " не найден (возможно, его удалил другой пользователь)");
    }

    public static NotFoundException location(Long id) {
        return new NotFoundException("Место №" + id + " не найдено (возможно, его удалил другой пользователь)");
    }
}
