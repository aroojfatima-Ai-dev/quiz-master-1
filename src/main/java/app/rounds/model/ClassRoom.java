package app.rounds.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "classes", indexes = @Index(columnList = "code", unique = true))
public class ClassRoom {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 120) private String name;
    @Column(length = 80) private String subject;
    @Column(nullable = false, length = 6, unique = true) private String code;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) private User teacher;
    @Column(nullable = false) private Instant createdAt = Instant.now();
    @OneToMany(mappedBy = "classRoom", cascade = CascadeType.ALL, orphanRemoval = true) private List<Enrollment> enrollments = new ArrayList<>();

    public Long getId() { return id; }
    public String getName() { return name; } public void setName(String v) { name = v; }
    public String getSubject() { return subject; } public void setSubject(String v) { subject = v; }
    public String getCode() { return code; } public void setCode(String v) { code = v; }
    public User getTeacher() { return teacher; } public void setTeacher(User v) { teacher = v; }
    public Instant getCreatedAt() { return createdAt; }
    public List<Enrollment> getEnrollments() { return enrollments; }
}
