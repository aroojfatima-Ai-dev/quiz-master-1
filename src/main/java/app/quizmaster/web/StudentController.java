package app.quizmaster.web;

import app.quizmaster.config.AppUser;
import app.quizmaster.model.*;
import app.quizmaster.repo.*;
import app.quizmaster.service.AttemptService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/student")
public class StudentController {
    private final ClassRepo classes; private final EnrollmentRepo enrollments; private final TestRepo tests;
    private final QuestionRepo questions; private final AttemptRepo attempts; private final AnswerRepo answers; private final AttemptService svc;
    private final TeacherController teacherCtl;

    public StudentController(ClassRepo classes, EnrollmentRepo enrollments, TestRepo tests, QuestionRepo questions,
                             AttemptRepo attempts, AnswerRepo answers, AttemptService svc, TeacherController teacherCtl) {
        this.classes = classes; this.enrollments = enrollments; this.tests = tests; this.questions = questions;
        this.attempts = attempts; this.answers = answers; this.svc = svc; this.teacherCtl = teacherCtl;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public String dashboard(@AuthenticationPrincipal AppUser p, Model m) {
        User me = p.getUser();
        List<Enrollment> ens = enrollments.findByStudentOrderByJoinedAtDesc(me);
        List<ClassRoom> myClasses = ens.stream().map(Enrollment::getClassRoom).toList();
        List<Test> available = myClasses.isEmpty() ? tests.findPublic() : tests.findAvailableForStudent(myClasses);
        List<Attempt> history = attempts.findByStudentAndSubmittedAtIsNotNullOrderBySubmittedAtDesc(me);
        Map<Long, Attempt> best = new HashMap<>();
        for (Attempt a : history) best.merge(a.getTest().getId(), a, (x, y) -> x.getPercentage() >= y.getPercentage() ? x : y);
        Map<Long, Boolean> inProgress = new HashMap<>();
        for (Test t : available) inProgress.put(t.getId(), attempts.findFirstByTestAndStudentAndSubmittedAtIsNull(t, me).isPresent());
        m.addAttribute("enrollments", ens); m.addAttribute("available", available);
        m.addAttribute("qc", available.stream().collect(Collectors.toMap(Test::getId, questions::countByTest)));
        m.addAttribute("history", history.size() > 8 ? history.subList(0, 8) : history);
        m.addAttribute("best", best); m.addAttribute("inProgress", inProgress);
        m.addAttribute("kpiTaken", history.size());
        m.addAttribute("kpiAvg", history.isEmpty() ? null : history.stream().mapToDouble(Attempt::getPercentage).average().orElse(0));
        m.addAttribute("kpiBest", history.stream().mapToDouble(Attempt::getPercentage).max().orElse(0));
        m.addAttribute("nav", "overview");
        return "student/dashboard";
    }

    @PostMapping("/join")
    @Transactional
    public String join(@AuthenticationPrincipal AppUser p, @RequestParam String code, RedirectAttributes ra) {
        String c = code.trim().toUpperCase().replaceAll("[^A-Z0-9]", "");
        Optional<ClassRoom> room = classes.findByCodeIgnoreCase(c);
        if (room.isEmpty()) { ra.addFlashAttribute("error", "No class found for code " + c + ". Check it with your teacher."); return "redirect:/student"; }
        if (enrollments.existsByStudentAndClassRoom(p.getUser(), room.get())) { ra.addFlashAttribute("notice", "You're already in " + room.get().getName() + "."); return "redirect:/student"; }
        Enrollment e = new Enrollment(); e.setStudent(p.getUser()); e.setClassRoom(room.get()); enrollments.save(e);
        ra.addFlashAttribute("notice", "Joined " + room.get().getName() + ".");
        return "redirect:/student";
    }

    @GetMapping("/history")
    @Transactional(readOnly = true)
    public String history(@AuthenticationPrincipal AppUser p, Model m) {
        m.addAttribute("history", attempts.findByStudentAndSubmittedAtIsNotNullOrderBySubmittedAtDesc(p.getUser())); m.addAttribute("nav", "history");
        return "student/history";
    }

    @GetMapping("/tests/{id}")
    @Transactional(readOnly = true)
    public String testIntro(@AuthenticationPrincipal AppUser p, @PathVariable Long id, Model m) {
        Test t = accessibleTest(p, id);
        m.addAttribute("t", t); m.addAttribute("qCount", questions.countByTest(t));
        m.addAttribute("previous", attempts.findByTestAndStudentOrderByStartedAtDesc(t, p.getUser()).stream().filter(Attempt::isSubmitted).toList());
        m.addAttribute("resume", attempts.findFirstByTestAndStudentAndSubmittedAtIsNull(t, p.getUser()).isPresent());
        m.addAttribute("nav", "overview");
        return "student/test_intro";
    }

    @PostMapping("/tests/{id}/start")
    public String start(@AuthenticationPrincipal AppUser p, @PathVariable Long id) {
        Test t = accessibleTest(p, id);
        Attempt a = svc.startOrResume(t, p.getUser());
        return "redirect:/student/attempts/" + a.getId() + "/q/" + a.getCurrentIndex();
    }

    @GetMapping("/attempts/{aid}/q/{idx}")
    @Transactional(readOnly = true)
    public String question(@AuthenticationPrincipal AppUser p, @PathVariable Long aid, @PathVariable int idx, Model m) {
        Attempt a = ownAttempt(p, aid);
        if (a.isSubmitted()) return "redirect:/student/attempts/" + aid;
        List<Question> qs = questions.findByTestOrderByPositionAscIdAsc(a.getTest());
        if (qs.isEmpty()) return "redirect:/student";
        idx = Math.max(0, Math.min(idx, qs.size() - 1));
        long remaining = svc.secondsRemaining(a);
        if (remaining <= 0 && !a.getTest().isAllowOvertime()) return "redirect:/student/attempts/" + aid + "/timeout";
        Question q = qs.get(idx);
        Map<Long, Answer> byQ = answers.findByAttempt(a).stream().collect(Collectors.toMap(x -> x.getQuestion().getId(), x -> x));
        Set<Long> answered = byQ.keySet();
        m.addAttribute("a", a); m.addAttribute("t", a.getTest()); m.addAttribute("q", q); m.addAttribute("idx", idx); m.addAttribute("count", qs.size());
        m.addAttribute("selected", byQ.containsKey(q.getId()) ? byQ.get(q.getId()).getSelected() : null);
        m.addAttribute("answeredCount", answered.size());
        m.addAttribute("remaining", remaining);
        m.addAttribute("totalSeconds", a.getTest().getTimeLimitMinutes() * 60L);
        m.addAttribute("isLast", idx == qs.size() - 1);
        m.addAttribute("nav", "overview");
        return "student/question";
    }

    @PostMapping("/attempts/{aid}/q/{idx}")
    @Transactional
    public String answer(@AuthenticationPrincipal AppUser p, @PathVariable Long aid, @PathVariable int idx,
                         @RequestParam(required = false) String choice, @RequestParam String action) {
        Attempt a = ownAttempt(p, aid);
        if (a.isSubmitted()) return "redirect:/student/attempts/" + aid;
        List<Question> qs = questions.findByTestOrderByPositionAscIdAsc(a.getTest());
        idx = Math.max(0, Math.min(idx, qs.size() - 1));
        Question q = qs.get(idx);
        if (choice != null && choice.matches("[ABCD]")) svc.recordAnswer(a, q, choice);
        long remaining = svc.secondsRemaining(a);
        if (remaining <= 0 && !a.getTest().isAllowOvertime()) { svc.submit(a, true); return "redirect:/student/attempts/" + aid; }
        switch (action) {
            case "prev" -> { a.setCurrentIndex(Math.max(0, idx - 1)); attempts.save(a); return "redirect:/student/attempts/" + aid + "/q/" + a.getCurrentIndex(); }
            case "submit" -> { svc.submit(a, false); return "redirect:/student/attempts/" + aid; }
            case "timeout" -> { svc.submit(a, true); return "redirect:/student/attempts/" + aid; }
            default -> { a.setCurrentIndex(Math.min(qs.size() - 1, idx + 1)); attempts.save(a); return "redirect:/student/attempts/" + aid + "/q/" + a.getCurrentIndex(); }
        }
    }

    /** hit when the clock reaches zero and overtime is not allowed */
    @GetMapping("/attempts/{aid}/timeout")
    @Transactional
    public String timeout(@AuthenticationPrincipal AppUser p, @PathVariable Long aid) {
        Attempt a = ownAttempt(p, aid);
        if (!a.isSubmitted()) svc.submit(a, true);
        return "redirect:/student/attempts/" + aid + "?auto";
    }

    @GetMapping("/attempts/{aid}")
    @Transactional(readOnly = true)
    public String review(@AuthenticationPrincipal AppUser p, @PathVariable Long aid, Model m) {
        Attempt a = ownAttempt(p, aid);
        if (!a.isSubmitted()) return "redirect:/student/attempts/" + aid + "/q/" + a.getCurrentIndex();
        m.addAttribute("a", a); m.addAttribute("t", a.getTest()); m.addAttribute("rows", teacherCtl.reviewRows(a));
        m.addAttribute("backUrl", "/student"); m.addAttribute("nav", "history");
        return "review";
    }

    private Test accessibleTest(AppUser p, Long id) {
        Test t = tests.findById(id).filter(Test::isPublished).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Test not available"));
        if (t.getVisibility() == Test.Visibility.PUBLIC) return t;
        if (enrollments.existsByStudentAndClassRoom(p.getUser(), t.getClassRoom())) return t;
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Join the class first to take this test");
    }
    private Attempt ownAttempt(AppUser p, Long aid) {
        return attempts.findById(aid).filter(a -> a.getStudent().getId().equals(p.getUser().getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
