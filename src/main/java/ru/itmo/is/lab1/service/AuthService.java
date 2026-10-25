package ru.itmo.is.lab1.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import ru.itmo.is.lab1.model.AppUser;
import ru.itmo.is.lab1.repository.UserRepository;
import ru.itmo.is.lab1.service.exception.BusinessException;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/** Регистрация и проверка учётных данных пользователей. */
@ApplicationScoped
@Transactional
public class AuthService {

    private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9_.-]{3,50}");
    private static final int MIN_PASSWORD = 6;
    private static final int MAX_PASSWORD = 100;

    /** Хеш-заглушка: проверка выполняется и для несуществующего имени, чтобы время ответа не выдавало, есть ли оно. */
    private static final String DUMMY_HASH = PasswordHasher.hash(UUID.randomUUID().toString());

    @Inject
    private UserRepository users;

    @Inject
    private EntityValidator validator;

    public AppUser register(String username, String password, String confirmation) {
        String name = Texts.trimToNull(username);
        if (name == null) {
            throw new BusinessException("Введите имя пользователя");
        }
        if (!USERNAME.matcher(name).matches()) {
            throw new BusinessException(
                    "Имя пользователя: от 3 до 50 символов, только латинские буквы, цифры и знаки _ . -");
        }
        if (password == null || password.length() < MIN_PASSWORD || password.length() > MAX_PASSWORD) {
            throw new BusinessException("Пароль должен содержать от " + MIN_PASSWORD + " до " + MAX_PASSWORD + " символов");
        }
        if (!password.equals(confirmation)) {
            throw new BusinessException("Пароли не совпадают");
        }
        return DataAccess.call(() -> {
            if (users.findByUsername(name).isPresent()) {
                throw new BusinessException("Имя «" + name + "» уже занято");
            }
            AppUser user = new AppUser();
            user.setUsername(name);
            user.setPasswordHash(PasswordHasher.hash(password));
            validator.check(user);
            users.save(user);
            users.flush();
            return user;
        });
    }

    public AppUser authenticate(String username, String password) {
        String name = Texts.trimToNull(username);
        if (name == null || password == null || password.isEmpty()) {
            throw new BusinessException("Введите имя пользователя и пароль");
        }
        Optional<AppUser> found = DataAccess.call(() -> users.findByUsername(name));
        boolean passwordMatches = PasswordHasher.matches(password,
                found.map(AppUser::getPasswordHash).orElse(DUMMY_HASH));
        if (found.isEmpty() || !passwordMatches) {
            throw new BusinessException("Неверное имя пользователя или пароль");
        }
        return found.get();
    }
}
