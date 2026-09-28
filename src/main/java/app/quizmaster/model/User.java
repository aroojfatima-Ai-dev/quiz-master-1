package app.quizmaster.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "users", indexes = @Index(columnList = "email", unique = true))
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 120) private String name;
    @Column(nullable = false, length = 160, unique = true) private String email;
    @Column(nullable = false) private String passwordHash;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Role role;
    @Column(nullable = false) private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public String getName() { return name; } public void setName(String v) { name = v; }
    public String getEmail() { return email; } public void setEmail(String v) { email = v; }
    public String getPasswordHash() { return passwordHash; } public void setPasswordHash(String v) { passwordHash = v; }
    public Role getRole() { return role; } public void setRole(Role v) { role = v; }
    public Instant getCreatedAt() { return createdAt; }
    public String getInitials() {
        String[] p = name.trim().split("\\s+");
        String s = "" + Character.toUpperCase(p[0].charAt(0));
        if (p.length > 1) s += Character.toUpperCase(p[p.length - 1].charAt(0));
        return s;
    }
}
