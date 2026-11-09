package ru.sber.kkd;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.sber.kkd.controller.MetaController;
import ru.sber.kkd.dto.IssueItemDto;
import ru.sber.kkd.dto.KkdIssuesResponse;
import ru.sber.kkd.dto.KkdSummaryResponse;
import ru.sber.kkd.exception.KkdException;
import ru.sber.kkd.repository.GoalRepository;
import ru.sber.kkd.repository.KkdRunRepository;
import ru.sber.kkd.repository.OrganizationalUnitRepository;
import ru.sber.kkd.service.KkdService;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class KkdApplicationTests {

    @Autowired
    private KkdService kkdService;

    @Autowired
    private MetaController metaController;

    @Autowired
    private OrganizationalUnitRepository unitRepository;

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private KkdRunRepository runRepository;

    @Test
    @DisplayName("Контекст Spring Boot успешно поднимается и Liquibase накатывает стартовые данные")
    void contextLoadsAndDataSeeded() {
        assertTrue(unitRepository.count() >= 5, "Подразделения должны быть загружены");
        assertTrue(goalRepository.count() >= 20, "Стартовый набор целей (20-30 шт.) должен быть загружен");
        assertEquals(2, runRepository.count(), "Должно быть 2 прогона (RUN-001 и RUN-002)");
    }

    @Test
    @DisplayName("Расчет сводки summary для прогона RUN-002 с иерархией по департаменту HQ-CIB и issues_by_rule")
    void testSummaryForHqCib() {
        KkdSummaryResponse summary = kkdService.getSummary("RUN-002", "HQ-CIB");

        assertNotNull(summary);
        assertEquals("RUN-002", summary.getRun().getId());
        assertNotNull(summary.getComparedTo());
        assertEquals("RUN-001", summary.getComparedTo().getId());
        assertEquals("HQ-CIB", summary.getUnit().getId());

        assertNotNull(summary.getMetrics());
        assertFalse(summary.getMetrics().isEmpty());

        assertNotNull(summary.getDelta());
        assertNotNull(summary.getDelta().getNewCount());
        assertNotNull(summary.getDelta().getPersisting());

        // Проверка issues_by_rule
        assertNotNull(summary.getIssuesByRule(), "issues_by_rule должен присутствовать в сводке");
        assertFalse(summary.getIssuesByRule().isEmpty(), "issues_by_rule не должен быть пустым");
        assertTrue(summary.getIssuesByRule().stream().anyMatch(r -> "GOAL_NO_OWNER".equals(r.getCode())));
    }

    @Test
    @DisplayName("Первый прогон RUN-001 не имеет сравнения (comparedTo == null, delta содержит null)")
    void testSummaryForFirstRun() {
        KkdSummaryResponse summary = kkdService.getSummary("RUN-001", "B2B");

        assertNotNull(summary);
        assertEquals("RUN-001", summary.getRun().getId());
        assertNull(summary.getComparedTo(), "У первого прогона нет базы сравнения");
        assertNull(summary.getDelta().getNewCount());
        assertNull(summary.getDelta().getPersisting());
        assertNull(summary.getDelta().getResolved());
    }

    @Test
    @DisplayName("Обращение к несуществующему подразделению выбрасывает 404 UNIT_NOT_FOUND")
    void testUnknownUnitThrows404() {
        KkdException ex = assertThrows(KkdException.class, () ->
                kkdService.getSummary("RUN-002", "NON_EXISTENT_UNIT")
        );
        assertEquals(404, ex.getStatus().value());
        assertEquals("UNIT_NOT_FOUND", ex.getCode());
    }

    @Test
    @DisplayName("Карточка проблемы getIssueById возвращает полную информацию и историю по прогонам")
    void testGetIssueById() {
        // ISS-E3A190BC44 - GOAL_NO_OWNER на GOAL-005
        IssueItemDto issue = kkdService.getIssueById("ISS-E3A190BC44", "RUN-002");

        assertNotNull(issue);
        assertEquals("ISS-E3A190BC44", issue.getId());
        assertEquals("GOAL_NO_OWNER", issue.getRuleCode());
        assertEquals("GOAL-005", issue.getGoalId());
        assertEquals("critical", issue.getSeverity());
        assertNotNull(issue.getRecommendation());

        // Проверка истории по прогонам
        assertNotNull(issue.getHistory());
        assertEquals(2, issue.getHistory().size(), "В истории должны быть прогоны RUN-001 и RUN-002");
        assertTrue(issue.getHistory().get(0).getPresent(), "В RUN-001 проблема присутствовала");
        assertTrue(issue.getHistory().get(1).getPresent(), "В RUN-002 проблема сохраняется");
    }

    @Test
    @DisplayName("getIssues по умолчанию исключает resolved проблемы и отдает только активные (new, persisting)")
    void testGetIssuesExcludesResolvedByDefault() {
        KkdIssuesResponse response = kkdService.getIssues("RUN-002", "B2B", null, null, null, null, "id", 50, 0);

        assertNotNull(response);
        assertNotNull(response.getItems());
        // В RUN-002 активных проблем 5 (4 persisting + 1 new), а 1 проблема resolved (GOAL-016)
        assertEquals(5, response.getTotal(), "Всего активных проблем должно быть 5");
        assertTrue(response.getItems().stream().noneMatch(i -> "resolved".equalsIgnoreCase(i.getDeltaStatus())),
                "В списке по умолчанию не должно быть resolved проблем!");
    }

    @Test
    @DisplayName("getIssues при фильтре delta_status=resolved возвращает только исправленные проблемы")
    void testGetIssuesWithResolvedFilter() {
        KkdIssuesResponse response = kkdService.getIssues("RUN-002", "B2B", null, null, "resolved", null, "id", 50, 0);

        assertNotNull(response);
        assertEquals(1, response.getTotal(), "Должна быть ровно 1 исправленная проблема (GOAL-016)");
        assertEquals("resolved", response.getItems().get(0).getDeltaStatus());
        assertEquals("GOAL-016", response.getItems().get(0).getGoalId());
    }

    @Test
    @DisplayName("getIssues с параметром поиска q фильтрует результаты по цели и сотруднику")
    void testGetIssuesWithSearchQuery() {
        // Поиск по ID цели
        KkdIssuesResponse resGoal = kkdService.getIssues("RUN-002", "B2B", null, null, null, "GOAL-005", "id", 50, 0);
        assertEquals(1, resGoal.getTotal());
        assertEquals("GOAL-005", resGoal.getItems().get(0).getGoalId());

        // Поиск по несуществующей строке
        KkdIssuesResponse resNone = kkdService.getIssues("RUN-002", "B2B", null, null, null, "UNKNOWN_NON_EXISTENT", "id", 50, 0);
        assertEquals(0, resNone.getTotal());
        assertTrue(resNone.getItems().isEmpty());
    }

    @Test
    @DisplayName("Метаданные /api/meta возвращают null parent_id для корневого подразделения и полные справочники")
    @SuppressWarnings("unchecked")
    void testMetaEndpoint() {
        Map<String, Object> meta = metaController.getMeta();
        assertNotNull(meta);

        List<Map<String, Object>> units = (List<Map<String, Object>>) meta.get("units");
        assertNotNull(units);
        Map<String, Object> rootUnit = units.stream()
                .filter(u -> "B2B".equals(u.get("id")))
                .findFirst()
                .orElseThrow();
        assertNull(rootUnit.get("parent_id"), "parent_id корневого узла B2B должен быть строго null!");

        assertTrue(meta.containsKey("rules"));
        assertTrue(meta.containsKey("severities"));
        assertTrue(meta.containsKey("goal_levels"));
        assertTrue(meta.containsKey("metrics"));
        assertTrue(meta.containsKey("delta_statuses"));
    }
}
