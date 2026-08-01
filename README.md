# Cupid — User Profile Component

COIT13235 Enterprise Software Development — Assignment 1
CQUniversity

A component of **Cupid**, a clone of the Tinder dating service. This submission implements the **User Profile** function.

---

## Requirements Implemented

| Requirement | Description | Where Implemented |
|---|---|---|
| `FR_Profile` | Create/update profile (name, age, bio) | `ProfileService.createProfile`, `JdbcProfileRepository.save` (insert/update) |
| `FR_Profile_Picture` | Upload profile pictures | `ProfilePictureStorage`, `ProfileService.uploadProfilePicture` |
| `FR_Profile_Fetch` | Fetch user profile | `ProfileService.findById`, `JdbcProfileRepository.findById` |
| `FR_Profile_Keep_Ethics` | Difficult but not impossible to delete, toggleable via a single setting | `EthicsConfig`, `ProfileService.deleteProfile` (soft-delete vs hard-delete) |
| `FR_Web_UI` | Web user interface | `ProfileWebServer` (Java's built-in `HttpServer`) |
| `NFR_Input_Sanitise` | All user input sanitised | `PreparedStatement` (SQL injection prevention), `ProfileValidator` (data validation rules), `escapeHtml` (XSS prevention), file-type whitelist in `ProfilePictureStorage` |
| `NFR_Traceability` | Code documented with requirement/design references | Javadoc comments referencing `docs/erd-profile.png`, `docs/uml-profile.png`, and FR/NFR codes throughout |

---

## Architecture

Layered design: **Web → Service → Repository → Database**

- **`Profile`** — domain entity (plain data holder, no business logic)
- **`ProfileRepository`** (interface) + **`JdbcProfileRepository`** (implementation) — persistence abstraction using the Repository pattern; `ProfileService` depends only on the interface, enabling the database technology to be swapped without touching business logic (Dependency Inversion)
- **`ProfileService`** — business logic layer, including the ethics-toggle delete decision and input validation orchestration
- **`EthicsConfig`** — the single setting controlling `FR_Profile_Keep_Ethics`
- **`ProfileValidator`** — input validation rules (name, age, bio length)
- **`ProfilePictureStorage`** — file upload handling with UUID-based filenames and file-type whitelisting
- **`ProfileWebServer`** — web UI using Java's built-in `com.sun.net.httpserver.HttpServer`
- **`Main`** — application entry point, wires all dependencies together

See `docs/erd-profile.png` (database schema) and `docs/uml-profile.png` (class relationships) for design diagrams.

---

## Technology Stack

- Java 24
- Maven (build tool)
- JDBC (plain, no ORM) with H2 in-memory database
- JUnit 6 (Jupiter) for testing
- No external web framework — uses Java's built-in `HttpServer`

---

## How to Run

```bash
mvn clean package
java -cp target/classes com.cqu.cupid.profile.Main
```

Then open `http://localhost:8080` in a browser.

### Available pages/routes
- `/` — home page with all forms
- `/create` — create a new profile
- `/view?id=X` — view a profile by ID
- `/delete` — delete a profile (respects ethics setting)
- `/upload` — upload a profile picture
- `/image?id=X` — serves the stored profile picture
- `/settings` — toggle the ethics setting (soft-delete vs hard-delete)

---

## How to Run Tests

```bash
mvn clean test
```

Test coverage includes:
- `JdbcProfileRepositoryTest` — save, findById, softDelete against a real H2 database
- `ProfileServiceTest` — ethics-toggle logic (proves soft-delete keeps the row while hard-delete truly removes it), profile picture upload flow
- `ProfileValidatorTest` — name/age/bio validation rules
- `ProfilePictureStorageTest` — file storage mechanism

---

## Known Limitations

- **In-memory database:** uses H2 (`jdbc:h2:mem:`) for fast, zero-setup development per course guidance. Data resets whenever the application restarts. A production deployment would use a persistent database (e.g. H2 file mode, MySQL, PostgreSQL) — the `ProfileRepository` interface makes this a drop-in change without touching `ProfileService`.
- **Multipart parsing:** profile picture upload uses a hand-written multipart/form-data parser rather than an external library, since the project uses plain JDBC/`HttpServer` without a web framework. This is a reasonable trade-off for the assignment's scope; production code would use a dedicated multipart-handling library.
- **Ethics settings page:** currently a simple, unsecured web page (`/settings`). A production system would restrict this to authorised administrators only.

---

## AI Assistance Disclosure

See [AI_USAGE.md](AI_USAGE.md) for a full discussion of AI tools used and how they were used.