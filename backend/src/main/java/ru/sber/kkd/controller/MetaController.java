package ru.sber.kkd.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.kkd.entity.OrganizationalUnit;
import ru.sber.kkd.repository.OrganizationalUnitRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/meta")
@CrossOrigin(origins = "*")
public class MetaController {
    private final OrganizationalUnitRepository unitRepository;

    public MetaController(OrganizationalUnitRepository unitRepository) {
        this.unitRepository = unitRepository;
    }

    @GetMapping
    public Map<String, Object> getMeta() {
        List<OrganizationalUnit> units = unitRepository.findAll();

        List<Map<String, Object>> unitList = units.stream().map(u -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", u.getId());
            map.put("name", u.getName());
            map.put("level", u.getLevel());
            map.put("parent_id", u.getParentId());
            map.put("depth", u.getDepth());
            return map;
        }).toList();

        List<Map<String, String>> rules = List.of(
                Map.of("code", "GOAL_NO_OWNER", "title", "Цель без ответственного", "severity", "critical"),
                Map.of("code", "GOAL_NO_KR", "title", "Цель без ключевых результатов", "severity", "critical"),
                Map.of("code", "KR_WEIGHTS_SUM", "title", "Сумма весов КР не равна 100%", "severity", "major"),
                Map.of("code", "CASCADE_NO_PARENT", "title", "Цель не связана с вышестоящей", "severity", "major"),
                Map.of("code", "KR_NO_TARGET", "title", "КР без целевого значения", "severity", "major"),
                Map.of("code", "KR_STALE_PROGRESS", "title", "Прогресс КР давно не обновлялся", "severity", "minor")
        );

        List<Map<String, Object>> severities = List.of(
                Map.of("code", "critical", "title", "Критичная", "order", 0),
                Map.of("code", "major", "title", "Существенная", "order", 1),
                Map.of("code", "minor", "title", "Незначительная", "order", 2)
        );

        List<Map<String, String>> deltaStatuses = List.of(
                Map.of("code", "new", "title", "Новая"),
                Map.of("code", "persisting", "title", "Сохраняется"),
                Map.of("code", "resolved", "title", "Исправлена")
        );

        List<Map<String, Object>> metrics = List.of(
                Map.of("key", "clean_goals_share", "title", "Доля корректных целей", "unit", "%", "higher_is_better", true, "formula", "активные цели без проблем ÷ все активные цели × 100"),
                Map.of("key", "cascade_coverage", "title", "Покрытие каскадом", "unit", "%", "higher_is_better", true, "formula", "цели с корректной связью с вышестоящей ÷ все не-корневые цели × 100"),
                Map.of("key", "issues_total", "title", "Найдено проблем", "unit", "шт.", "higher_is_better", false, "formula", "общее количество нарушений правил ККД"),
                Map.of("key", "critical_issues", "title", "Критичные проблемы", "unit", "шт.", "higher_is_better", false, "formula", "количество нарушений с severity = critical")
        );

        List<Map<String, String>> goalLevels = List.of(
                Map.of("code", "block", "title", "Цель блока"),
                Map.of("code", "hq", "title", "Цель департамента"),
                Map.of("code", "unit", "title", "Цель подразделения"),
                Map.of("code", "employee", "title", "Цель сотрудника")
        );

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("units", unitList);
        response.put("rules", rules);
        response.put("severities", severities);
        response.put("goal_levels", goalLevels);
        response.put("metrics", metrics);
        response.put("delta_statuses", deltaStatuses);

        return response;
    }
}
