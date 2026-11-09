package ru.sber.kkd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sber.kkd.entity.Goal;

import java.util.Collection;
import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<Goal, String> {
    List<Goal> findByStatus(String status);
    List<Goal> findByUnitIdInAndStatus(Collection<String> unitIds, String status);
    List<Goal> findByUnitId(String unitId);
}
