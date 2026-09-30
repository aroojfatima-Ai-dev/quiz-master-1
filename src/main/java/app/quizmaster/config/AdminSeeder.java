package app.quizmaster.config;

import app.quizmaster.model.Role;
import app.quizmaster.model.User;
import app.quizmaster.repo.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates one ADMIN account on first boot so the admin panel can be reached.
 *
 * Credentials come from app.admin.email / app.admin.password
 * (env: APP_ADMIN_EMAIL / APP_ADMIN_PASSWORD). Seeding is a no-op when an ADMIN
 * already exists; set app.admin.seed=false to switch it off entirely.
 */
@Component
public class AdminSeeder implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);
    private static final String DEFAULT_PASSWORD = "Admin@12345";

    private final UserRepo users;
    private final PasswordEncoder encoder;

    @Value("${app.admin.seed:true}") private boolean seed;
    @Value("${app.admin.email:admin@quizmaster.app}") private String email;
    @Value("${app.admin.password:Admin@12345}") private String password;

    public AdminSeeder(UserRepo users, PasswordEncoder encoder) { this.users = users; this.encoder = encoder; }

    @Override
    public void run(ApplicationArguments args) {
        if (!seed) { log.info("Admin bootstrap disabled (app.admin.seed=false)."); return; }

        String mail = email == null ? "" : email.trim().toLowerCase();
        if (mail.isBlank() || password == null || password.length() < 8) {
            log.warn("Admin bootstrap skipped: set APP_ADMIN_EMAIL and an APP_ADMIN_PASSWORD of at least 8 characters.");
            return;
        }
        if (users.countByRole(Role.ADMIN) > 0) { log.info("An ADMIN account already exists — skipping bootstrap."); return; }
        if (users.existsByEmailIgnoreCase(mail)) {
            log.warn("Cannot create the initial admin: {} is already taken by a non-admin account. Set APP_ADMIN_EMAIL to a free address.", mail);
            return;
        }

        User admin = new User();
        admin.setName("Platform Admin");
        admin.setEmail(mail);
        admin.setPasswordHash(encoder.encode(password));
        admin.setRole(Role.ADMIN);
        users.save(admin);

        if (DEFAULT_PASSWORD.equals(password)) {
            log.warn("Initial ADMIN account created → {} with the built-in default password. Set APP_ADMIN_PASSWORD before exposing this deployment.", mail);
        } else {
            log.info("Initial ADMIN account created → {} (password from APP_ADMIN_PASSWORD).", mail);
        }
    }
}
