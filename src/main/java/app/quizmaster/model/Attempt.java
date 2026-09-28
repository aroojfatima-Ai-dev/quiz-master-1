package app.quizmaster.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "attempts")
public class Attempt {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) private Test test;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) private User student;
    @Column(nullable = false) private Instant startedAt = Instant.now();
    private Instant submittedAt;
    @Column(nullable = false) private int currentIndex = 0;
    @Column(nullable = false) private int score = 0;
    @Column(nullable = false) private int total = 0;
    @Column(nullable = false) private double percentage = 0;
    @Column(nullable = false) private boolean overtime = false;
    @Column(nullable = false) private boolean autoSubmitted = false;
    @Column(nullable = false) private long durationSeconds = 0;
    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true) private List<Answer> answers = new ArrayList<>();

    public Long getId() { return id; }
    public Test getTest() { return test; } public void setTest(Test v) { test = v; }
    public User getStudent() { return student; } public void setStudent(User v) { student = v; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getSubmittedAt() { return submittedAt; } public void setSubmittedAt(Instant v) { submittedAt = v; }
    public int getCurrentIndex() { return currentIndex; } public void setCurrentIndex(int v) { currentIndex = v; }
    public int getScore() { return score; } public void setScore(int v) { score = v; }
    public int getTotal() { return total; } public void setTotal(int v) { total = v; }
    public double getPercentage() { return percentage; } public void setPercentage(double v) { percentage = v; }
    public boolean isOvertime() { return overtime; } public void setOvertime(boolean v) { overtime = v; }
    public boolean isAutoSubmitted() { return autoSubmitted; } public void setAutoSubmitted(boolean v) { autoSubmitted = v; }
    public long getDurationSeconds() { return durationSeconds; } public void setDurationSeconds(long v) { durationSeconds = v; }
    public List<Answer> getAnswers() { return answers; }
    public boolean isSubmitted() { return submittedAt != null; }
    public String getDurationLabel() { return String.format("%d:%02d", durationSeconds / 60, durationSeconds % 60); }
}
