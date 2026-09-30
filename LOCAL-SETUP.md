# Quiz Master — Local Setup Guide

**Is zip me:** Admin Panel wala complete project (ADMIN role, admin dashboard, users/classes/tests/attempts pages).
**Yahan se chalega:** apne laptop/PC par — koi server ya cloud ki zaroorat nahi.

---

## 0. Kya chahiye (Requirements)

| Cheez | Detail |
|---|---|
| **Java (JDK 17 ya upar)** | Ek hi baar install karna hai — neeche step 1 |
| Internet | Sirf **pehli baar** (Maven + libraries download hote hain, ~1–5 min) |
| Maven | ❌ Install karne ki zaroorat **nahi** — Maven Wrapper project ke andar hai (`mvnw` / `mvnw.cmd`) |
| Disk | ~400 MB (Maven cache + dependencies) |

---

## 1. JDK 17 install karo (ek hi baar)

**Windows** — PowerShell kholo aur yeh chalayo:
```powershell
winget install --id EclipseAdoptium.Temurin.17.JDK -e
```
Ya seedha installer: <https://adoptium.net/temurin/releases/?version=17> → **.msi** download karke install.

**macOS**
```bash
brew install --cask temurin@17
```

**Ubuntu / Debian**
```bash
sudo apt update && sudo apt install -y openjdk-17-jdk
```

**Check karo** (terminal/Command Prompt naya khol ke):
```bash
java -version
```
`17.x` ya usse zyada dikhna chahiye. Agar "command not found" aaye to terminal **band karke naya** kholo.

---

## 2. Zip extract karo

- **Windows:** zip par right-click → *Extract All…* → `C:\quiz-master-1`
- **macOS / Linux:** `unzip quiz-master-1-local.zip -d ~/quiz-master-1`

> Folder ke path me **spaces** na rakhein (`C:\quiz-master-1` best hai).

---

## 3. Server start karo

**Windows** — folder ke andar `run-local.bat` par **double-click** karo.

**macOS / Linux**
```bash
cd ~/quiz-master-1
chmod +x mvnw run-local.sh
./run-local.sh
```

Pehli baar output me `Downloading from: ...` lines aayengi — **yeh normal hai** (Maven + dependencies aa rahe hain).
Jab yeh line aa jaye to app tayyar hai:
```
Started QuizMasterApplication in 5.2 seconds
```

> Agar aapke paas Maven pehle se installed hai to `mvn spring-boot:run` bhi kaam karega — dono same hain.

---

## 4. Browser me kholo

### 👉 <http://localhost:8080>

Bilkul naya database hai, is liye **koi user pehle se nahi hoga** — admin account app khud bana deta hai jab pehli baar start hoti hai:

| Role | Email | Password |
|---|---|---|
| **Admin** | `admin@quizmaster.app` | `Admin@12345` |
| Teacher | `/register` page se khud banao | — |
| Student | `/register` page se khud banao | — |

Admin se login karo → seedha **`/admin/dashboard`** khul jayega (teacher/student nahi).

---

## 5. Admin panel ke pages

| URL | Kya milega |
|---|---|
| `/admin/dashboard` | Total **teachers, students, classes, tests, attempts submitted** + questions, live tests, average score, latest submissions, newest accounts |
| `/admin/users` | Saare users (id, username, email, role, created date) + role filters (All/Teacher/Student/Admin) + **Delete** |
| `/admin/classes` | Saari classes: naam, code, kis teacher ki hai, enrolled students, tests + **Delete** |
| `/admin/tests` | Saare tests: title, teacher, Live/Draft, Public/Class-only, Urdu/English, questions, attempts, average + **Delete** |
| `/admin/tests/{id}/questions` | Us test ke **saare sawal** (read-only, sahi option green) |
| `/admin/attempts` | Poore system ke attempts (student, test, teacher, score, %, status, dates) — **read-only** |

**Delete rules (safe deletion):** teacher tab tak delete nahi hota jab tak uske classes/tests maujood hain (message batata hai kitne bache hain) · class tab delete hoti hai jab us me test na ho · test delete karne par uske questions + attempts bhi jaate hain · student delete karne par uski enrollments + attempts saaf ho jaate hain · admin account aur apna khud ka account panel se delete nahi hota.

---

## 6. Band karna aur dobara chalana

- Band karne ke liye usi window me **`Ctrl + C`** dabao.
- Dobara chalane ke liye wahi `run-local.bat` / `./run-local.sh`.
- **Data save rehta hai** `./data/` folder me (H2 database file) — students, tests, attempts sab wapis mil jayenge.

---

## 7. Settings (optional)

| Kya chahiye | Kaise |
|---|---|
| Admin **password badalna** (naya setup) | Server band karo → `data/` folder delete karo → env var set karke start karo (neeche dekho) → naya admin naye password ke sath banega |
| Admin **email badalna** | `APP_ADMIN_EMAIL` env var (same tarika) |
| Seeder **band** karna | `APP_ADMIN_SEED=false` |
| **Port** badalna (8080 busy ho) | `PORT=8081` |
| **Timezone** | `APP_TZ=Asia/Karachi` |

**Windows (Command Prompt)** — `run-local.bat` se pehle:
```bat
set APP_ADMIN_EMAIL=admin@myschool.pk
set APP_ADMIN_PASSWORD=MeraPassword123
set PORT=8081
run-local.bat
```

**macOS / Linux:**
```bash
APP_ADMIN_EMAIL=admin@myschool.pk APP_ADMIN_PASSWORD=MeraPassword123 PORT=8081 ./run-local.sh
```

> ⚠️ Yeh env vars **sirf tab** kaam karte hain jab database me koi ADMIN na ho. Is liye password badalne ka sabse aasan tarika: `data/` folder delete → naye settings ke sath start.

**MySQL par chalana ho** (H2 ki jagah): README.md → *Deploy on Railway (MySQL)* section dekho, wahi `DB_URL`, `DB_USER`, `DB_PASSWORD` env vars local par bhi chal jaate hain.

---

## 8. Bilkul fresh start (sara data delete)

1. Server band karo (`Ctrl + C`)
2. Project folder se `data/` folder delete karo
3. Dobara start karo → naya database + naya admin account

---

## 9. Masail (Troubleshooting)

| Problem | Hal |
|---|---|
| `'java' is not recognized` / `java: command not found` | JDK 17 install karo (step 1) aur **naya** terminal kholo |
| `Port 8080 was already in use` | `set PORT=8081` (Windows) / `PORT=8081 ./run-local.sh` — phir <http://localhost:8081> |
| `./run-local.sh: Permission denied` | `chmod +x mvnw run-local.sh` |
| Windows par `run-local.sh` chalane ki koshish | `.sh` ki jagah `run-local.bat` use karo |
| Pehla build slow lag raha hai | Normal — pehli baar ~200 MB dependencies download hoti hain |
| `Downloading from: ...` par fail ho jaye | Internet check karo; dobara chalao. Bar-bar fail ho to Maven manually install karke `mvn spring-boot:run` |
| Login par `Email or password didn't match` | `data/` delete karke fresh start karo (admin dobara seed hoga) |
| Urdu text theek na dikhe | Browser zoom/font check karo; app UTF-8 me hi save karta hai (MySQL me `characterEncoding=utf8mb4` URL me hona chahiye) |

---

## 10. Project ke andar kya naya hai (Admin Panel)

```
src/main/java/app/quizmaster
  model/Role.java                     TEACHER, STUDENT, ADMIN
  config/AdminSeeder.java             pehli baar admin account banata hai
  config/SecurityConfig.java          /admin/** sirf ADMIN ke liye + login redirect
  web/AdminController.java            dashboard, users, classes, tests, questions, attempts
  repo/*.java                         naye count/aggregate queries
src/main/resources/templates/admin/   dashboard, users, classes, tests, test-questions, attempts
src/main/resources/templates/fragments/layout.html   ADMIN-only nav link
mvnw / mvnw.cmd / .mvn/               Maven Wrapper (Maven install ki zaroorat nahi)
run-local.sh / run-local.bat          one-click start
```

Teacher aur student ka saara purana kaam (aur bulk-import) bilkul waisa hi hai — sirf naya admin panel upar se add hua hai.
