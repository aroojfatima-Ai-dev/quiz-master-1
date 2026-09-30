# Quiz Master — Quiz master for teachers & students

Java 17 · Spring Boot 3 · Thymeleaf · Spring Security (BCrypt) · Spring Data JPA · MySQL (H2 fallback for local dev)

## What it does
| Step | Teacher | Student |
|---|---|---|
| 1 | Registers, creates a **class** → gets a 6-char code (e.g. `K7QPM2`) | Registers, enters the code, joins the class |
| 2 | Creates a **test**: title, language (English/Urdu), time limit, visibility (public / class-only), overtime yes/no | Sees public tests + tests of enrolled classes |
| 3 | Adds MCQs (4 options, correct answer, topic, difficulty) one by one **or bulk-imports from PDF / DOCX / TXT** | Starts a test: one question per page, countdown bar, keyboard shortcuts |
| 4 | Publishes | Test auto-submits at zero (or continues flagged as overtime if allowed) |
| 5 | Sees every submission: score, %, time taken, overtime/auto flags, per-question review | Gets instant score + full answer review; history kept |

Security: BCrypt password hashing, CSRF on every form, role-based routes (`/teacher/**`, `/student/**`, `/admin/**`), ownership checks on every entity, students only see tests they're entitled to.

## Run locally
```bash
mvn spring-boot:run          # uses embedded H2 file DB in ./data
# open http://localhost:8080
```

No Maven installed? Use the bundled Maven Wrapper (it fetches Maven itself on first run):
```bash
./mvnw spring-boot:run                 # Windows: mvnw.cmd spring-boot:run
```
Or the one-click helpers — `run-local.bat` (Windows, double-click) / `./run-local.sh` (macOS, Linux).

Step-by-step instructions (Roman Urdu + English), including the admin login, ports, settings and
troubleshooting, live in **[LOCAL-SETUP.md](LOCAL-SETUP.md)**.

## Deploy on Railway (MySQL)
1. Push this folder to a GitHub repo.
2. Railway → **New Project → Deploy from GitHub repo**. The `Dockerfile` is detected automatically (`railway.toml` included).
3. In the same project: **+ New → Database → MySQL**.
4. On the app service → **Variables**, add (use *Variable references* to the MySQL service):
   ```
   DB_URL      = jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?useSSL=true&serverTimezone=UTC&characterEncoding=utf8mb4
   DB_USER     = ${{MySQL.MYSQLUSER}}
   DB_PASSWORD = ${{MySQL.MYSQLPASSWORD}}
   APP_TZ      = Asia/Karachi
   ```
   `PORT` is injected by Railway automatically.
5. **Settings → Networking → Generate Domain**. Done — tables are created on first boot (`ddl-auto=update`).

> Urdu text is stored as UTF-8; `characterEncoding=utf8mb4` in the JDBC URL keeps it intact in MySQL.

## Admin panel

A third role, `ADMIN`, sits above teachers and students. The first admin is created automatically on boot
by `config/AdminSeeder` (only when no admin exists yet):

| Setting | Env var | Default |
|---|---|---|
| Bootstrap enabled | `APP_ADMIN_SEED` | `true` |
| Admin email (login) | `APP_ADMIN_EMAIL` | `admin@quizmaster.app` |
| Admin password | `APP_ADMIN_PASSWORD` | `Admin@12345` |

> Sign in at `/login` with the admin email — you land straight on `/admin/dashboard`.
> Change `APP_ADMIN_PASSWORD` (and delete the default account through the panel) before going public.
> To pre-seed by hand instead, insert a row into `users` with `role='ADMIN'` and a BCrypt `password_hash`
> and set `APP_ADMIN_SEED=false`.

| Page | What it shows |
|---|---|
| `/admin/dashboard` | Totals: teachers, students, classes, tests, attempts submitted (+ questions, live tests, average) |
| `/admin/users` | Every account (id, username, email, role, created) with role filters and a Delete action |
| `/admin/classes` | Every class: name, join code, owning teacher, enrolled students, tests — plus Delete |
| `/admin/tests` | Every test: title, teacher, status, visibility, language, question count, attempts, average — plus Delete |
| `/admin/tests/{id}/questions` | Read-only view of one test's questions (correct option marked) |
| `/admin/attempts` | Read-only log of every attempt across the system (student, test, score, dates) |

Deletions are guarded so no record is orphaned:

* a **teacher** can only be deleted once every class and test they own is gone (the panel says how many are left);
* a **class** can only be deleted once it holds no tests (enrollments are cleared with it);
* a **test** delete carries its questions and every attempt recorded against it;
* a **student** delete clears their enrollments and attempts first;
* **admin accounts** and **your own account** can't be deleted from the panel.

## Bulk-import format
See `samples/sample-questions.txt`. Each question block:
```
1. Question text?
A) option
B) option
C) option
D) option
Answer: B
Topic: Algebra          (optional)
Difficulty: Easy        (optional: Easy / Medium / Hard)
```
Markers `A)` `A.` `(A)` `A:` are all accepted; PDFs and Word files are read as text, then parsed with the same rules. You preview the parsed questions before confirming.

## Project layout
```
src/main/java/app/quizmaster
  config/     SecurityConfig, AppUser (principal), GlobalModel, AdminSeeder (first ADMIN account)
  model/      User, ClassRoom, Enrollment, Test, Question, Attempt, Answer, Role (TEACHER/STUDENT/ADMIN)
  repo/       Spring Data repositories
  service/    CodeService (class codes), QuestionImportService (PDF/DOCX/TXT parser), AttemptService (timer, scoring), Fmt
  web/        AuthController, TeacherController, StudentController, AdminController
src/main/resources
  templates/  Thymeleaf views (auth/, teacher/, student/, admin/, review.html, fragments/layout.html)
  static/css/quiz-master.css   design tokens: paper mode (admin) + slate stage (test runner)
```
