package app.rounds.config;

import app.rounds.model.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@ControllerAdvice
public class GlobalModel {
    @ModelAttribute("me")
    public User me(@AuthenticationPrincipal AppUser p) { return p == null ? null : p.getUser(); }

    @ModelAttribute("todayLabel")
    public String today() { return LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy")); }
}
