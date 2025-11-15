package sumdu.edu.ua.web.http;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sumdu.edu.ua.core.domain.Book;
import sumdu.edu.ua.core.domain.Page;
import sumdu.edu.ua.core.domain.PageRequest;
import sumdu.edu.ua.web.config.Beans;

import java.io.IOException;

public class BooksServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        try {
            int page = parseInt(req.getParameter("page"), 0);
            int size = parseInt(req.getParameter("size"), 20);
            String q = req.getParameter("q");
            String sort = req.getParameter("sort");

            if (page < 0) {
                page = 0;
            }
            if (size <= 0 || size > 100) {
                size = 20;
            }

            PageRequest pageRequest = new PageRequest(page, size, sort, true);

            Page<Book> result = Beans.getBookRepo().search(q, pageRequest);
            long total = result.getTotal();
            int totalPages = (int) ((total + size - 1) / size);

            req.setAttribute("books", result.getItems());
            req.setAttribute("q", q);
            req.setAttribute("page", page);
            req.setAttribute("size", size);
            req.setAttribute("sort", sort);
            req.setAttribute("total", total);
            req.setAttribute("totalPages", totalPages);

            req.getRequestDispatcher("/WEB-INF/views/books.jsp").forward(req, resp);

        } catch (Exception e) {
            throw new ServletException("Cannot load books", e);
        }
    }

    private int parseInt(String s, int def) {
        try {
            return (s != null) ? Integer.parseInt(s) : def;
        } catch (NumberFormatException e) {
            return def;
        }
    }
}

