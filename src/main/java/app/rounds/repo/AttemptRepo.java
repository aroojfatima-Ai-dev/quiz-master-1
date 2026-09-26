package app.rounds.repo;
import app.rounds.model.Attempt;
import app.rounds.model.Test;
import app.rounds.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
public interface AttemptRepo extends JpaRepository<Attempt, Long> {
    Optional<Attempt> findFirstByTestAndStudentAndSubmittedAtIsNull(Test t, User s);
    List<Attempt> findByStudentAndSubmittedAtIsNotNullOrderBySubmittedAtDesc(User s);
    List<Attempt> findByTestAndSubmittedAtIsNotNullOrderBySubmittedAtDesc(Test t);
    List<Attempt> findByTestAndStudentOrderByStartedAtDesc(Test t, User s);
    long countByTestAndSubmittedAtIsNotNull(Test t);
    List<Attempt> findByTest(Test t);
    @Query("select avg(a.percentage) from Attempt a where a.test = ?1 and a.submittedAt is not null") Double avgPercentage(Test t);
    @Query("select avg(a.percentage) from Attempt a where a.test.teacher = ?1 and a.submittedAt is not null") Double avgPercentageForTeacher(User t);
    @Query("select a from Attempt a where a.test.teacher = ?1 and a.submittedAt is not null order by a.submittedAt desc") List<Attempt> recentForTeacher(User t);
}
