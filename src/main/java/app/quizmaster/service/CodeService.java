package app.quizmaster.service;

import app.quizmaster.repo.ClassRepo;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;

@Service
public class CodeService {
    // no 0/O/1/I to avoid classroom confusion
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private final SecureRandom rnd = new SecureRandom();
    private final ClassRepo classes;
    public CodeService(ClassRepo classes) { this.classes = classes; }

    public String uniqueClassCode() {
        String c;
        do { c = random(6); } while (classes.existsByCode(c));
        return c;
    }
    private String random(int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append(ALPHABET.charAt(rnd.nextInt(ALPHABET.length())));
        return sb.toString();
    }
}
