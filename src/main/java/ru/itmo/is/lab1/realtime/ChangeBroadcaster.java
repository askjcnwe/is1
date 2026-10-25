package ru.itmo.is.lab1.realtime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.faces.push.Push;
import jakarta.faces.push.PushContext;
import jakarta.inject.Inject;

/**
 * Рассылает уведомления об изменениях всем открытым страницам через WebSocket
 * ({@code <f:websocket channel="changes">} в шаблоне). Получив сообщение,
 * страница перезагружает свои данные с сервера.
 */
@ApplicationScoped
public class ChangeBroadcaster {

    public static final String CHANNEL = "changes";

    @Inject
    @Push(channel = CHANNEL)
    private PushContext changes;

    public void onDataChanged(@Observes(during = TransactionPhase.AFTER_SUCCESS) DataChangedEvent event) {
        changes.send(event.toMessage());
    }
}
