package app.rounds.model;

import jakarta.persistence.*;

@Entity @Table(name = "questions")
public class Question {
    public enum Difficulty { EASY, MEDIUM, HARD }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) private Test test;
    @Column(nullable = false) private int position;
    @Column(nullable = false, length = 2000) private String prompt;
    @Column(nullable = false, length = 500) private String optionA;
    @Column(nullable = false, length = 500) private String optionB;
    @Column(nullable = false, length = 500) private String optionC;
    @Column(nullable = false, length = 500) private String optionD;
    /** 'A' | 'B' | 'C' | 'D' */
    @Column(nullable = false, length = 1) private String correct;
    @Column(length = 80) private String topic;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 10) private Difficulty difficulty = Difficulty.MEDIUM;

    public Long getId() { return id; }
    public Test getTest() { return test; } public void setTest(Test v) { test = v; }
    public int getPosition() { return position; } public void setPosition(int v) { position = v; }
    public String getPrompt() { return prompt; } public void setPrompt(String v) { prompt = v; }
    public String getOptionA() { return optionA; } public void setOptionA(String v) { optionA = v; }
    public String getOptionB() { return optionB; } public void setOptionB(String v) { optionB = v; }
    public String getOptionC() { return optionC; } public void setOptionC(String v) { optionC = v; }
    public String getOptionD() { return optionD; } public void setOptionD(String v) { optionD = v; }
    public String getCorrect() { return correct; } public void setCorrect(String v) { correct = v; }
    public String getTopic() { return topic; } public void setTopic(String v) { topic = v; }
    public Difficulty getDifficulty() { return difficulty; } public void setDifficulty(Difficulty v) { difficulty = v; }

    public String option(String letter) {
        return switch (letter == null ? "" : letter) { case "A" -> optionA; case "B" -> optionB; case "C" -> optionC; case "D" -> optionD; default -> null; };
    }
    public String getCorrectText() { return option(correct); }
}
