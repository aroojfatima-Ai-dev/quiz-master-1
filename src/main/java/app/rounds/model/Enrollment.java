package app.rounds.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "enrollments", uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "class_room_id"}))
public class Enrollment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) private User student;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) private ClassRoom classRoom;
    @Column(nullable = false) private Instant joinedAt = Instant.now();

    public Long getId() { return id; }
    public User getStudent() { return student; } public void setStudent(User v) { student = v; }
    public ClassRoom getClassRoom() { return classRoom; } public void setClassRoom(ClassRoom v) { classRoom = v; }
    public Instant getJoinedAt() { return joinedAt; }
}
