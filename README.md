## Lab 2 – 3-layer architecture (Books + Comments)

### Description

A simple web application for working with a catalog of books and comments, implemented using a **3-layer architecture**:

- **core** – domain model, ports (repository interfaces) and business logic;
- **persistence** – port implementations using **JDBC + H2**;
- **web** – HTTP layer (Servlet + JSP + JSON API).

The user can:

- browse the list of books with search, sorting and pagination;
- open a particular book page and see its comments;
- add comments and (according to business rules) delete them;
- work with JSON API for books.

### Technologies

- **Java**: 11
- **Maven**: 3.x (multi‑module)
- **Jakarta Servlet + JSP + JSTL**
- **Jetty**: 11.0.20 (via `jetty-maven-plugin`)
- **H2 Database**: 2.2.224 (file mode)
- **Jackson**: 2.17.2 (JSON serialization)
- **SLF4J + Logback**: logging
- **JUnit 5 + ArchUnit**: unit and architecture tests

### Project structure

```text
sumdu-frameworks-lab2_3layers_architecture/
├── pom.xml                         # parent (multi-module, packaging=pom)
├── core/                           # domain layer (business / ports)
│   └── src/main/java/sumdu/edu/ua/core/
│       ├── domain/                 # Book, Comment, Page, PageRequest
│       ├── port/                   # CatalogRepositoryPort, CommentRepositoryPort
│       └── service/                # CommentService (delete business rule)
├── persistence/                    # data access layer (H2 + JDBC)
│   ├── src/main/java/sumdu/edu/ua/persistence/jdbc/
│   │   ├── Db.java                 # H2 connection configuration
│   │   ├── DbInit.java             # schema creation and initial data
│   │   ├── JdbcBookRepository.java
│   │   └── JdbcCommentRepository.java
│   └── src/main/resources/
│       ├── schema.sql              # DDL for books and comments tables
│       └── initial-data.sql        # initial books and comments
└── web/                            # web layer (Servlet + JSP + API)
    ├── src/main/java/sumdu/edu/ua/web/
    │   ├── config/                 # Beans, AppInit
    │   └── http/                   # BooksServlet, CommentsServlet, BooksApiServlet
    ├── src/main/webapp/
    │   └── WEB-INF/
    │       ├── web.xml             # web descriptor
    │       └── views/              # books.jsp, book-comments.jsp
    └── data/                       # H2 DB file (guest.mv.db)
```

### Database schema

The application uses a file-based **H2** database with two tables: books and comments.

```sql
drop table if exists comments;
drop table if exists books;
create table if not exists books (
  id identity primary key,
  title varchar(255) not null,
  author varchar(255) not null,
  pub_year int not null
);
create table if not exists comments (
  id identity primary key,
  book_id bigint not null,
  author varchar(64) not null,
  text varchar(1000) not null,
  created_at timestamp not null default current_timestamp,
  constraint fk_book foreign key (book_id) references books(id) on delete cascade
);
```

Initial data (books and several comments) are defined in `persistence/src/main/resources/initial-data.sql`.

### Running the application

#### Build & tests

```bash
mvn clean test

```

#### Start Jetty

Run the web module with embedded Jetty:

```bash
cd web
mvn jetty:run
```

#### Stop

- press `Ctrl+C` in the terminal where Jetty is running.

#### Open in browser

- **Books catalog**: `http://localhost:8080/books`
- **Book page with comments**: `http://localhost:8080/books/{id}`  
  (for example, `http://localhost:8080/books/1`)

### API (JSON)

- **Base URL**: `http://localhost:8080/api`

#### GET /api/books

Get a page of books in JSON format.

- **Query parameters**:
  - `page` – page number (starts from 0, must be `>= 0`);
  - `size` – page size (`1..100`);
  - `q` – optional search by title/author;
  - `sort` – sort field (`title`, `author`, `pub_year` or empty).

**Example response:**

```json
{
  "items": [
    {
      "id": 1,
      "title": "Clean Code",
      "author": "Robert Martin",
      "pubYear": 2008
    }
  ],
  "page": 0,
  "size": 10,
  "total": 25
}
```

#### POST /api/books

Add a new book.

- **Request body (application/json):**

```json
{
  "title": "New Book",
  "author": "Some Author",
  "pubYear": 2024
}
```

- **Responses:**
  - `201 Created` – book created successfully, saved entity is returned in the body;
  - `400 Bad Request` – validation error (`title`, `author` are required, `pubYear` > 0);
  - `500 Internal Server Error` – database error.

### Business rules (comments)

- A comment can be deleted **only within 24 hours** from its creation  
  (implemented in `CommentService` in the `core` module).
- On rule violation the service throws `IllegalStateException`, and the web layer returns appropriate HTTP status.

### Database

- **URL**: `jdbc:h2:file:./data/guest;AUTO_SERVER=TRUE`
- **File**: `./data/guest.mv.db` (created automatically on first run)
- **User**: `sa`
- **Password**: empty

### Logging

- Logging is configured via **SLF4J + Logback** (configuration in `web/src/main/resources/logback.xml`).
- In the console you can see:
  - information about application start and URL (`AppInit`);
  - warnings about invalid requests;
  - database and business errors (e.g. when deleting comments).

### Testing and architecture rules

- Run all tests:

```bash
mvn test
```

With **ArchUnit** we check:

- correctness of the **3-layer architecture** (web → core → persistence, no reverse dependencies);
- that classes are located in appropriate packages (`domain`, `service`, `port`, `jdbc`, `http`, etc.).

### Screenshots

1. Books catalog page (`/books`) with search, sorting and pagination.
   ![Books catalog page](/screenshots/books.png)
2. Book page with comments (`/books/{id}`) and the form for adding a new comment.
   ![Book page with comments form](/screenshots/comments.png)
   ![Book page with comments list](/screenshots/comments-2.png)
3. Result of `GET /api/books` request in browser or Postman/Insomnia.
   ![GET /api/books request result](/screenshots/postman.png)
4. Console with Jetty/application logs during work
   ![Console with Jetty/application logs](/screenshots/logs.png)
5. Test report with ArchUnit tests
   ![Test report with ArchUnit tests](/screenshots/tests.png)
