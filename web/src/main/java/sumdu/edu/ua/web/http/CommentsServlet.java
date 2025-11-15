package sumdu.edu.ua.web.http;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sumdu.edu.ua.core.domain.Book;
import sumdu.edu.ua.core.domain.Comment;
import sumdu.edu.ua.core.domain.Page;
import sumdu.edu.ua.core.domain.PageRequest;
import sumdu.edu.ua.web.config.Beans;

import java.io.IOException;

public class CommentsServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(CommentsServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Long bookId = resolveBookId(req);
        if (bookId == null) {
            resp.sendRedirect(req.getContextPath() + "/books");
            return;
        }

        try {
            Book book = Beans.getBookRepo().findById(bookId);

            if (book == null) {
                log.warn("Book not found: {}", bookId);
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Book not found");
                return;
            }

            int page = parseInt(req.getParameter("page"), 0);
            int size = parseInt(req.getParameter("size"), 20);

            if (page < 0) {
                page = 0;
            }
            if (size <= 0 || size > 100) {
                size = 20;
            }

            PageRequest pageRequest = new PageRequest(page, size);
            Page<Comment> result = Beans.getCommentRepo()
                    .list(bookId, null, null, pageRequest);

            long total = result.getTotal();
            int totalPages = (int) ((total + size - 1) / size);

            req.setAttribute("book", book);
            req.setAttribute("comments", result.getItems());
            req.setAttribute("page", page);
            req.setAttribute("size", size);
            req.setAttribute("total", total);
            req.setAttribute("totalPages", totalPages);
            req.getRequestDispatcher("/WEB-INF/views/book-comments.jsp").forward(req, resp);
        } catch (Exception e) {
            log.error("Cannot load book details", e);
            throw new ServletException("Cannot load book details", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        req.setCharacterEncoding("UTF-8");
        String method = req.getParameter("_method");

        Long bookId = resolveBookId(req);
        if (bookId == null) {
            resp.sendRedirect(req.getContextPath() + "/books");
            return;
        }

        if ("delete".equalsIgnoreCase(method)) {
            long commentId = Long.parseLong(req.getParameter("commentId"));

            try {
                Beans.getCommentService().delete(bookId, commentId);
                resp.sendRedirect(req.getContextPath() + "/books/" + bookId);
            } catch (IllegalStateException e) {
                log.warn("Cannot delete comment due to business rule: {}", e.getMessage());
                resp.sendError(HttpServletResponse.SC_CONFLICT, e.getMessage());
            } catch (Exception e) {
                log.error("Cannot delete comment", e);
                throw new ServletException("Cannot delete comment", e);
            }
            return;
        }

        String author = req.getParameter("author");
        String text = req.getParameter("text");

        if (author == null || author.isBlank() || text == null || text.isBlank()) {
            log.warn("Bad request: author & text required");
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "author & text required");
            return;
        }

        try {
            Beans.getCommentRepo().add(bookId, author.trim(), text.trim());
            resp.sendRedirect(req.getContextPath() + "/books/" + bookId);
        } catch (Exception e) {
            log.error("Cannot save comment", e);
            throw new ServletException("Cannot save comment", e);
        }
    }

    private int parseInt(String s, int def) {
        try {
            return (s != null) ? Integer.parseInt(s) : def;
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private Long resolveBookId(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.length() > 1) {
            String idPart = pathInfo.substring(1);
            try {
                return Long.parseLong(idPart);
            } catch (NumberFormatException e) {
                log.warn("Invalid book id in path: {}", pathInfo);
                return null;
            }
        }

        String param = req.getParameter("bookId");
        if (param != null) {
            try {
                return Long.parseLong(param);
            } catch (NumberFormatException e) {
                log.warn("Invalid bookId parameter: {}", param);
            }
        }
        return null;
    }
}

