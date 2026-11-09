# Design Specification: Модуль ККД (Frontend React)

## 1. Архитектура приложения
Приложение построено на базе React 18, TypeScript и дизайн-системы Ant Design 5 с использованием компонентной декомпозиции:
```
[App Container (State Coordinator)]
   │
   ├── [Header: TreeSelect (Units), Select (Runs), RunStatus]
   ├── [ErrorAlert: Graceful 409 / Network Error Handling]
   ├── [EmptyUnitAlert: Empty unit handling (TX-NEW)]
   ├── [MetricCards: Big numbers, honest delta, direction arrows, trend coloring]
   ├── [BreakdownBar: Severity filters, clickable delta status filters]
   ├── [IssuesTable: Antd Table, server pagination, combined filters, search]
   └── [IssueModal: 28px rounded modal, recommendations, run history timeline]
```

## 2. Реализация токенов дизайн-системы (UI-KIT.md)
1. **Палитра:**
   - Фон раскладки страницы: `colorBgLayout = #f7f3f0` (тёплый бежевый тон);
   - Фон карточек и контейнеров: `colorBgContainer = #fefcfa`;
   - Основной текст: `colorText = #312921`;
   - Вторичный текст: `colorTextSecondary = #76726C`;
   - Третичный текст (подписи отсутствия сравнения): `colorTextTertiary = #9d9b99`;
   - Акцентный синий: `colorPrimary = #056cfd`;
   - Границы и разделители: `colorBorder = #EAE7E5`, `colorBorderSecondary = #e8e5e2`.
2. **Типографика:**
   - Шрифт: `Inter, sans-serif`;
   - Основной текст: `14px / line-height 22px (1.5714)`;
   - Заголовки разделов: `20px / 600 / 28px`;
   - Крупные числа метрик: `28px / 600`.
3. **Геометрия и отступы:**
   - Карточки метрик: `borderRadius = 12px`;
   - Модальное окно: `borderRadius = 28px`;
   - Контролы (кнопки, инпуты): `controlHeight = 32px`;
   - Шаг отступов кратен 4px (8, 12, 16, 20, 24).
4. **Доступность (A11y):**
   - Цвет никогда не является единственным носителем информации. Статусы дельты и уровни важности обязательно дублируются иконками и текстом на русском языке.

## 3. Обработка граничных состояний
- `RUN-001` (первый прогон): при значении `previous: null` отображается нейтральная подпись «нет данных для сравнения» (не `0`).
- `RUN-006` (неуспешный прогон 409): перехват ошибки в `ErrorAlert` с понятным описанием и кнопкой возврата на последний успешный прогон.
- `TX-NEW` (пустое подразделение): рендеринг предупреждения `EmptyUnitAlert`, метрики показывают прочерк «—».
