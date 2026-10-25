package ru.itmo.is.lab1.web;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import ru.itmo.is.lab1.model.AppUser;
import ru.itmo.is.lab1.service.AuthService;

/** Страницы входа и регистрации и кнопка выхода. */
@Named("auth")
@RequestScoped
public class AuthBean {

    private static final String HOME = "/index?faces-redirect=true";
    private static final String LOGIN = "/login?faces-redirect=true";

    @Inject
    private AuthService auth;

    private String username;
    private String password;
    private String confirmation;

    public String login() {
        AppUser[] user = new AppUser[1];
        if (!Actions.perform(() -> user[0] = auth.authenticate(username, password))) {
            return null;
        }
        startSession(user[0]);
        return HOME;
    }

    /** После успешной регистрации пользователь сразу входит в систему. */
    public String register() {
        AppUser[] user = new AppUser[1];
        if (!Actions.perform(() -> user[0] = auth.register(username, password, confirmation))) {
            return null;
        }
        startSession(user[0]);
        return HOME;
    }

    public String logout() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return LOGIN;
    }

    /** Имя вошедшего пользователя (для шапки приложения). */
    public String getCurrentUser() {
        return (String) FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().get(AuthFilter.USER_ATTRIBUTE);
    }

    private static void startSession(AppUser user) {
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        context.getSession(true);
        // новый идентификатор сессии после входа защищает от подмены сессии (session fixation)
        ((HttpServletRequest) context.getRequest()).changeSessionId();
        context.getSessionMap().put(AuthFilter.USER_ATTRIBUTE, user.getUsername());
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmation() {
        return confirmation;
    }

    public void setConfirmation(String confirmation) {
        this.confirmation = confirmation;
    }
}
