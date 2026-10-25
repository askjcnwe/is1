package ru.itmo.is.lab1.service.exception;

/**
 * Ошибка, о которой нужно сообщить пользователю. Непроверяемое исключение,
 * поэтому {@code @Transactional} откатывает транзакцию, в которой оно возникло.
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
