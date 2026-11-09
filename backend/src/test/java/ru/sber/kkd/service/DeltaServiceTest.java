package ru.sber.kkd.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.kkd.dto.DeltaSummaryDto;
import ru.sber.kkd.entity.KkdIssueEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeltaServiceTest {

    private final DeltaService deltaService = new DeltaService();

    @Test
    @DisplayName("Первый прогон: счётчики new, persisting, resolved равны null")
    void shouldReturnNullWhenNoComparisonRun() {
        List<KkdIssueEntity> curIssues = List.of(
                KkdIssueEntity.builder().id("I1").fingerprint("GOAL_NO_OWNER:G1").build()
        );

        DeltaSummaryDto delta = deltaService.calculateDelta(curIssues, null, false);

        assertNull(delta.getNewCount());
        assertNull(delta.getPersisting());
        assertNull(delta.getResolved());
    }

    @Test
    @DisplayName("Корректная классификация new, persisting и resolved проблем")
    void shouldCorrectlyClassifyIssues() {
        // Run 1 had issues: A and B
        List<KkdIssueEntity> prevIssues = List.of(
                KkdIssueEntity.builder().id("I1").fingerprint("GOAL_NO_OWNER:G1").build(),
                KkdIssueEntity.builder().id("I2").fingerprint("KR_WEIGHTS_SUM:G2").build()
        );

        // Run 2 has issues: A (still there) and C (new). B is fixed!
        List<KkdIssueEntity> curIssues = List.of(
                KkdIssueEntity.builder().id("I1").fingerprint("GOAL_NO_OWNER:G1").build(),
                KkdIssueEntity.builder().id("I3").fingerprint("CASCADE_NO_PARENT:G3").build()
        );

        DeltaSummaryDto delta = deltaService.calculateDelta(curIssues, prevIssues, true);

        assertEquals(1, delta.getNewCount(), "Одна новая проблема C");
        assertEquals(1, delta.getPersisting(), "Одна сохраняющаяся проблема A");
        assertEquals(1, delta.getResolved(), "Одна исправленная проблема B");
    }
}
