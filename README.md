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

Security: BCrypt password hashing, CSRF on every form, role-based routes (`/teacher/**`, `/student/**`), ownership checks on every entity, students only see tests they're entitled to.

## Run locally
```bash
mvn spring-boot:run          # uses embedded H2 file DB in ./data
# open http://localhost:8080
```

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
  config/     SecurityConfig, AppUser (principal), GlobalModel
  model/      User, ClassRoom, Enrollment, Test, Question, Attempt, Answer
  repo/       Spring Data repositories
  service/    CodeService (class codes), QuestionImportService (PDF/DOCX/TXT parser), AttemptService (timer, scoring), Fmt
  web/        AuthController, TeacherController, StudentController
src/main/resources
  templates/  Thymeleaf views (auth/, teacher/, student/, review.html, fragments/layout.html)
  static/css/quiz-master.css   design tokens: paper mode (admin) + slate stage (test runner)
```
