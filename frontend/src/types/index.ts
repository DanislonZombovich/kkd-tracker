export interface Unit {
  id: string;
  name: string;
  level: string;
  parent_id: string | null;
  depth: number;
}

export interface Run {
  id: string;
  run_no: number;
  status: 'running' | 'completed' | 'failed';
  snapshot_date: string;
  compared_to_run_id: string | null;
  started_at?: string;
  finished_at?: string;
  goals_total?: number;
  issues_total?: number;
  critical_issues?: number;
}

export interface MetricItem {
  key: string;
  title: string;
  unit: string;
  value: number | null;
  previous: number | null;
  delta: number | null;
  delta_pct?: number | null;
  trend?: 'better' | 'worse' | 'same' | null;
  higher_is_better?: boolean | null;
  formula?: string;
}

export interface SeverityCount {
  code: string;
  title: string;
  current: number;
  previous: number | null;
  delta: number | null;
}

export interface DeltaCount {
  new: number | null;
  persisting: number | null;
  resolved: number | null;
}

export interface SummaryResponse {
  run: {
    id: string;
    status: string;
    snapshot_date: string;
    compared_to_run_id?: string | null;
  };
  compared_to: {
    id: string;
    status?: string;
    snapshot_date: string;
  } | null;
  unit: {
    id: string;
    name: string;
    level: string;
    parent_id?: string | null;
    path: Array<{ id: string; name: string }>;
  };
  metrics: MetricItem[];
  issues_by_severity: SeverityCount[];
  delta: DeltaCount;
}

export interface IssueHistory {
  run_id: string;
  snapshot_date: string;
  run_status: string;
  present: boolean | null;
}

export interface Issue {
  id: string;
  fingerprint: string;
  rule_code: string;
  rule_title: string;
  severity: 'critical' | 'major' | 'minor';
  object_type: 'goal' | 'key_result';
  object_id: string;
  goal_id: string;
  goal_title: string;
  kr_id?: string | null;
  kr_title?: string | null;
  unit_id: string;
  unit_name: string;
  owner?: { id: string; full_name: string } | null;
  details: string;
  actual?: string | null;
  expected?: string | null;
  recommendation: string;
  delta_status: 'new' | 'persisting' | 'resolved';
  runs_in_row: number;
  history?: IssueHistory[];
}

export interface IssuesResponse {
  run_id: string;
  items: Issue[];
  total: number;
  limit: number;
  offset: number;
}

export interface MetaData {
  units: Unit[];
  rules: Array<{ code: string; title: string; severity: string; object_type?: string }>;
  severities: Array<{ code: string; title: string; order: number }>;
  delta_statuses: Array<{ code: string; title: string }>;
}
