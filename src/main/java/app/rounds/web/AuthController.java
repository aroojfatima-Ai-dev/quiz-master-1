package app.rounds.web;

import app.rounds.config.AppUser;
import app.rounds.model.Role;
import app.rounds.model.User;
import app.rounds.repo.UserRepo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {
    private final UserRepo users; private final PasswordEncoder enc;
    public AuthController(UserRepo users, PasswordEncoder enc) { this.users = users; this.enc = enc; }

    @GetMapping("/")
    public String home(@AuthenticationPrincipal AppUser p) {
        if (p == null) return "redirect:/login";
        return p.getUser().getRole() == Role.TEACHER ? "redirect:/teacher" : "redirect:/student";
    }

    @GetMapping("/login")
    public String login(@AuthenticationPrincipal AppUser p) { return p != null ? "redirect:/" : "auth/login"; }

    @GetMapping("/register")
    public String register(@RequestParam(defaultValue = "STUDENT") String role, Model m) {
        m.addAttribute("role", role.equalsIgnoreCase("TEACHER") ? "TEACHER" : "STUDENT");
        return "auth/register";
    }

    @PostMapping("/register")
    public String doRegister(@RequestParam String name, @RequestParam String email, @RequestParam String password,
                             @RequestParam String role, @RequestParam(required = false) String classCode,
                             Model m, RedirectAttributes ra) {
        name = name.trim(); email = email.trim().toLowerCase();
        m.addAttribute("role", role); m.addAttribute("name", name); m.addAttribute("email", email); m.addAttribute("classCode", classCode);
        if (name.length() < 2) { m.addAttribute("error", "Please enter your full name."); return "auth/register"; }
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) { m.addAttribute("error", "That email address doesn't look right."); return "auth/register"; }
        if (password.length() < 8) { m.addAttribute("error", "Password must be at least 8 characters."); return "auth/register"; }
        if (users.existsByEmailIgnoreCase(email)) { m.addAttribute("error", "An account with that email already exists."); return "auth/register"; }
        User u = new User(); u.setName(name); u.setEmail(email); u.setPasswordHash(enc.encode(password));
        u.setRole(role.equalsIgnoreCase("TEACHER") ? Role.TEACHER : Role.STUDENT);
        users.save(u);
        ra.addFlashAttribute("notice", "Account created — sign in to continue.");
        if (u.getRole() == Role.STUDENT && classCode != null && !classCode.isBlank()) ra.addFlashAttribute("pendingCode", classCode.trim().toUpperCase());
        return "redirect:/login";
    }
}
