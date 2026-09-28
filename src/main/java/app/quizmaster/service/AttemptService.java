package app.quizmaster.service;

import app.quizmaster.model.*;
import app.quizmaster.repo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AttemptService {
    private final AttemptRepo attempts; private final AnswerRepo answers; private final QuestionRepo questions;
    public AttemptService(AttemptRepo attempts, AnswerRepo answers, QuestionRepo questions) {
        this.attempts = attempts; this.answers = answers; this.questions = questions;
    }

    @Transactional
    public Attempt startOrResume(Test test, User student) {
        return attempts.findFirstByTestAndStudentAndSubmittedAtIsNull(test, student).orElseGet(() -> {
            Attempt a = new Attempt(); a.setTest(test); a.setStudent(student);
            a.setTotal(questions.countByTest(test));
            return attempts.save(a);
        });
    }

    public long secondsRemaining(Attempt a) {
        long limit = a.getTest().getTimeLimitMinutes() * 60L;
        long elapsed = Duration.between(a.getStartedAt(), Instant.now()).getSeconds();
        return limit - elapsed;
    }

    @Transactional
    public void recordAnswer(Attempt a, Question q, String selected) {
        Answer ans = answers.findByAttemptAndQuestion(a, q).orElseGet(() -> { Answer n = new Answer(); n.setAttempt(a); n.setQuestion(q); return n; });
        ans.setSelected(selected);
        ans.setCorrect(selected != null && selected.equals(q.getCorrect()));
        answers.save(ans);
    }

    @Transactional
    public Attempt submit(Attempt a, boolean auto) {
        if (a.isSubmitted()) return a;
        List<Question> qs = questions.findByTestOrderByPositionAscIdAsc(a.getTest());
        Map<Long, Answer> byQ = answers.findByAttempt(a).stream().collect(Collectors.toMap(x -> x.getQuestion().getId(), x -> x));
        int score = 0;
        for (Question q : qs) {
            Answer ans = byQ.get(q.getId());
            if (ans == null) { ans = new Answer(); ans.setAttempt(a); ans.setQuestion(q); ans.setSelected(null); ans.setCorrect(false); answers.save(ans); }
            if (ans.isCorrect()) score++;
        }
        Instant now = Instant.now();
        long secs = Duration.between(a.getStartedAt(), now).getSeconds();
        a.setSubmittedAt(now); a.setDurationSeconds(secs);
        a.setScore(score); a.setTotal(qs.size());
        a.setPercentage(qs.isEmpty() ? 0 : Math.round(score * 1000.0 / qs.size()) / 10.0);
        a.setOvertime(secs > a.getTest().getTimeLimitMinutes() * 60L);
        a.setAutoSubmitted(auto);
        return attempts.save(a);
    }
}
