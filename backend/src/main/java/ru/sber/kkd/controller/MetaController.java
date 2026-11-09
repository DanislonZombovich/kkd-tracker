package ru.sber.kkd.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.kkd.entity.OrganizationalUnit;
import ru.sber.kkd.repository.OrganizationalUnitRepository;

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

        List<Map<String, Object>> unitList = units.stream().map(u -> Map.<String, Object>of(
                "id", u.getId(),
                "name", u.getName(),
                "level", u.getLevel(),
                "parent_id", u.getParentId() != null ? u.getParentId() : "",
                "depth", u.getDepth()
        )).toList();

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

        return Map.of(
                "units", unitList,
                "rules", rules,
                "severities", severities,
                "delta_statuses", deltaStatuses
        );
    }
}
