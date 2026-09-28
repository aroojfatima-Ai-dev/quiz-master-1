package app.quizmaster.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "tests")
public class Test {
    public enum Language { ENGLISH, URDU }
    public enum Visibility { PUBLIC, CLASS_ONLY }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 160) private String title;
    @Column(length = 500) private String description;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Language language = Language.ENGLISH;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Visibility visibility = Visibility.CLASS_ONLY;
    /** total time limit in minutes */
    @Column(nullable = false) private int timeLimitMinutes = 10;
    @Column(nullable = false) private boolean allowOvertime = false;
    @Column(nullable = false) private boolean published = false;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) private ClassRoom classRoom;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) private User teacher;
    @Column(nullable = false) private Instant createdAt = Instant.now();
    @Column(nullable = false) private Instant updatedAt = Instant.now();
    @OneToMany(mappedBy = "test", cascade = CascadeType.ALL, orphanRemoval = true) @OrderBy("position ASC, id ASC")
    private List<Question> questions = new ArrayList<>();

    @PreUpdate void touch() { updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public String getTitle() { return title; } public void setTitle(String v) { title = v; }
    public String getDescription() { return description; } public void setDescription(String v) { description = v; }
    public Language getLanguage() { return language; } public void setLanguage(Language v) { language = v; }
    public Visibility getVisibility() { return visibility; } public void setVisibility(Visibility v) { visibility = v; }
    public int getTimeLimitMinutes() { return timeLimitMinutes; } public void setTimeLimitMinutes(int v) { timeLimitMinutes = v; }
    public boolean isAllowOvertime() { return allowOvertime; } public void setAllowOvertime(boolean v) { allowOvertime = v; }
    public boolean isPublished() { return published; } public void setPublished(boolean v) { published = v; }
    public ClassRoom getClassRoom() { return classRoom; } public void setClassRoom(ClassRoom v) { classRoom = v; }
    public User getTeacher() { return teacher; } public void setTeacher(User v) { teacher = v; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<Question> getQuestions() { return questions; }
    public boolean isUrdu() { return language == Language.URDU; }
}
