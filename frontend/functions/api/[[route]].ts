// Cloudflare Pages Function serving edge mock API for KKD Tracker demo

interface Env {}

const UNITS = [
  { id: 'B2B', name: 'Корпоративный блок', level: 'block', parent_id: null, depth: 0 },
  { id: 'HQ-CIB', name: 'Департамент корпоративного бизнеса', level: 'hq', parent_id: 'B2B', depth: 1 },
  { id: 'CIB-KEY', name: 'Управление по работе с ключевыми клиентами', level: 'unit', parent_id: 'HQ-CIB', depth: 2 },
  { id: 'CIB-REG', name: 'Управление региональных продаж', level: 'unit', parent_id: 'HQ-CIB', depth: 2 },
  { id: 'HQ-SME', name: 'Департамент малого и среднего бизнеса', level: 'hq', parent_id: 'B2B', depth: 1 },
  { id: 'HQ-TX', name: 'Департамент транзакционного бизнеса', level: 'hq', parent_id: 'B2B', depth: 1 },
  { id: 'TX-NEW', name: 'Отдел платёжных сервисов (Новый)', level: 'unit', parent_id: 'HQ-TX', depth: 2 },
];

const RULES = [
  { code: 'GOAL_NO_OWNER', title: 'Цель без ответственного', severity: 'critical', object_type: 'goal' },
  { code: 'GOAL_NO_KR', title: 'Цель без ключевых результатов', severity: 'critical', object_type: 'goal' },
  { code: 'KR_WEIGHTS_SUM', title: 'Сумма весов КР не равна 100%', severity: 'major', object_type: 'goal' },
  { code: 'CASCADE_NO_PARENT', title: 'Цель не связана с вышестоящей', severity: 'major', object_type: 'goal' },
  { code: 'KR_NO_TARGET', title: 'КР без целевого значения', severity: 'major', object_type: 'key_result' },
  { code: 'KR_STALE_PROGRESS', title: 'Прогресс КР давно не обновлялся (>45 дн.)', severity: 'minor', object_type: 'key_result' },
];

const SEVERITIES = [
  { code: 'critical', title: 'Критичная', order: 0 },
  { code: 'major', title: 'Существенная', order: 1 },
  { code: 'minor', title: 'Незначительная', order: 2 },
];

const DELTA_STATUSES = [
  { code: 'new', title: 'Новая' },
  { code: 'persisting', title: 'Сохраняется' },
  { code: 'resolved', title: 'Устранена' },
];

const RUNS = [
  {
    id: 'RUN-002',
    run_no: 2,
    status: 'completed',
    snapshot_date: '2026-10-26',
    compared_to_run_id: 'RUN-001',
    started_at: '2026-10-26T03:00:00Z',
    finished_at: '2026-10-26T03:04:12Z',
    goals_total: 24,
    issues_total: 7,
    critical_issues: 2,
  },
  {
    id: 'RUN-001',
    run_no: 1,
    status: 'completed',
    snapshot_date: '2026-10-19',
    compared_to_run_id: null,
    started_at: '2026-10-19T03:00:00Z',
    finished_at: '2026-10-19T03:05:00Z',
    goals_total: 22,
    issues_total: 9,
    critical_issues: 3,
  },
  {
    id: 'RUN-006',
    run_no: 6,
    status: 'failed',
    snapshot_date: '2026-11-02',
    compared_to_run_id: 'RUN-002',
    started_at: '2026-11-02T03:00:00Z',
    finished_at: '2026-11-02T03:01:10Z',
    goals_total: 0,
    issues_total: 0,
    critical_issues: 0,
  },
];

const ISSUES = [
  {
    id: 'ISSUE-001',
    fingerprint: 'fp-g-003-owner',
    rule_code: 'GOAL_NO_OWNER',
    rule_title: 'Цель без ответственного',
    severity: 'critical',
    object_type: 'goal',
    object_id: 'GOAL-003',
    goal_id: 'GOAL-003',
    goal_title: 'Рост доли цифровых продаж в B2B',
    unit_id: 'B2B',
    unit_name: 'Корпоративный блок',
    owner: null,
    details: 'У активной цели не назначен ответственный сотрудник',
    actual: 'Ответственный отсутствует (null)',
    expected: 'Назначен ответственный сотрудник',
    recommendation: 'Назначить владельца цели в модуле «Целеполагание»',
    delta_status: 'persisting',
    runs_in_row: 2,
    history: [
      { run_id: 'RUN-001', snapshot_date: '2026-10-19', run_status: 'completed', present: true },
      { run_id: 'RUN-002', snapshot_date: '2026-10-26', run_status: 'completed', present: true },
    ],
  },
  {
    id: 'ISSUE-002',
    fingerprint: 'fp-g-008-kr',
    rule_code: 'GOAL_NO_KR',
    rule_title: 'Цель без ключевых результатов',
    severity: 'critical',
    object_type: 'goal',
    object_id: 'GOAL-008',
    goal_id: 'GOAL-008',
    goal_title: 'Масштабирование цифрового факторинга',
    unit_id: 'HQ-CIB',
    unit_name: 'Департамент корпоративного бизнеса',
    owner: { id: 'EMP-104', full_name: 'Дмитрий Смирнов' },
    details: 'Активная цель не содержит ни одного измеримого ключевого результата',
    actual: '0 ключевых результатов',
    expected: 'От 1 до 5 ключевых результатов',
    recommendation: 'Сформировать измеримые КР с целевыми метриками',
    delta_status: 'new',
    runs_in_row: 1,
    history: [
      { run_id: 'RUN-001', snapshot_date: '2026-10-19', run_status: 'completed', present: false },
      { run_id: 'RUN-002', snapshot_date: '2026-10-26', run_status: 'completed', present: true },
    ],
  },
  {
    id: 'ISSUE-003',
    fingerprint: 'fp-g-005-weights',
    rule_code: 'KR_WEIGHTS_SUM',
    rule_title: 'Сумма весов КР не равна 100%',
    severity: 'major',
    object_type: 'goal',
    object_id: 'GOAL-005',
    goal_id: 'GOAL-005',
    goal_title: 'Привлечение новых корпоративных клиентов в сегменте CIB',
    unit_id: 'HQ-CIB',
    unit_name: 'Департамент корпоративного бизнеса',
    owner: { id: 'EMP-102', full_name: 'Мария Васильева' },
    details: 'Сумма весов ключевых результатов цели составляет 85% вместо 100%',
    actual: '85.0%',
    expected: '100.0%',
    recommendation: 'Скорректировать веса ключевых результатов до суммы в 100%',
    delta_status: 'persisting',
    runs_in_row: 2,
    history: [
      { run_id: 'RUN-001', snapshot_date: '2026-10-19', run_status: 'completed', present: true },
      { run_id: 'RUN-002', snapshot_date: '2026-10-26', run_status: 'completed', present: true },
    ],
  },
  {
    id: 'ISSUE-004',
    fingerprint: 'fp-g-012-parent',
    rule_code: 'CASCADE_NO_PARENT',
    rule_title: 'Цель не связана с вышестоящей',
    severity: 'major',
    object_type: 'goal',
    object_id: 'GOAL-012',
    goal_id: 'GOAL-012',
    goal_title: 'Запуск региональных продаж в ПФО',
    unit_id: 'CIB-REG',
    unit_name: 'Управление региональных продаж',
    owner: { id: 'EMP-205', full_name: 'Алексей Кузнецов' },
    details: 'Цель уровня подразделения (unit) не привязана к цели вышестоящего департамента',
    actual: 'parent_id is null',
    expected: 'Ссылка на цель департамента HQ-CIB',
    recommendation: 'Указать вышестоящую родительскую цель в структуре каскада',
    delta_status: 'new',
    runs_in_row: 1,
    history: [
      { run_id: 'RUN-001', snapshot_date: '2026-10-19', run_status: 'completed', present: false },
      { run_id: 'RUN-002', snapshot_date: '2026-10-26', run_status: 'completed', present: true },
    ],
  },
  {
    id: 'ISSUE-005',
    fingerprint: 'fp-kr-019-stale',
    rule_code: 'KR_STALE_PROGRESS',
    rule_title: 'Прогресс КР давно не обновлялся (>45 дн.)',
    severity: 'minor',
    object_type: 'key_result',
    object_id: 'KR-019',
    goal_id: 'GOAL-010',
    goal_title: 'Повышение операционной эффективности обслуживания в CIB',
    kr_id: 'KR-019',
    kr_title: 'Сокращение среднего времени проведения сделки до 3 дней',
    unit_id: 'HQ-CIB',
    unit_name: 'Департамент корпоративного бизнеса',
    owner: { id: 'EMP-105', full_name: 'Ольга Морозова' },
    details: 'Фактическое значение КР не обновлялось более 52 дней (последнее обновление: 2026-09-04)',
    actual: '52 дня с даты последнего обновления',
    expected: 'Не более 45 дней',
    recommendation: 'Внести актуальное фактическое значение прогресса в систему',
    delta_status: 'persisting',
    runs_in_row: 2,
    history: [
      { run_id: 'RUN-001', snapshot_date: '2026-10-19', run_status: 'completed', present: true },
      { run_id: 'RUN-002', snapshot_date: '2026-10-26', run_status: 'completed', present: true },
    ],
  },
  {
    id: 'ISSUE-006',
    fingerprint: 'fp-kr-022-target',
    rule_code: 'KR_NO_TARGET',
    rule_title: 'КР без целевого значения',
    severity: 'major',
    object_type: 'key_result',
    object_id: 'KR-022',
    goal_id: 'GOAL-015',
    goal_title: 'Развитие партнерской сети в сегменте малого бизнеса',
    kr_id: 'KR-022',
    kr_title: 'Число подписанных соглашений о сотрудничестве',
    unit_id: 'HQ-SME',
    unit_name: 'Департамент малого и среднего бизнеса',
    owner: { id: 'EMP-301', full_name: 'Сергей Попов' },
    details: 'У ключевого результата не задано числовое целевое значение',
    actual: 'target_value = null',
    expected: 'Числовое значение > 0',
    recommendation: 'Задать целевой показатель в карточке ключевого результата',
    delta_status: 'persisting',
    runs_in_row: 2,
    history: [
      { run_id: 'RUN-001', snapshot_date: '2026-10-19', run_status: 'completed', present: true },
      { run_id: 'RUN-002', snapshot_date: '2026-10-26', run_status: 'completed', present: true },
    ],
  },
  {
    id: 'ISSUE-007',
    fingerprint: 'fp-g-002-owner',
    rule_code: 'GOAL_NO_OWNER',
    rule_title: 'Цель без ответственного',
    severity: 'critical',
    object_type: 'goal',
    object_id: 'GOAL-002',
    goal_id: 'GOAL-002',
    goal_title: 'Увеличение активной базы клиентов малого бизнеса',
    unit_id: 'HQ-SME',
    unit_name: 'Департамент малого и среднего бизнеса',
    owner: null,
    details: 'Ответственное лицо не назначено',
    actual: 'owner_id is null',
    expected: 'Назначен ответственный сотрудник',
    recommendation: 'Назначить владельца цели',
    delta_status: 'persisting',
    runs_in_row: 2,
    history: [
      { run_id: 'RUN-001', snapshot_date: '2026-10-19', run_status: 'completed', present: true },
      { run_id: 'RUN-002', snapshot_date: '2026-10-26', run_status: 'completed', present: true },
    ],
  },
  {
    id: 'ISSUE-RESOLVED-001',
    fingerprint: 'fp-kr-001-resolved',
    rule_code: 'KR_WEIGHTS_SUM',
    rule_title: 'Сумма весов КР не равна 100%',
    severity: 'major',
    object_type: 'goal',
    object_id: 'GOAL-001',
    goal_id: 'GOAL-001',
    goal_title: 'Рост комиссионного дохода корпоративного блока',
    unit_id: 'B2B',
    unit_name: 'Корпоративный блок',
    owner: { id: 'EMP-001', full_name: 'Иван Петров' },
    details: 'Веса КР были исправлены до 100% (замечание устранено)',
    actual: '100.0%',
    expected: '100.0%',
    recommendation: 'Замечание успешно закрыто',
    delta_status: 'resolved',
    runs_in_row: 0,
    history: [
      { run_id: 'RUN-001', snapshot_date: '2026-10-19', run_status: 'completed', present: true },
      { run_id: 'RUN-002', snapshot_date: '2026-10-26', run_status: 'completed', present: false },
    ],
  },
];

export async function onRequest(context: { request: Request }): Promise<Response> {
  const url = new URL(context.request.url);
  const path = url.pathname;

  const json = (data: unknown, status = 200) =>
    new Response(JSON.stringify(data), {
      status,
      headers: {
        'Content-Type': 'application/json; charset=utf-8',
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
        'Access-Control-Allow-Headers': '*',
      },
    });

  if (context.request.method === 'OPTIONS') {
    return new Response(null, {
      status: 204,
      headers: {
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
        'Access-Control-Allow-Headers': '*',
      },
    });
  }

  // 1. /api/meta
  if (path === '/api/meta') {
    return json({
      units: UNITS,
      rules: RULES,
      severities: SEVERITIES,
      delta_statuses: DELTA_STATUSES,
    });
  }

  // 2. /api/runs
  if (path === '/api/runs') {
    return json({
      items: RUNS,
      total: RUNS.length,
    });
  }

  // 3. /api/kkd/summary
  if (path === '/api/kkd/summary') {
    const runId = url.searchParams.get('run_id') || 'RUN-002';
    const unitId = url.searchParams.get('unit_id') || 'B2B';

    // Edge Case: Failed Run RUN-006 -> 409
    if (runId === 'RUN-006') {
      return json(
        {
          error: {
            code: 'RUN_FAILED',
            message: 'Прогон RUN-006 завершился с ошибкой расчета. Данные недоступны.',
          },
        },
        409
      );
    }

    const currentUnit = UNITS.find((u) => u.id === unitId) || UNITS[0];

    // Edge Case: Empty Unit TX-NEW -> null metrics
    if (unitId === 'TX-NEW') {
      return json({
        run: { id: runId, status: 'completed', snapshot_date: '2026-10-26', compared_to_run_id: 'RUN-001' },
        compared_to: { id: 'RUN-001', status: 'completed', snapshot_date: '2026-10-19' },
        unit: {
          id: 'TX-NEW',
          name: 'Отдел платёжных сервисов (Новый)',
          level: 'unit',
          parent_id: 'HQ-TX',
          path: [
            { id: 'B2B', name: 'Корпоративный блок' },
            { id: 'HQ-TX', name: 'Департамент транзакционного бизнеса' },
            { id: 'TX-NEW', name: 'Отдел платёжных сервисов (Новый)' },
          ],
        },
        metrics: [
          { key: 'clean_goals_share', title: 'Доля корректных целей', unit: '%', value: null, previous: null, delta: null, trend: null, higher_is_better: true },
          { key: 'cascade_coverage', title: 'Охват каскадированием', unit: '%', value: null, previous: null, delta: null, trend: null, higher_is_better: true },
          { key: 'critical_issues', title: 'Критические проблемы', unit: 'шт.', value: 0, previous: null, delta: null, trend: null, higher_is_better: false },
          { key: 'issues_total', title: 'Всего проблем', unit: 'шт.', value: 0, previous: null, delta: null, trend: null, higher_is_better: false },
        ],
        issues_by_severity: [
          { code: 'critical', title: 'Критичная', current: 0, previous: null, delta: null },
          { code: 'major', title: 'Существенная', current: 0, previous: null, delta: null },
          { code: 'minor', title: 'Незначительная', current: 0, previous: null, delta: null },
        ],
        delta: { new: 0, persisting: 0, resolved: 0 },
      });
    }

    // Edge Case: First Run RUN-001 -> no previous, no delta
    if (runId === 'RUN-001') {
      return json({
        run: { id: 'RUN-001', status: 'completed', snapshot_date: '2026-10-19', compared_to_run_id: null },
        compared_to: null,
        unit: {
          id: currentUnit.id,
          name: currentUnit.name,
          level: currentUnit.level,
          parent_id: currentUnit.parent_id,
          path: [{ id: currentUnit.id, name: currentUnit.name }],
        },
        metrics: [
          { key: 'clean_goals_share', title: 'Доля корректных целей', unit: '%', value: 59.1, previous: null, delta: null, trend: null, higher_is_better: true },
          { key: 'cascade_coverage', title: 'Охват каскадированием', unit: '%', value: 72.7, previous: null, delta: null, trend: null, higher_is_better: true },
          { key: 'critical_issues', title: 'Критические проблемы', unit: 'шт.', value: 3, previous: null, delta: null, trend: null, higher_is_better: false },
          { key: 'issues_total', title: 'Всего проблем', unit: 'шт.', value: 9, previous: null, delta: null, trend: null, higher_is_better: false },
        ],
        issues_by_severity: [
          { code: 'critical', title: 'Критичная', current: 3, previous: null, delta: null },
          { code: 'major', title: 'Существенная', current: 4, previous: null, delta: null },
          { code: 'minor', title: 'Незначительная', current: 2, previous: null, delta: null },
        ],
        delta: { new: null, persisting: null, resolved: null },
      });
    }

    // Standard completed run RUN-002
    return json({
      run: { id: 'RUN-002', status: 'completed', snapshot_date: '2026-10-26', compared_to_run_id: 'RUN-001' },
      compared_to: { id: 'RUN-001', status: 'completed', snapshot_date: '2026-10-19' },
      unit: {
        id: currentUnit.id,
        name: currentUnit.name,
        level: currentUnit.level,
        parent_id: currentUnit.parent_id,
        path: [{ id: 'B2B', name: 'Корпоративный блок' }, { id: currentUnit.id, name: currentUnit.name }],
      },
      metrics: [
        { key: 'clean_goals_share', title: 'Доля корректных целей', unit: '%', value: 70.8, previous: 59.1, delta: 11.7, delta_pct: 19.8, trend: 'better', higher_is_better: true },
        { key: 'cascade_coverage', title: 'Охват каскадированием', unit: '%', value: 83.3, previous: 72.7, delta: 10.6, delta_pct: 14.6, trend: 'better', higher_is_better: true },
        { key: 'critical_issues', title: 'Критические проблемы', unit: 'шт.', value: 2, previous: 3, delta: -1, delta_pct: -33.3, trend: 'better', higher_is_better: false },
        { key: 'issues_total', title: 'Всего проблем', unit: 'шт.', value: 7, previous: 9, delta: -2, delta_pct: -22.2, trend: 'better', higher_is_better: false },
      ],
      issues_by_severity: [
        { code: 'critical', title: 'Критичная', current: 2, previous: 3, delta: -1 },
        { code: 'major', title: 'Существенная', current: 4, previous: 4, delta: 0 },
        { code: 'minor', title: 'Незначительная', current: 1, previous: 2, delta: -1 },
      ],
      delta: { new: 2, persisting: 5, resolved: 2 },
    });
  }

  // 4. /api/kkd/issues/:id
  const issueMatch = path.match(/^\/api\/kkd\/issues\/([^/]+)$/);
  if (issueMatch) {
    const issueId = issueMatch[1];
    const issue = ISSUES.find((i) => i.id === issueId) || ISSUES[0];
    return json(issue);
  }

  // 5. /api/kkd/issues
  if (path === '/api/kkd/issues') {
    const ruleCode = url.searchParams.get('rule_code');
    const severity = url.searchParams.get('severity');
    const deltaStatus = url.searchParams.get('delta_status');
    const q = url.searchParams.get('q');
    const limit = parseInt(url.searchParams.get('limit') || '20', 10);
    const offset = parseInt(url.searchParams.get('offset') || '0', 10);

    let filtered = [...ISSUES];
    if (ruleCode) {
      const codes = ruleCode.split(',');
      filtered = filtered.filter((i) => codes.includes(i.rule_code));
    }
    if (severity) {
      const sevs = severity.split(',');
      filtered = filtered.filter((i) => sevs.includes(i.severity));
    }
    if (deltaStatus) {
      const statuses = deltaStatus.split(',');
      filtered = filtered.filter((i) => statuses.includes(i.delta_status));
    }
    if (q) {
      const lower = q.toLowerCase();
      filtered = filtered.filter(
        (i) =>
          i.goal_title.toLowerCase().includes(lower) ||
          i.rule_title.toLowerCase().includes(lower) ||
          i.details.toLowerCase().includes(lower) ||
          i.recommendation.toLowerCase().includes(lower)
      );
    }

    const items = filtered.slice(offset, offset + limit);
    return json({
      run_id: url.searchParams.get('run_id') || 'RUN-002',
      items,
      total: filtered.length,
      limit,
      offset,
    });
  }

  return json({ error: { code: 'NOT_FOUND', message: 'Эндпоинт не найден' } }, 404);
}
