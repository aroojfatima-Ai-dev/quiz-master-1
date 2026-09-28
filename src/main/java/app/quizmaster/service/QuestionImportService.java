package app.quizmaster.service;

import app.quizmaster.model.Question;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses questions out of PDF / DOCX / TXT. Expected block format (blank line between questions):
 *
 *   1. What is the capital of Pakistan?
 *   A) Lahore
 *   B) Karachi
 *   C) Islamabad
 *   D) Quetta
 *   Answer: C
 *   Topic: Geography          (optional)
 *   Difficulty: Easy          (optional)
 *
 * Option markers accepted: A) A. (A) A: a) etc. Urdu text is fine (UTF-8).
 */
@Service
public class QuestionImportService {

    private static final Pattern OPT = Pattern.compile("^\\(?([A-Da-d])[\\)\\.:\\-]\\s*(.+)$");
    private static final Pattern ANS = Pattern.compile("^(?:answer|ans|correct|درست جواب|جواب)\\s*[:\\-=]?\\s*\\(?([A-Da-d])\\)?\\s*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern TOPIC = Pattern.compile("^(?:topic|موضوع)\\s*[:\\-=]\\s*(.+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern DIFF = Pattern.compile("^(?:difficulty|level|مشکل)\\s*[:\\-=]\\s*(easy|medium|hard|آسان|درمیانہ|مشکل)\\s*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern QNUM = Pattern.compile("^(?:q(?:uestion)?\\s*)?\\d+\\s*[\\)\\.:\\-]\\s*(.+)$", Pattern.CASE_INSENSITIVE);

    public String extractText(MultipartFile file) throws IOException {
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        try (InputStream in = file.getInputStream()) {
            if (name.endsWith(".pdf")) {
                try (PDDocument doc = PDDocument.load(in)) { return new PDFTextStripper().getText(doc); }
            } else if (name.endsWith(".docx")) {
                try (XWPFDocument doc = new XWPFDocument(in); XWPFWordExtractor ex = new XWPFWordExtractor(doc)) { return ex.getText(); }
            } else {
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
    }

    public List<Question> parse(String text) {
        List<Question> out = new ArrayList<>();
        if (text == null) return out;
        String[] lines = text.replace("\r", "").split("\n");
        Draft d = null;
        for (String raw : lines) {
            String line = raw.strip();
            if (line.isEmpty()) continue;
            Matcher m;
            if ((m = OPT.matcher(line)).matches() && d != null) {
                d.set(m.group(1).toUpperCase(Locale.ROOT), m.group(2).strip());
            } else if ((m = ANS.matcher(line)).matches() && d != null) {
                d.correct = m.group(1).toUpperCase(Locale.ROOT);
            } else if ((m = TOPIC.matcher(line)).matches() && d != null) {
                d.topic = m.group(1).strip();
            } else if ((m = DIFF.matcher(line)).matches() && d != null) {
                d.difficulty = mapDifficulty(m.group(1));
            } else {
                // a new question starts when we hit a non-option line and the current draft is complete or absent
                if (d == null || d.isComplete() || d.hasOptions()) {
                    if (d != null && d.isComplete()) out.add(d.build());
                    d = new Draft();
                    m = QNUM.matcher(line);
                    d.prompt = m.matches() ? m.group(1).strip() : line;
                } else {
                    d.prompt = d.prompt + " " + line; // multi-line prompt
                }
            }
        }
        if (d != null && d.isComplete()) out.add(d.build());
        return out;
    }

    private Question.Difficulty mapDifficulty(String s) {
        s = s.toLowerCase(Locale.ROOT);
        if (s.startsWith("e") || s.equals("آسان")) return Question.Difficulty.EASY;
        if (s.startsWith("h") || s.equals("مشکل")) return Question.Difficulty.HARD;
        return Question.Difficulty.MEDIUM;
    }

    private static class Draft {
        String prompt, a, b, c, dd, correct, topic; Question.Difficulty difficulty = Question.Difficulty.MEDIUM;
        void set(String k, String v) { switch (k) { case "A" -> a = v; case "B" -> b = v; case "C" -> c = v; case "D" -> dd = v; } }
        boolean hasOptions() { return a != null || b != null || c != null || dd != null; }
        boolean isComplete() { return prompt != null && a != null && b != null && c != null && dd != null && correct != null; }
        Question build() {
            Question q = new Question();
            q.setPrompt(prompt); q.setOptionA(a); q.setOptionB(b); q.setOptionC(c); q.setOptionD(dd);
            q.setCorrect(correct); q.setTopic(topic); q.setDifficulty(difficulty);
            return q;
        }
    }
}
