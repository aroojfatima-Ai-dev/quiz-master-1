package app.quizmaster.repo;
import app.quizmaster.model.Answer;
import app.quizmaster.model.Attempt;
import app.quizmaster.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface AnswerRepo extends JpaRepository<Answer, Long> {
    Optional<Answer> findByAttemptAndQuestion(Attempt a, Question q);
    List<Answer> findByAttempt(Attempt a);
}
