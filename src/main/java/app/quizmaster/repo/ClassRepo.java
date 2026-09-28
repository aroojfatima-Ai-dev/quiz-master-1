package app.quizmaster.repo;
import app.quizmaster.model.ClassRoom;
import app.quizmaster.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface ClassRepo extends JpaRepository<ClassRoom, Long> {
    List<ClassRoom> findByTeacherOrderByCreatedAtDesc(User teacher);
    Optional<ClassRoom> findByCodeIgnoreCase(String code);
    boolean existsByCode(String code);
    long countByTeacher(User teacher);
}
