package app.quizmaster.repo;
import app.quizmaster.model.Test;
import app.quizmaster.model.ClassRoom;
import app.quizmaster.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
public interface TestRepo extends JpaRepository<Test, Long> {
    List<Test> findByTeacherOrderByUpdatedAtDesc(User t);
    List<Test> findByClassRoomOrderByUpdatedAtDesc(ClassRoom c);
    long countByTeacher(User t);
    @Query("select t from Test t where t.published = true and (t.visibility = app.quizmaster.model.Test$Visibility.PUBLIC or t.classRoom in ?1) order by t.updatedAt desc")
    List<Test> findAvailableForStudent(List<ClassRoom> classes);
    @Query("select t from Test t where t.published = true and t.visibility = app.quizmaster.model.Test$Visibility.PUBLIC order by t.updatedAt desc")
    List<Test> findPublic();
    /* ---------- admin panel ---------- */
    List<Test> findAllByOrderByUpdatedAtDesc();
    List<Test> findByTeacher(User teacher);
    long countByClassRoom(ClassRoom c);
    long countByPublishedTrue();
}
