package app.rounds.repo;
import app.rounds.model.Question;
import app.rounds.model.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
public interface QuestionRepo extends JpaRepository<Question, Long> {
    List<Question> findByTestOrderByPositionAscIdAsc(Test t);
    int countByTest(Test t);
    @Query("select count(q) from Question q where q.test.teacher = ?1") long countByTeacher(app.rounds.model.User t);
}
