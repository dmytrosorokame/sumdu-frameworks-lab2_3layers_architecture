package sumdu.edu.ua.web.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sumdu.edu.ua.core.domain.Book;
import sumdu.edu.ua.core.domain.PageRequest;
import sumdu.edu.ua.web.config.Beans;

import java.io.IOException;
import java.util.Locale;

public class BooksApiServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(BooksApiServlet.class);

    private final ObjectMapper om = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int page = parseInt(req.getParameter("page"), 0);
        int size = parseInt(req.getParameter("size"), 10);
        String q = req.getParameter("q");
        String sort = req.getParameter("sort");

        if (page < 0) {
            writeError(resp, req.getRequestURI(), HttpServletResponse.SC_BAD_REQUEST,
                    "page must be >= 0");
            return;
        }
        if (size <= 0 || size > 100) {
            writeError(resp, req.getRequestURI(), HttpServletResponse.SC_BAD_REQUEST,
                    "size must be between 1 and 100");
            return;
        }

        try {
            PageRequest pageRequest = new PageRequest(page, size, sort, true);
            var result = Beans.getBookRepo().search(q, pageRequest);

            resp.setContentType("application/json;charset=UTF-8");
            om.writeValue(resp.getWriter(), result);
        } catch (Exception e) {
            log.error("DB error while GET /api/books", e);
            writeError(resp, req.getRequestURI(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "DB error");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Book book = om.readValue(req.getInputStream(), Book.class);

            if (book.getTitle() == null || book.getTitle().isBlank()
                    || book.getAuthor() == null || book.getAuthor().isBlank()) {
                log.warn("Bad request: title & author required");
                writeError(resp, req.getRequestURI(), HttpServletResponse.SC_BAD_REQUEST,
                        "title & author required");
                return;
            }

            if (book.getPubYear() <= 0) {
                log.warn("Bad request: invalid pubYear");
                writeError(resp, req.getRequestURI(), HttpServletResponse.SC_BAD_REQUEST,
                        "invalid pubYear");
                return;
            }

            Book saved = Beans.getBookRepo().add(
                    book.getTitle().trim(),
                    book.getAuthor().trim(),
                    book.getPubYear()
            );

            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.setContentType("application/json;charset=UTF-8");
            om.writeValue(resp.getWriter(), saved);

        } catch (Exception e) {
            log.error("DB error while POST /api/books", e);
            writeError(resp, req.getRequestURI(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "DB error");
        }
    }

    private int parseInt(String s, int def) {
        try {
            return (s != null) ? Integer.parseInt(s) : def;
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private void writeError(HttpServletResponse resp, String path, int status, String message) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json;charset=UTF-8");

        String errorName;
        switch (status) {
            case HttpServletResponse.SC_BAD_REQUEST:
                errorName = "Bad Request";
                break;
            case HttpServletResponse.SC_NOT_FOUND:
                errorName = "Not Found";
                break;
            case HttpServletResponse.SC_CONFLICT:
                errorName = "Conflict";
                break;
            case HttpServletResponse.SC_INTERNAL_SERVER_ERROR:
            default:
                errorName = "Internal Server Error";
                break;
        }

        ErrorResponse body = new ErrorResponse(status, errorName, message, path);
        om.writeValue(resp.getWriter(), body);
    }
}

