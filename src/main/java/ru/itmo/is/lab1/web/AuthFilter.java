package ru.itmo.is.lab1.web;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Set;

/**
 * Пускает к приложению только авторизованных пользователей. Без входа доступны страницы входа
 * и регистрации и ресурсы (стили, скрипты); остальные адреса перенаправляют на страницу входа.
 */
@WebFilter(urlPatterns = "/*")
public class AuthFilter implements Filter {

    /** Атрибут сессии с именем вошедшего пользователя. */
    public static final String USER_ATTRIBUTE = "lab1.user";

    private static final String LOGIN_PAGE = "/login.xhtml";
    private static final String HOME_PAGE = "/index.xhtml";
    private static final Set<String> PUBLIC_PAGES = Set.of(LOGIN_PAGE, "/register.xhtml");
    private static final String RESOURCES = "/jakarta.faces.resource/";

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String path = request.getServletPath();

        if (path.startsWith(RESOURCES)) {
            chain.doFilter(req, res);
            return;
        }
        // после выхода кнопка «Назад» не должна показывать страницы из кэша браузера
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");

        boolean authenticated = isAuthenticated(request);
        boolean publicPage = PUBLIC_PAGES.contains(path);
        if (authenticated == publicPage) {
            // вошедшему не нужны страницы входа, а гостю — страницы приложения
            redirect(request, response, authenticated ? HOME_PAGE : LOGIN_PAGE);
            return;
        }
        chain.doFilter(req, res);
    }

    private static boolean isAuthenticated(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute(USER_ATTRIBUTE) != null;
    }

    private static void redirect(HttpServletRequest request, HttpServletResponse response, String page)
            throws IOException {
        String url = request.getContextPath() + page;
        if ("partial/ajax".equals(request.getHeader("Faces-Request"))) {
            // ajax-запрос (например, сессия истекла): браузер должен сам перейти на страницу
            response.setContentType("text/xml");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<partial-response><redirect url=\"" + url + "\"/></partial-response>");
        } else {
            response.sendRedirect(url);
        }
    }
}
