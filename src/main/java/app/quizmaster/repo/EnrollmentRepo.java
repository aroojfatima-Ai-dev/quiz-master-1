package app.quizmaster.repo;
import app.quizmaster.model.ClassRoom;
import app.quizmaster.model.Enrollment;
import app.quizmaster.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
public interface EnrollmentRepo extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByStudentOrderByJoinedAtDesc(User student);
    List<Enrollment> findByClassRoomOrderByJoinedAtAsc(ClassRoom c);
    boolean existsByStudentAndClassRoom(User s, ClassRoom c);
    long countByClassRoom(ClassRoom c);
    @Query("select count(e) from Enrollment e where e.classRoom.teacher = ?1") long countStudentsOfTeacher(User t);
}
