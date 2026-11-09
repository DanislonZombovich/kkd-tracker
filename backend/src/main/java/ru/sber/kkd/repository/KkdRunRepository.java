package ru.sber.kkd.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sber.kkd.entity.KkdRun;

import java.util.Optional;

@Repository
public interface KkdRunRepository extends JpaRepository<KkdRun, String> {
    Page<KkdRun> findAllByOrderByRunNoDesc(Pageable pageable);

    @Query("SELECT r FROM KkdRun r WHERE r.status = 'completed' ORDER BY r.runNo DESC LIMIT 1")
    Optional<KkdRun> findLatestCompleted();

    @Query("SELECT r FROM KkdRun r WHERE r.status = 'completed' AND r.runNo < :runNo ORDER BY r.runNo DESC LIMIT 1")
    Optional<KkdRun> findPreviousCompleted(Integer runNo);
}
