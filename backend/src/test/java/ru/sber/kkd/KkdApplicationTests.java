package ru.sber.kkd;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.sber.kkd.dto.KkdSummaryResponse;
import ru.sber.kkd.exception.KkdException;
import ru.sber.kkd.repository.GoalRepository;
import ru.sber.kkd.repository.KkdRunRepository;
import ru.sber.kkd.repository.OrganizationalUnitRepository;
import ru.sber.kkd.service.KkdService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class KkdApplicationTests {

    @Autowired
    private KkdService kkdService;

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
    @DisplayName("Расчет сводки summary для прогона RUN-002 с иерархией по департаменту HQ-CIB")
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
}
