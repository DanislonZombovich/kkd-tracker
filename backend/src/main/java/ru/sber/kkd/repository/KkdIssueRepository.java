package ru.sber.kkd.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.sber.kkd.entity.KkdIssueEntity;
import ru.sber.kkd.entity.KkdIssueId;

import java.util.Collection;
import java.util.List;

@Repository
public interface KkdIssueRepository extends JpaRepository<KkdIssueEntity, KkdIssueId> {
    List<KkdIssueEntity> findByRunId(String runId);

    List<KkdIssueEntity> findByRunIdAndUnitIdIn(String runId, Collection<String> unitIds);

    @Query("SELECT i FROM KkdIssueEntity i WHERE i.runId = :runId " +
           "AND (:unitIds IS NULL OR i.unitId IN :unitIds) " +
           "AND (:ruleCodes IS NULL OR i.ruleCode IN :ruleCodes) " +
           "AND (:severities IS NULL OR i.severity IN :severities) " +
           "AND (:deltaStatuses IS NULL OR i.deltaStatus IN :deltaStatuses) " +
           "AND (:q IS NULL OR (" +
           "  LOWER(i.goalId) LIKE :q OR " +
           "  LOWER(i.objectId) LIKE :q OR " +
           "  (i.krId IS NOT NULL AND LOWER(i.krId) LIKE :q) OR " +
           "  (i.ownerName IS NOT NULL AND LOWER(i.ownerName) LIKE :q) OR " +
           "  LOWER(i.details) LIKE :q" +
           "))")
    Page<KkdIssueEntity> findWithFilters(
            @Param("runId") String runId,
            @Param("unitIds") Collection<String> unitIds,
            @Param("ruleCodes") Collection<String> ruleCodes,
            @Param("severities") Collection<String> severities,
            @Param("deltaStatuses") Collection<String> deltaStatuses,
            @Param("q") String q,
            Pageable pageable
    );

    @Query("SELECT i FROM KkdIssueEntity i WHERE i.id = :id")
    List<KkdIssueEntity> findAllByIssueId(@Param("id") String id);
}
