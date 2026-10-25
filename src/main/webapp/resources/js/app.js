/*
 * Обновление данных на странице. Каждая страница объявляет p:remoteCommand с именем refreshData,
 * который перечитывает её данные с сервера. Его вызывают:
 *  - уведомление через f:websocket, когда данные изменил любой пользователь;
 *  - диалоги после успешного сохранения или удаления.
 */
var lab1 = (function () {
    var timer = null;

    function refresh() {
        // несколько уведомлений подряд (своё действие + сообщение сервера) сливаются в один запрос
        if (timer) {
            clearTimeout(timer);
        }
        timer = setTimeout(function () {
            timer = null;
            if (typeof window.refreshData === 'function') {
                window.refreshData();
            }
        }, 200);
    }

    return {
        refresh: refresh,
        onDataChanged: function () {
            refresh();
        }
    };
})();
