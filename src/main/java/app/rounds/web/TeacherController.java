package app.rounds.web;

import app.rounds.config.AppUser;
import app.rounds.model.*;
import app.rounds.repo.*;
import app.rounds.service.CodeService;
import app.rounds.service.QuestionImportService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/teacher")
public class TeacherController {
    private final ClassRepo classes; private final EnrollmentRepo enrollments; private final TestRepo tests;
    private final QuestionRepo questions; private final AttemptRepo attempts; private final AnswerRepo answers;
    private final CodeService codes; private final QuestionImportService importer;

    public TeacherController(ClassRepo classes, EnrollmentRepo enrollments, TestRepo tests, QuestionRepo questions,
                             AttemptRepo attempts, AnswerRepo answers, CodeService codes, QuestionImportService importer) {
        this.classes = classes; this.enrollments = enrollments; this.tests = tests; this.questions = questions;
        this.attempts = attempts; this.answers = answers; this.codes = codes; this.importer = importer;
    }

    /* ---------- dashboard ---------- */
    @GetMapping
    @Transactional(readOnly = true)
    public String dashboard(@AuthenticationPrincipal AppUser p, Model m) {
        User me = p.getUser();
        List<Test> myTests = tests.findByTeacherOrderByUpdatedAtDesc(me);
        Map<Long, Long> subs = new HashMap<>(); Map<Long, Double> avg = new HashMap<>(); Map<Long, Integer> qc = new HashMap<>();
        for (Test t : myTests) { subs.put(t.getId(), attempts.countByTestAndSubmittedAtIsNotNull(t)); avg.put(t.getId(), attempts.avgPercentage(t)); qc.put(t.getId(), questions.countByTest(t)); }
        List<Attempt> recent = attempts.recentForTeacher(me);
        m.addAttribute("tests", myTests.size() > 8 ? myTests.subList(0, 8) : myTests);
        m.addAttribute("subs", subs); m.addAttribute("avg", avg); m.addAttribute("qc", qc);
        m.addAttribute("recent", recent.size() > 6 ? recent.subList(0, 6) : recent);
        m.addAttribute("classes", classes.findByTeacherOrderByCreatedAtDesc(me));
        m.addAttribute("kpiTests", tests.countByTeacher(me));
        m.addAttribute("kpiAvg", attempts.avgPercentageForTeacher(me));
        m.addAttribute("kpiQuestions", questions.countByTeacher(me));
        m.addAttribute("kpiStudents", enrollments.countStudentsOfTeacher(me));
        m.addAttribute("nav", "overview");
        return "teacher/dashboard";
    }

    /* ---------- classes ---------- */
    @GetMapping("/classes")
    @Transactional(readOnly = true)
    public String classList(@AuthenticationPrincipal AppUser p, Model m) {
        List<ClassRoom> list = classes.findByTeacherOrderByCreatedAtDesc(p.getUser());
        Map<Long, Long> counts = list.stream().collect(Collectors.toMap(ClassRoom::getId, enrollments::countByClassRoom));
        Map<Long, Integer> testCounts = list.stream().collect(Collectors.toMap(ClassRoom::getId, c -> tests.findByClassRoomOrderByUpdatedAtDesc(c).size()));
        m.addAttribute("classes", list); m.addAttribute("counts", counts); m.addAttribute("testCounts", testCounts); m.addAttribute("nav", "classes");
        return "teacher/classes";
    }

    @PostMapping("/classes")
    public String createClass(@AuthenticationPrincipal AppUser p, @RequestParam String name, @RequestParam(required = false) String subject, RedirectAttributes ra) {
        if (name.isBlank()) { ra.addFlashAttribute("error", "Class name is required."); return "redirect:/teacher/classes"; }
        ClassRoom c = new ClassRoom(); c.setName(name.trim()); c.setSubject(subject == null ? null : subject.trim()); c.setTeacher(p.getUser()); c.setCode(codes.uniqueClassCode());
        classes.save(c);
        ra.addFlashAttribute("notice", "Class created. Share code " + c.getCode() + " with your students.");
        return "redirect:/teacher/classes/" + c.getId();
    }

    @GetMapping("/classes/{id}")
    @Transactional(readOnly = true)
    public String classDetail(@AuthenticationPrincipal AppUser p, @PathVariable Long id, Model m) {
        ClassRoom c = ownClass(p, id);
        m.addAttribute("c", c);
        m.addAttribute("students", enrollments.findByClassRoomOrderByJoinedAtAsc(c));
        List<Test> ts = tests.findByClassRoomOrderByUpdatedAtDesc(c);
        m.addAttribute("tests", ts);
        m.addAttribute("qc", ts.stream().collect(Collectors.toMap(Test::getId, questions::countByTest)));
        m.addAttribute("subs", ts.stream().collect(Collectors.toMap(Test::getId, attempts::countByTestAndSubmittedAtIsNotNull)));
        m.addAttribute("nav", "classes");
        return "teacher/class_detail";
    }

    @PostMapping("/classes/{id}/delete")
    @Transactional
    public String deleteClass(@AuthenticationPrincipal AppUser p, @PathVariable Long id, RedirectAttributes ra) {
        ClassRoom c = ownClass(p, id);
        if (!tests.findByClassRoomOrderByUpdatedAtDesc(c).isEmpty()) { ra.addFlashAttribute("error", "Delete the class's tests first."); return "redirect:/teacher/classes/" + id; }
        classes.delete(c); ra.addFlashAttribute("notice", "Class removed."); return "redirect:/teacher/classes";
    }

    /* ---------- tests ---------- */
    @GetMapping("/tests")
    @Transactional(readOnly = true)
    public String testList(@AuthenticationPrincipal AppUser p, Model m) {
        List<Test> list = tests.findByTeacherOrderByUpdatedAtDesc(p.getUser());
        m.addAttribute("tests", list);
        m.addAttribute("qc", list.stream().collect(Collectors.toMap(Test::getId, questions::countByTest)));
        m.addAttribute("subs", list.stream().collect(Collectors.toMap(Test::getId, attempts::countByTestAndSubmittedAtIsNotNull)));
        Map<Long, Double> avg = new HashMap<>(); list.forEach(t -> avg.put(t.getId(), attempts.avgPercentage(t)));
        m.addAttribute("avg", avg);
        m.addAttribute("classes", classes.findByTeacherOrderByCreatedAtDesc(p.getUser()));
        m.addAttribute("nav", "tests");
        return "teacher/tests";
    }

    @GetMapping("/tests/new")
    public String newTest(@AuthenticationPrincipal AppUser p, @RequestParam(required = false) Long classId, Model m) {
        List<ClassRoom> cs = classes.findByTeacherOrderByCreatedAtDesc(p.getUser());
        m.addAttribute("classes", cs); m.addAttribute("classId", classId); m.addAttribute("nav", "tests");
        return "teacher/test_form";
    }

    @PostMapping("/tests")
    public String createTest(@AuthenticationPrincipal AppUser p, @RequestParam String title, @RequestParam(required = false) String description,
                             @RequestParam Long classId, @RequestParam Test.Language language, @RequestParam Test.Visibility visibility,
                             @RequestParam int timeLimitMinutes, @RequestParam(defaultValue = "false") boolean allowOvertime, RedirectAttributes ra) {
        ClassRoom c = ownClass(p, classId);
        Test t = new Test(); t.setTitle(title.trim()); t.setDescription(description); t.setClassRoom(c); t.setTeacher(p.getUser());
        t.setLanguage(language); t.setVisibility(visibility); t.setTimeLimitMinutes(Math.max(1, Math.min(300, timeLimitMinutes))); t.setAllowOvertime(allowOvertime);
        tests.save(t);
        ra.addFlashAttribute("notice", "Test created — now add questions.");
        return "redirect:/teacher/tests/" + t.getId();
    }

    @GetMapping("/tests/{id}")
    @Transactional(readOnly = true)
    public String testDetail(@AuthenticationPrincipal AppUser p, @PathVariable Long id, Model m, HttpSession session) {
        Test t = ownTest(p, id);
        m.addAttribute("t", t);
        m.addAttribute("questions", questions.findByTestOrderByPositionAscIdAsc(t));
        m.addAttribute("subCount", attempts.countByTestAndSubmittedAtIsNotNull(t));
        m.addAttribute("avg", attempts.avgPercentage(t));
        m.addAttribute("classes", classes.findByTeacherOrderByCreatedAtDesc(p.getUser()));
        @SuppressWarnings("unchecked") List<Question> preview = (List<Question>) session.getAttribute("import-" + id);
        m.addAttribute("preview", preview);
        m.addAttribute("nav", "tests");
        return "teacher/test_detail";
    }

    @PostMapping("/tests/{id}/settings")
    @Transactional
    public String updateTest(@AuthenticationPrincipal AppUser p, @PathVariable Long id, @RequestParam String title, @RequestParam(required = false) String description,
                             @RequestParam Long classId, @RequestParam Test.Language language, @RequestParam Test.Visibility visibility,
                             @RequestParam int timeLimitMinutes, @RequestParam(defaultValue = "false") boolean allowOvertime, RedirectAttributes ra) {
        Test t = ownTest(p, id);
        t.setTitle(title.trim()); t.setDescription(description); t.setClassRoom(ownClass(p, classId)); t.setLanguage(language); t.setVisibility(visibility);
        t.setTimeLimitMinutes(Math.max(1, Math.min(300, timeLimitMinutes))); t.setAllowOvertime(allowOvertime);
        ra.addFlashAttribute("notice", "Settings saved."); return "redirect:/teacher/tests/" + id;
    }

    @PostMapping("/tests/{id}/publish")
    @Transactional
    public String publish(@AuthenticationPrincipal AppUser p, @PathVariable Long id, @RequestParam boolean value, RedirectAttributes ra) {
        Test t = ownTest(p, id);
        if (value && questions.countByTest(t) == 0) { ra.addFlashAttribute("error", "Add at least one question before publishing."); return "redirect:/teacher/tests/" + id; }
        t.setPublished(value);
        ra.addFlashAttribute("notice", value ? "Test is live for students." : "Test moved back to draft.");
        return "redirect:/teacher/tests/" + id;
    }

    @PostMapping("/tests/{id}/delete")
    @Transactional
    public String deleteTest(@AuthenticationPrincipal AppUser p, @PathVariable Long id, RedirectAttributes ra) {
        Test t = ownTest(p, id);
        attempts.deleteAll(attempts.findByTest(t));
        tests.delete(t); ra.addFlashAttribute("notice", "Test deleted."); return "redirect:/teacher/tests";
    }

    /* ---------- questions ---------- */
    @PostMapping("/tests/{id}/questions")
    @Transactional
    public String addQuestion(@AuthenticationPrincipal AppUser p, @PathVariable Long id, @RequestParam String prompt,
                              @RequestParam String optionA, @RequestParam String optionB, @RequestParam String optionC, @RequestParam String optionD,
                              @RequestParam String correct, @RequestParam(required = false) String topic, @RequestParam Question.Difficulty difficulty, RedirectAttributes ra) {
        Test t = ownTest(p, id);
        Question q = new Question(); q.setTest(t); q.setPosition(questions.countByTest(t) + 1);
        q.setPrompt(prompt.trim()); q.setOptionA(optionA.trim()); q.setOptionB(optionB.trim()); q.setOptionC(optionC.trim()); q.setOptionD(optionD.trim());
        q.setCorrect(correct.toUpperCase()); q.setTopic(topic == null || topic.isBlank() ? null : topic.trim()); q.setDifficulty(difficulty);
        questions.save(q);
        ra.addFlashAttribute("notice", "Question " + q.getPosition() + " added.");
        return "redirect:/teacher/tests/" + id + "#add";
    }

    @PostMapping("/tests/{id}/questions/{qid}/edit")
    @Transactional
    public String editQuestion(@AuthenticationPrincipal AppUser p, @PathVariable Long id, @PathVariable Long qid, @RequestParam String prompt,
                               @RequestParam String optionA, @RequestParam String optionB, @RequestParam String optionC, @RequestParam String optionD,
                               @RequestParam String correct, @RequestParam(required = false) String topic, @RequestParam Question.Difficulty difficulty, RedirectAttributes ra) {
        ownTest(p, id);
        Question q = questions.findById(qid).filter(x -> x.getTest().getId().equals(id)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        q.setPrompt(prompt.trim()); q.setOptionA(optionA.trim()); q.setOptionB(optionB.trim()); q.setOptionC(optionC.trim()); q.setOptionD(optionD.trim());
        q.setCorrect(correct.toUpperCase()); q.setTopic(topic == null || topic.isBlank() ? null : topic.trim()); q.setDifficulty(difficulty);
        ra.addFlashAttribute("notice", "Question updated."); return "redirect:/teacher/tests/" + id;
    }

    @PostMapping("/tests/{id}/questions/{qid}/delete")
    @Transactional
    public String deleteQuestion(@AuthenticationPrincipal AppUser p, @PathVariable Long id, @PathVariable Long qid, RedirectAttributes ra) {
        Test t = ownTest(p, id);
        questions.findById(qid).filter(x -> x.getTest().getId().equals(id)).ifPresent(questions::delete);
        int i = 1; for (Question q : questions.findByTestOrderByPositionAscIdAsc(t)) q.setPosition(i++);
        ra.addFlashAttribute("notice", "Question removed."); return "redirect:/teacher/tests/" + id;
    }

    /* ---------- bulk import ---------- */
    @PostMapping("/tests/{id}/import")
    public String importPreview(@AuthenticationPrincipal AppUser p, @PathVariable Long id, @RequestParam(required = false) MultipartFile file,
                                @RequestParam(required = false) String pasted, HttpSession session, RedirectAttributes ra) {
        ownTest(p, id);
        try {
            String text = (file != null && !file.isEmpty()) ? importer.extractText(file) : pasted;
            List<Question> parsed = importer.parse(text);
            if (parsed.isEmpty()) { ra.addFlashAttribute("error", "No complete questions found. Check the format guide below the upload box."); return "redirect:/teacher/tests/" + id + "#import"; }
            session.setAttribute("import-" + id, parsed);
            ra.addFlashAttribute("notice", parsed.size() + " question(s) parsed — review and confirm.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Could not read that file: " + e.getMessage());
        }
        return "redirect:/teacher/tests/" + id + "#import";
    }

    @PostMapping("/tests/{id}/import/confirm")
    @Transactional
    public String importConfirm(@AuthenticationPrincipal AppUser p, @PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        Test t = ownTest(p, id);
        @SuppressWarnings("unchecked") List<Question> parsed = (List<Question>) session.getAttribute("import-" + id);
        if (parsed == null) return "redirect:/teacher/tests/" + id;
        int pos = questions.countByTest(t);
        for (Question q : parsed) { q.setTest(t); q.setPosition(++pos); questions.save(q); }
        session.removeAttribute("import-" + id);
        ra.addFlashAttribute("notice", parsed.size() + " questions imported.");
        return "redirect:/teacher/tests/" + id;
    }

    @PostMapping("/tests/{id}/import/discard")
    public String importDiscard(@PathVariable Long id, HttpSession session) { session.removeAttribute("import-" + id); return "redirect:/teacher/tests/" + id + "#import"; }

    /* ---------- results ---------- */
    @GetMapping("/tests/{id}/results")
    @Transactional(readOnly = true)
    public String results(@AuthenticationPrincipal AppUser p, @PathVariable Long id, Model m) {
        Test t = ownTest(p, id);
        List<Attempt> list = attempts.findByTestAndSubmittedAtIsNotNullOrderBySubmittedAtDesc(t);
        m.addAttribute("t", t); m.addAttribute("attempts", list);
        m.addAttribute("avg", attempts.avgPercentage(t));
        m.addAttribute("best", list.stream().mapToDouble(Attempt::getPercentage).max().orElse(0));
        m.addAttribute("overtimeCount", list.stream().filter(Attempt::isOvertime).count());
        m.addAttribute("qCount", questions.countByTest(t));
        m.addAttribute("nav", "results");
        return "teacher/results";
    }

    @GetMapping("/attempts/{aid}")
    @Transactional(readOnly = true)
    public String attemptReview(@AuthenticationPrincipal AppUser p, @PathVariable Long aid, Model m) {
        Attempt a = attempts.findById(aid).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!a.getTest().getTeacher().getId().equals(p.getUser().getId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        m.addAttribute("a", a); m.addAttribute("t", a.getTest());
        m.addAttribute("rows", reviewRows(a));
        m.addAttribute("backUrl", "/teacher/tests/" + a.getTest().getId() + "/results");
        m.addAttribute("nav", "results");
        return "review";
    }

    @GetMapping("/results")
    @Transactional(readOnly = true)
    public String allResults(@AuthenticationPrincipal AppUser p, Model m) {
        m.addAttribute("recent", attempts.recentForTeacher(p.getUser())); m.addAttribute("nav", "results");
        return "teacher/all_results";
    }

    /* ---------- helpers ---------- */
    public List<Map<String, Object>> reviewRows(Attempt a) {
        Map<Long, Answer> byQ = answers.findByAttempt(a).stream().collect(Collectors.toMap(x -> x.getQuestion().getId(), x -> x));
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Question q : questions.findByTestOrderByPositionAscIdAsc(a.getTest())) {
            Answer ans = byQ.get(q.getId());
            Map<String, Object> r = new HashMap<>();
            r.put("q", q); r.put("selected", ans == null ? null : ans.getSelected()); r.put("correct", ans != null && ans.isCorrect());
            rows.add(r);
        }
        return rows;
    }

    private ClassRoom ownClass(AppUser p, Long id) {
        return classes.findById(id).filter(c -> c.getTeacher().getId().equals(p.getUser().getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Class not found"));
    }
    private Test ownTest(AppUser p, Long id) {
        return tests.findById(id).filter(t -> t.getTeacher().getId().equals(p.getUser().getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Test not found"));
    }
}
