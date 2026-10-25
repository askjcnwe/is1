package ru.itmo.is.lab1.web;

import jakarta.faces.context.FacesContext;
import ru.itmo.is.lab1.service.exception.BusinessException;

import java.util.logging.Level;
import java.util.logging.Logger;

/** Вызов бизнес-логики из обработчиков действий: ошибки показываются пользователю, а не обрывают запрос. */
final class Actions {

    private static final Logger LOG = Logger.getLogger(Actions.class.getName());

    private Actions() {
    }

    /** Ошибки выводятся общим сообщением. @return true, если действие выполнено без ошибок */
    static boolean perform(Runnable action) {
        return perform(null, action);
    }

    /**
     * @param errorTarget id формы диалогового окна, в котором показать ошибку (null — общее сообщение)
     * @return true, если действие выполнено без ошибок
     */
    static boolean perform(String errorTarget, Runnable action) {
        try {
            action.run();
            return true;
        } catch (BusinessException e) {
            Messages.error(errorTarget, e.getMessage());
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "Операция завершилась ошибкой", e);
            Messages.error(errorTarget, "Не удалось выполнить операцию: " + rootMessage(e));
        }
        FacesContext.getCurrentInstance().validationFailed();
        return false;
    }

    private static String rootMessage(Throwable error) {
        Throwable root = error;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root.getMessage() != null ? root.getMessage() : root.getClass().getSimpleName();
    }
}
