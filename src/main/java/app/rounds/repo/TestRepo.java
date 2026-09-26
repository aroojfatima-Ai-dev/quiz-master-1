package app.rounds.repo;
import app.rounds.model.ClassRoom;
import app.rounds.model.Test;
import app.rounds.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
public interface TestRepo extends JpaRepository<Test, Long> {
    List<Test> findByTeacherOrderByUpdatedAtDesc(User t);
    List<Test> findByClassRoomOrderByUpdatedAtDesc(ClassRoom c);
    long countByTeacher(User t);
    @Query("select t from Test t where t.published = true and (t.visibility = app.rounds.model.Test$Visibility.PUBLIC or t.classRoom in ?1) order by t.updatedAt desc")
    List<Test> findAvailableForStudent(List<ClassRoom> classes);
    @Query("select t from Test t where t.published = true and t.visibility = app.rounds.model.Test$Visibility.PUBLIC order by t.updatedAt desc")
    List<Test> findPublic();
}
