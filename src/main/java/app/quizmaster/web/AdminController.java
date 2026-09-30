package app.quizmaster.web;

import app.quizmaster.config.AppUser;
import app.quizmaster.model.Attempt;
import app.quizmaster.model.ClassRoom;
import app.quizmaster.model.Enrollment;
import app.quizmaster.model.Role;
import app.quizmaster.model.Test;
import app.quizmaster.model.User;
import app.quizmaster.repo.AttemptRepo;
import app.quizmaster.repo.ClassRepo;
import app.quizmaster.repo.EnrollmentRepo;
import app.quizmaster.repo.QuestionRepo;
import app.quizmaster.repo.TestRepo;
import app.quizmaster.repo.UserRepo;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * System-wide administration: user / class / test oversight plus read-only attempt browsing.
 * Everything here is protected by {@code /admin/** → hasRole("ADMIN")} in SecurityConfig.
 *
 * Deletions are deliberately conservative — nothing is removed while a dependent record
 * would be orphaned; the operator gets a message telling them what to clear first.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {
    private final UserRepo users; private final ClassRepo classes; private final TestRepo tests;
    private final QuestionRepo questions; private final AttemptRepo attempts; private final EnrollmentRepo enrollments;

    public AdminController(UserRepo users, ClassRepo classes, TestRepo tests, QuestionRepo questions,
                           AttemptRepo attempts, EnrollmentRepo enrollments) {
        this.users = users; this.classes = classes; this.tests = tests;
        this.questions = questions; this.attempts = attempts; this.enrollments = enrollments;
    }

    /* ---------- dashboard ---------- */
    @GetMapping("/dashboard")
    @Transactional(readOnly = true)
    public String dashboard(Model m) {
        m.addAttribute("teacherCount", users.countByRole(Role.TEACHER));
        m.addAttribute("studentCount", users.countByRole(Role.STUDENT));
        m.addAttribute("adminCount", users.countByRole(Role.ADMIN));
        m.addAttribute("classCount", classes.count());
        m.addAttribute("testCount", tests.count());
        m.addAttribute("attemptCount", attempts.countBySubmittedAtIsNotNull());
        m.addAttribute("questionCount", questions.count());
        m.addAttribute("liveTestCount", tests.countByPublishedTrue());
        m.addAttribute("avgAll", attempts.avgPercentageAll());
        m.addAttribute("recent", attempts.findTop8BySubmittedAtIsNotNullOrderBySubmittedAtDesc());
        m.addAttribute("newest", users.findTop8ByOrderByCreatedAtDesc());
        m.addAttribute("nav", "overview");
        return "admin/dashboard";
    }

    /* ---------- users ---------- */
    @GetMapping("/users")
    @Transactional(readOnly = true)
    public String userList(@RequestParam(required = false) String role, Model m) {
        Role filter = parseRole(role);
        List<User> list = filter == null ? users.findAllByOrderByCreatedAtDesc() : users.findByRoleOrderByCreatedAtDesc(filter);
        Map<Long, String> footprint = new HashMap<>();
        for (User u : list) {
            if (u.getRole() == Role.TEACHER) {
                footprint.put(u.getId(), classes.countByTeacher(u) + " class(es) · " + tests.countByTeacher(u) + " test(s)");
            } else if (u.getRole() == Role.STUDENT) {
                footprint.put(u.getId(), enrollments.countByStudent(u) + " class(es) · "
                        + attempts.countByStudentAndSubmittedAtIsNotNull(u) + " submission(s)");
            } else {
                footprint.put(u.getId(), "full system access");
            }
        }
        m.addAttribute("list", list); m.addAttribute("footprint", footprint);
        m.addAttribute("role", filter == null ? "ALL" : filter.name());
        m.addAttribute("total", users.count());
        m.addAttribute("teacherCount", users.countByRole(Role.TEACHER));
        m.addAttribute("studentCount", users.countByRole(Role.STUDENT));
        m.addAttribute("adminCount", users.countByRole(Role.ADMIN));
        m.addAttribute("nav", "users");
        return "admin/users";
    }

    @PostMapping("/users/{id}/delete")
    @Transactional
    public String deleteUser(@AuthenticationPrincipal AppUser p, @PathVariable Long id, RedirectAttributes ra) {
        User u = users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (u.getId().equals(p.getUser().getId())) {
            ra.addFlashAttribute("error", "You can't delete the account you're signed in with.");
            return "redirect:/admin/users";
        }
        if (u.getRole() == Role.ADMIN) {
            ra.addFlashAttribute("error", "Admin accounts can't be deleted from the panel.");
            return "redirect:/admin/users";
        }
        if (u.getRole() == Role.TEACHER) {
            long ownedClasses = classes.countByTeacher(u), ownedTests = tests.countByTeacher(u);
            if (ownedClasses > 0 || ownedTests > 0) {
                ra.addFlashAttribute("error", u.getName() + " still owns " + ownedClasses + " class(es) and " + ownedTests
                        + " test(s). Delete those from the Classes and Tests tabs first, then remove the account.");
                return "redirect:/admin/users";
            }
        }
        /* remove the student's own records so no orphan rows are left behind */
        List<Attempt> theirAttempts = attempts.findByStudent(u);
        long submitted = theirAttempts.stream().filter(Attempt::isSubmitted).count();
        if (!theirAttempts.isEmpty()) attempts.deleteAll(theirAttempts);
        List<Enrollment> theirEnrollments = enrollments.findByStudent(u);
        if (!theirEnrollments.isEmpty()) enrollments.deleteAll(theirEnrollments);
        String email = u.getEmail(), role = u.getRole().name();
        users.delete(u);

        StringBuilder cleared = new StringBuilder();
        if (!theirAttempts.isEmpty()) cleared.append(submitted).append(" submission(s)");
        if (!theirEnrollments.isEmpty()) {
            if (cleared.length() > 0) cleared.append(" and ");
            cleared.append(theirEnrollments.size()).append(" enrollment(s)");
        }
        ra.addFlashAttribute("notice", "Account " + email + " (" + role + ") deleted"
                + (cleared.length() == 0 ? "." : " with " + cleared + "."));
        return "redirect:/admin/users";
    }

    /* ---------- classes ---------- */
    @GetMapping("/classes")
    @Transactional(readOnly = true)
    public String classList(Model m) {
        List<ClassRoom> list = classes.findAllByOrderByCreatedAtDesc();
        Map<Long, Long> studentCounts = new HashMap<>(), testCounts = new HashMap<>();
        long totalStudents = 0, totalTests = 0;
        for (ClassRoom c : list) {
            long students = enrollments.countByClassRoom(c), inClass = tests.countByClassRoom(c);
            studentCounts.put(c.getId(), students);
            testCounts.put(c.getId(), inClass);
            totalStudents += students; totalTests += inClass;
        }
        m.addAttribute("list", list); m.addAttribute("studentCounts", studentCounts); m.addAttribute("testCounts", testCounts);
        m.addAttribute("totalStudents", totalStudents); m.addAttribute("totalTests", totalTests);
        m.addAttribute("nav", "classes");
        return "admin/classes";
    }

    @PostMapping("/classes/{id}/delete")
    @Transactional
    public String deleteClass(@PathVariable Long id, RedirectAttributes ra) {
        ClassRoom c = classes.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Class not found"));
        long testCount = tests.countByClassRoom(c);
        if (testCount > 0) {
            ra.addFlashAttribute("error", "\"" + c.getName() + "\" still holds " + testCount
                    + " test(s). Delete them from the Tests tab first — classes are only removed once they are empty.");
            return "redirect:/admin/classes";
        }
        long studentCount = enrollments.countByClassRoom(c);
        String name = c.getName(), code = c.getCode();
        classes.delete(c);   // enrollments are cascade-removed with the class
        ra.addFlashAttribute("notice", "Class \"" + name + "\" (code " + code + ") deleted"
                + (studentCount == 0 ? "." : " — " + studentCount + " enrollment(s) cleared."));
        return "redirect:/admin/classes";
    }

    /* ---------- tests ---------- */
    @GetMapping("/tests")
    @Transactional(readOnly = true)
    public String testList(Model m) {
        List<Test> list = tests.findAllByOrderByUpdatedAtDesc();
        Map<Long, Integer> questionCounts = new HashMap<>();
        Map<Long, Long> attemptCounts = new HashMap<>();
        Map<Long, Double> averages = new HashMap<>();
        long totalQuestions = 0, totalAttempts = 0, liveTests = 0;
        for (Test t : list) {
            int qc = questions.countByTest(t);
            long ac = attempts.countByTestAndSubmittedAtIsNotNull(t);
            questionCounts.put(t.getId(), qc);
            attemptCounts.put(t.getId(), ac);
            averages.put(t.getId(), attempts.avgPercentage(t));
            totalQuestions += qc; totalAttempts += ac;
            if (t.isPublished()) liveTests++;
        }
        m.addAttribute("list", list); m.addAttribute("questionCounts", questionCounts);
        m.addAttribute("attemptCounts", attemptCounts); m.addAttribute("averages", averages);
        m.addAttribute("totalQuestions", totalQuestions); m.addAttribute("totalAttempts", totalAttempts);
        m.addAttribute("liveTests", liveTests);
        m.addAttribute("nav", "tests");
        return "admin/tests";
    }

    @GetMapping("/tests/{id}/questions")
    @Transactional(readOnly = true)
    public String testQuestions(@PathVariable Long id, Model m) {
        Test t = tests.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Test not found"));
        m.addAttribute("t", t);
        m.addAttribute("list", questions.findByTestOrderByPositionAscIdAsc(t));
        m.addAttribute("attemptCount", attempts.countByTestAndSubmittedAtIsNotNull(t));
        m.addAttribute("avg", attempts.avgPercentage(t));
        m.addAttribute("nav", "tests");
        return "admin/test-questions";
    }

    @PostMapping("/tests/{id}/delete")
    @Transactional
    public String deleteTest(@PathVariable Long id, RedirectAttributes ra) {
        Test t = tests.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Test not found"));
        List<Attempt> theirAttempts = attempts.findByTest(t);
        long submitted = theirAttempts.stream().filter(Attempt::isSubmitted).count();
        if (!theirAttempts.isEmpty()) attempts.deleteAll(theirAttempts);   // answers cascade with each attempt
        int questionCount = questions.countByTest(t);
        String title = t.getTitle();
        tests.delete(t);                                                   // questions cascade with the test
        ra.addFlashAttribute("notice", "Test \"" + title + "\" deleted with " + questionCount + " question(s)"
                + (theirAttempts.isEmpty() ? " and no attempts." : " and " + submitted + " submission(s)."));
        return "redirect:/admin/tests";
    }

    /* ---------- attempts (read-only) ---------- */
    @GetMapping("/attempts")
    @Transactional(readOnly = true)
    public String attemptList(@RequestParam(required = false) String status, Model m) {
        String filter = status == null || status.isBlank() ? "all" : status.trim().toLowerCase();
        List<Attempt> list;
        if (filter.equals("submitted")) {
            list = attempts.findBySubmittedAtIsNotNullOrderBySubmittedAtDesc();
        } else if (filter.equals("in-progress")) {
            list = attempts.findBySubmittedAtIsNullOrderByStartedAtDesc();
        } else {
            filter = "all";
            list = attempts.findAllByOrderByStartedAtDesc();
        }
        long submittedCount = attempts.countBySubmittedAtIsNotNull();
        m.addAttribute("list", list);
        m.addAttribute("status", filter);
        m.addAttribute("totalCount", attempts.count());
        m.addAttribute("submittedCount", submittedCount);
        m.addAttribute("openCount", attempts.count() - submittedCount);
        m.addAttribute("avgAll", attempts.avgPercentageAll());
        m.addAttribute("nav", "attempts");
        return "admin/attempts";
    }

    /* ---------- helpers ---------- */
    private Role parseRole(String value) {
        if (value == null || value.isBlank() || value.equalsIgnoreCase("ALL")) return null;
        try {
            return Role.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
