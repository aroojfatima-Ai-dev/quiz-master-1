package app.rounds.repo;
import app.rounds.model.Answer;
import app.rounds.model.Attempt;
import app.rounds.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface AnswerRepo extends JpaRepository<Answer, Long> {
    Optional<Answer> findByAttemptAndQuestion(Attempt a, Question q);
    List<Answer> findByAttempt(Attempt a);
}
