package app.rounds.model;

import jakarta.persistence.*;

@Entity @Table(name = "answers", uniqueConstraints = @UniqueConstraint(columnNames = {"attempt_id", "question_id"}))
public class Answer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) private Attempt attempt;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) private Question question;
    /** null = skipped */
    @Column(length = 1) private String selected;
    @Column(nullable = false) private boolean correct;

    public Long getId() { return id; }
    public Attempt getAttempt() { return attempt; } public void setAttempt(Attempt v) { attempt = v; }
    public Question getQuestion() { return question; } public void setQuestion(Question v) { question = v; }
    public String getSelected() { return selected; } public void setSelected(String v) { selected = v; }
    public boolean isCorrect() { return correct; } public void setCorrect(boolean v) { correct = v; }
}
