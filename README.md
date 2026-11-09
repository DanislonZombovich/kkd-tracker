# Модуль контроля качества данных (ККД) по целям сотрудников

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot 3.3.5](https://img.shields.io/badge/Spring_Boot-3.3.5-brightgreen.svg?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![React 18](https://img.shields.io/badge/React-18-blue.svg?logo=react&logoColor=white)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.6-blue.svg?logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![PostgreSQL 16](https://img.shields.io/badge/PostgreSQL-16-blue.svg?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Liquibase](https://img.shields.io/badge/Liquibase-Migrate-red.svg?logo=liquibase&logoColor=white)](https://www.liquibase.org/)
[![Ant Design 5](https://img.shields.io/badge/Ant_Design-5.21-0170FE.svg?logo=antdesign&logoColor=white)](https://ant.design/)
[![Docker Compose](https://img.shields.io/badge/Docker-Compose-2496ED.svg?logo=docker&logoColor=white)](https://www.docker.com/)

Комплексное решение тестового задания по проекту **«Автотрекинг целей B2B+»** (автоматизация процедуры контроля качества данных по целям и ключевым результатам сотрудников) по всем 4 проектным направлениям:
1. **BA (Бизнес-анализ)**
2. **SA (Системный анализ)**
3. **Backend (Java 21 / Spring Boot 3 / PostgreSQL / Liquibase)**
4. **Frontend (React 18 / TypeScript / Ant Design 5 / Корпоративный UI-Kit)**

---

## Оглавление
- [Архитектура решения](#архитектура-решения)
- [Структура репозитория](#структура-репозитория)
- [1. Направление: Бизнес-анализ (BA)](#1-направление-бизнес-анализ-ba)
- [2. Направление: Системный анализ (SA)](#2-направление-системный-анализ-sa)
- [3. Направление: Backend (Java 21 / Spring Boot 3)](#3-направление-backend-java-21--spring-boot-3)
- [4. Направление: Frontend (React 18 / Ant Design 5)](#4-направление-frontend-react-18--ant-design-5)
- [Быстрый старт (Docker Compose)](#быстрый-старт-docker-compose)
- [Локальный запуск и разработка](#локальный-запуск-и-разработка)
- [Проверка условий приёмки](#проверка-условий-приёмки)

---

## Архитектура решения

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Frontend (React 18 SPA)                         │
│   • Ant Design 5 (Corporate UI-Kit tokens: Inter, 28px card radius)   │
│   • Селектор прогона и оргструктуры, карточки метрик с трендами        │
│   • Интерактивная плашка статусов дельты (new / persisting / resolved) │
│   • Таблица замечаний с фильтрами и модальным окном деталей            │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ HTTP / JSON (Nginx proxy / Vite)
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                    Backend (Java 21 / Spring Boot 3)                   │
│   • REST API: /api/runs, /api/meta, /api/kkd/summary, /api/kkd/issues  │
│   • KkdRuleEngine: изолированные валидаторы бизнес-правил              │
│   • HierarchyService: рекурсивный расчет метрик по оргдереву           │
│   • DeltaService: сопоставление прогонов по SHA-1 fingerprint          │
│   • Единый формат ошибок: {"error": {"code": "...", "message": "..."}} │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ Spring Data JPA / Liquibase
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                         PostgreSQL 16 Database                         │
│   • organizational_units (дерево с parent_id и depth)                  │
│   • goals (цели со статусами active/closed/cancelled и уровнями)       │
│   • key_results (целевые/факт значения, веса, даты обновления)         │
│   • kkd_rules, kkd_runs, kkd_issues (журнал прогонов и дефектов)       │
│   • Все миграции Liquibase поддерживают накат -> откат -> накат        │
└────────────────────────────────────────────────────────────────────────┘
```

---

## Структура репозитория

```
kkd_tracker/
├── ba/                                    # Бизнес-анализ
│   ├── Тестовое_задание_BA_решение.md     # Полный текст решения с таблицами, Gherkin и процессами
│   └── Тестовое_задание_BA_решение.pdf    # Экспортированный отчет в формате PDF
├── sa/                                    # Системный анализ
│   ├── Тестовое_задание_SA_решение.md     # ER-модель, SQL-правила, CTE-агрегация, OpenAPI-спецификация
│   └── Тестовое_задание_SA_решение.pdf    # Экспортированный отчет в формате PDF
├── backend/                               # Микросервис на Java 21 / Spring Boot 3
│   ├── pom.xml
│   ├── Dockerfile
│   ├── specs/                             # Спецификации requirements.md, design.md
│   ├── src/main/java/ru/sber/kkd/         # Исходный код сервиса ККД
│   │   ├── config/                        # CORS и Spring Web конфигурация
│   │   ├── controller/                    # REST-контроллеры (/api/runs, /api/meta, /api/kkd/*)
│   │   ├── dto/                           # Строгие DTO запросов/ответов
│   │   ├── entity/                        # JPA-сущности модели целей и ККД
│   │   ├── exception/                     # Глобальный обработчик исключений
│   │   ├── repository/                    # Spring Data JPA репозитории
│   │   ├── rules/                         # Изолированный движок правил KkdRuleEngine
│   │   └── service/                       # Иерархия подразделений, дельта и метрики
│   ├── src/main/resources/
│   │   ├── application.yml                # Профиль PostgreSQL / Prod
│   │   └── db/changelog/                  # Liquibase миграции с секциями <rollback>
│   └── src/test/                          # 22 теста (JUnit 5): правила, метрики, дельта, откат Liquibase
├── frontend/                              # SPA на React 18 / TypeScript / Ant Design 5
│   ├── package.json
│   ├── Dockerfile
│   ├── nginx.conf                         # Reverse-proxy и SPA-роутинг
│   ├── specs/                             # Спецификации интерфейса
│   ├── src/
│   │   ├── api/                           # Axios-клиент с обработкой 409/404/422/500
│   │   ├── components/                    # Header, MetricCards, BreakdownBar, IssuesTable, IssueModal, Alert
│   │   ├── theme.ts                       # Корпоративная тема UI-KIT (токены Ant Design)
│   │   ├── types/                         # Интерфейсы TypeScript
│   │   ├── App.tsx                        # Главный экран модуля ККД
│   │   └── main.tsx                       # Точка входа приложения
├── mock_api/                              # Эталонный FastAPI Mock API с документацией и UI-KIT
├── specs/                                 # Общепроектные спецификации
│   ├── requirements.md                    # Требования к модулю ККД
│   └── design.md                          # Архитектурный дизайн-документ
├── docker-compose.yml                     # Docker Compose для развертывания полного стека
└── README.md                              # Главная документация проекта
```

---

## 1. Направление: Бизнес-анализ (BA)
Материалы размещены в каталоге [`ba/`](./ba/):
- **[`Тестовое_задание_BA_решение.pdf`](./ba/Тестовое_задание_BA_решение.pdf)** (и исходный Markdown):
  1. **10 ключевых вопросов к стейкхолдерам:** блок бизнес-целей, блок правил ККД, блок методологии метрик, блок регламента исправлений и эскалаций.
  2. **Каталог бизнес-правил:**
     - 4 базовых: `GOAL_NO_OWNER` (Critical), `GOAL_NO_KR` (Critical), `KR_WEIGHTS_SUM` (Major), `CASCADE_NO_PARENT` (Major).
     - 2 расширенных: `KR_NO_TARGET` (Major), `KR_STALE_PROGRESS` (Minor, >45 дней без обновления).
  3. **3 ключевые метрики качества данных:**
     - *Clean Goals Share* (% целей без дефектов, рост = лучше).
     - *Cascade Coverage* (% каскадированных целей, рост = лучше).
     - *Critical Issues Count* (число критических замечаний, снижение = лучше).
  4. **Целевой процесс TO BE:** BPMN-описание ночного ETL-среза, еженедельной валидации и 4-уровневого регламента эскалации (L1 Автор → L2 Руководитель → L3 C-Level → L4 Блокировка премирования).
  5. **Критерии приёмки (Acceptance Criteria):** 4 сценария на языке **Gherkin** (`RUN-001` первый прогон, `RUN-002` расчет дельты, `RUN-006` обработка ошибки 409, `TX-NEW` пустое подразделение).
  6. **Мотивация:** Профессиональный интерес к прозрачности целеполагания в B2B и продуктовой культуре данных.

---

## 2. Направление: Системный анализ (SA)
Материалы размещены в каталоге [`sa/`](./sa/):
- **[`Тестовое_задание_SA_решение.pdf`](./sa/Тестовое_задание_SA_решение.pdf)** (и исходный Markdown):
  1. **Реляционная модель PostgreSQL:** 6 нормализованных сущностей (`organizational_units`, `goals`, `key_results`, `kkd_rules`, `kkd_runs`, `kkd_issues`), первичные и внешние ключи, check-констрейнты, оптимизирующие индексы.
  2. **Формализация правил в SQL:** точные селекты выявления дефектов для каждого правила с защитой от деления на ноль и NULL.
  3. **Алгоритм иерархической агрегации:** рекурсивный Common Table Expression (CTE) для обхода поддерева оргструктуры вниз от выбранного узла.
  4. **Алгоритм расчета дельты:**
     - Стабильный хеш дефекта: `fingerprint = SHA1(rule_code || ':' || object_id)`.
     - Статусы: `NEW` (появилась), `PERSISTING` (сохраняется с прошлых прогонов), `RESOLVED` (была в прошлом прогоне, отсутствует в текущем).
     - Для первого прогона (`RUN-001`) дельта строго `null` (не ноль!).
  5. **Спецификация REST API (OpenAPI 3.0 / YAML):** эндпоинты `/api/runs`, `/api/meta`, `/api/kkd/summary`, `/api/kkd/issues`, `/api/kkd/issues/{id}` с примерами ответов и единым форматом ошибок.

---

## 3. Направление: Backend (Java 21 / Spring Boot 3)
Расположен в [`backend/`](./backend/):
- **Стек:** Java 21, Spring Boot 3.3.5, Spring Data JPA, Liquibase, PostgreSQL 16, H2 (для тестов), Lombok, Maven.
- **Liquibase миграции:**
  - `001-init-schema.xml`: DDL схемы БД с полными блоками `<rollback>`.
  - `002-seed-data.xml`: датасет (5 подразделений, 24 цели, 29 КР, 6 правил, 2 прогона с дефектами) со строгим `<rollback>`.
- **Изолированный Rule Engine:**
  - `KkdRuleEngine` принимает `KkdExecutionContext` и выполняет набор независимых правил (`KkdRule`).
- **REST Контроллеры:**
  - `GET /api/runs` — список запусков ККД с агрегированными счетчиками.
  - `GET /api/meta` — справочник подразделений с родительскими связями.
  - `GET /api/kkd/summary?run_id=...&unit_id=...` — сводные метрики, динамика трендов и разбивка дельты.
  - `GET /api/kkd/issues` — пагинированный список проблем с фильтрацией по правилу, критичности, статусу дельты.
  - `GET /api/kkd/issues/{id}` — карточка проблемы с историей по прогонам.
- **Тесты (22 теста JUnit 5, 100% pass):**
  - Юнит-тесты изолированных правил (`KrWeightsSumRuleTest`, `GoalNoOwnerRuleTest`, `CascadeNoParentRuleTest`).
  - Тесты метрик и обхода иерархии (`MetricServiceTest`).
  - Тесты сопоставления дельты (`DeltaServiceTest`).
  - Тест миграций: **накат → откат → повторный накат** (`LiquibaseRollbackTest`).

---

## 4. Направление: Frontend (React 18 / Ant Design 5)
Расположен в [`frontend/`](./frontend/):
- **Стек:** React 18, TypeScript 5.6, Ant Design 5.21, Axios, Vite 5.
- **Корпоративный стиль (`UI-KIT.md`):**
  - Палитра: фон страницы `#f7f3f0`, карточки `#fefcfa`, акцент `#056cfd`, ошибки `#d43629`, успех `#039306`.
  - Типографика: Inter, скругления `borderRadius = 28px`, мягкие тени `0 2px 8px rgba(49, 41, 33, 0.06)`.
- **Компоненты:**
  - `Header`: выбор прогона и подразделения с отображением дерева подчиненности.
  - `MetricCards`: карточки метрик с честным отображением дельты (поддержка `higher_is_better`, тег «Базовый срез» для `RUN-001`).
  - `BreakdownBar`: интерактивная шкала распределения статусов дельты (клик фильтрует таблицу).
  - `IssuesTable`: таблица замечаний с поиском, фильтрами и серверной пагинацией.
  - `IssueModal`: модальное окно деталей с таймлайном истории прогонов.
  - `ErrorAlert` и `EmptyUnitAlert`: обработка ошибки 409 (`RUN-006`) и пустого подразделения (`TX-NEW`).

---

## Быстрый старт (Docker Compose)

Запуск полного стенда (PostgreSQL 16 + Java Backend + React Frontend):

```bash
# Клонировать репозиторий
git clone https://github.com/DanislonZombovich/kkd-tracker.git
cd kkd-tracker

# Запустить сервисы
docker compose up --build -d

# Проверить статус контейнеров
docker compose ps
```

### Доступ к сервисам:
- **Frontend веб-интерфейс:** [http://localhost:3000](http://localhost:3000)
- **Backend REST API:** [http://localhost:8080/api/runs](http://localhost:8080/api/runs)
- **PostgreSQL:** `localhost:5432` (`kkd_db`, user: `kkd_user`, pass: `kkd_secret`)

---

## Локальный запуск и разработка

### 1. Запуск базы данных
```bash
docker run -d --name kkd-postgres -p 5432:5432 \
  -e POSTGRES_DB=kkd_db \
  -e POSTGRES_USER=kkd_user \
  -e POSTGRES_PASSWORD=kkd_secret \
  postgres:16-alpine
```

### 2. Запуск Backend
```bash
cd backend

# Прогон всех 22 тестов (включая откат Liquibase)
mvn clean test

# Сборка и запуск JAR
mvn clean package -DskipTests
java -jar target/kkd-tracker-backend-1.0.0.jar
```

### 3. Запуск Frontend
```bash
cd frontend

npm install
npm run build
npm run dev
```

---

## Проверка условий приёмки

| Условие приёмки | Статус | Подтверждение реализации |
| :--- | :---: | :--- |
| **1. Код оформлен по требованиям команды** | ✅ | Модульная архитектура, DTO, единый формат ошибок `{"error": {"code", "message"}}`, спецификации `requirements.md` и `design.md`. |
| **2. Java сервис корректно собирается под ИФТ** | ✅ | Компиляция под Java 21, все 22 теста пройдены без ошибок, многоэтапный `Dockerfile`. |
| **3. Скрипты БД: накат – откат – повторный накат** | ✅ | Все миграции Liquibase содержат блоки `<rollback>`. Автоматический тест `LiquibaseRollbackTest.java` проходит на 100%. |
| **4. Реализован интерфейс по Storybook / UI-Kit** | ✅ | Точное совпадение палитры, шрифтов Inter, радиусов 28px, шкалы распределения и модального окна. |
| **5. Реализация метрик соответствует требованиям** | ✅ | Иерархический рекурсивный расчет метрик, честная дельта с null-handling, учет трендов. |

---

## Автор
**Даниил Егоров** ([@DanislonZombovich](https://github.com/DanislonZombovich))  
Email: da.egorov.net@gmail.com
