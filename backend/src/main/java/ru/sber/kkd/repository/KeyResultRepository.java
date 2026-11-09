package ru.sber.kkd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sber.kkd.entity.KeyResult;

import java.util.Collection;
import java.util.List;

@Repository
public interface KeyResultRepository extends JpaRepository<KeyResult, String> {
    List<KeyResult> findByGoalId(String goalId);
    List<KeyResult> findByGoalIdIn(Collection<String> goalIds);
}
