# Design Specification: Модуль ККД (Backend Java)

## 1. Архитектура сервиса
Сервис построен по принципам слоистой архитектуры (Layered Architecture):
```
[REST Controllers & GlobalExceptionHandler]
                    │
                    ▼
[Service Layer: KkdService, MetricService, DeltaService, HierarchyService]
                    │
                    ├─────────► [Domain Rules Engine: KkdRuleEngine, KkdRule implementations]
                    ▼
[Repository Layer: Spring Data JPA Repositories]
                    │
                    ▼
[PostgreSQL Database / Liquibase Schema]
```

## 2. Ключевые архитектурные решения
1. **Изоляция движка правил (`KkdRuleEngine`):**
   - Интерфейс `KkdRule` определяет контракт правила: `evaluate(activeGoals, goalKeyResults, allGoalsMap, snapshotDate)`.
   - Правила являются Spring-бинами, реализующими стратегию (Strategy Pattern).
   - Движок правил не имеет прямых зависимостей от `EntityManager`, HTTP-запросов или репозиториев, что гарантирует 100% тестируемость и изоляцию.
2. **Алгоритм дедупликации проблем (Fingerprint & Issue ID):**
   - Отпечаток: `fingerprint = rule_code + ":" + object_id` (например `GOAL_NO_OWNER:GOAL-005` или `KR_NO_TARGET:KR-011`).
   - Идентификатор дефекта детерминирован: `ISS-` + SHA1(fingerprint)[:10].
   - Позволяет сопоставлять дефекты между любыми прогонами без суррогатных ссылок.
3. **Иерархическая агрегация:**
   - Построение графа подразделений через `HierarchyService.getDescendantsOrSelf()`.
   - Выборка целей и нарушений по множеству `IN :scopeUnitIds` за один запрос.
4. **Обработка граничных состояний:**
   - Пустое подразделение (`TX-NEW`): метрики корректно возвращают `null` вместо деления на ноль.
   - Первый прогон (`RUN-001`): `compared_to_run_id` отсутствует, `previous` и `delta` возвращают `null`.
   - Неуспешный прогон (`status='failed'`): возвращает `HTTP 409 RUN_FAILED`.

## 3. Схема базы данных
- `organizational_units` (id, name, level, parent_id, depth)
- `goals` (id, title, level, unit_id, owner_id, owner_name, parent_id, status, deadline)
- `key_results` (id, goal_id, title, target_value, fact_value, metric_unit, weight, progress_updated_at)
- `kkd_rules` (code, object_type, severity, title, description, recommendation)
- `kkd_runs` (id, run_no, snapshot_date, status, compared_to_run_id, started_at, finished_at)
- `kkd_issues` (run_id, id, fingerprint, rule_code, severity, object_type, object_id, goal_id, kr_id, unit_id, owner_id, owner_name, details, actual_value, expected_value, recommendation, delta_status, runs_in_row)
