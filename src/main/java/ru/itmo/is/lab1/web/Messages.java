package ru.itmo.is.lab1.web;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

/**
 * Сообщения пользователю. Общие сообщения выводятся во всплывающих уведомлениях (p:growl),
 * сообщения, привязанные к форме диалогового окна, — внутри этого окна (p:messages for="...").
 */
public final class Messages {

    private Messages() {
    }

    public static void info(String text) {
        add(null, FacesMessage.SEVERITY_INFO, text);
    }

    public static void error(String text) {
        add(null, FacesMessage.SEVERITY_ERROR, text);
    }

    /** @param clientId id формы диалогового окна, в котором нужно показать ошибку; null — общее сообщение */
    public static void error(String clientId, String text) {
        add(clientId, FacesMessage.SEVERITY_ERROR, text);
    }

    private static void add(String clientId, FacesMessage.Severity severity, String text) {
        FacesContext.getCurrentInstance().addMessage(clientId, new FacesMessage(severity, text, null));
    }
}
