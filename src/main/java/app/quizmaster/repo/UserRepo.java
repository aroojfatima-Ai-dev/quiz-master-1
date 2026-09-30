package app.quizmaster.repo;
import app.quizmaster.model.Role;
import app.quizmaster.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface UserRepo extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    /* ---------- admin panel ---------- */
    long countByRole(Role role);
    List<User> findAllByOrderByCreatedAtDesc();
    List<User> findByRoleOrderByCreatedAtDesc(Role role);
    List<User> findTop8ByOrderByCreatedAtDesc();
}
