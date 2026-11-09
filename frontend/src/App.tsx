import React, { useEffect, useState, useCallback } from 'react';
import { Layout, Spin, message } from 'antd';
import { kkdApi } from './api/client';
import { Header } from './components/Header';
import { MetricCards } from './components/MetricCards';
import { BreakdownBar } from './components/BreakdownBar';
import { IssuesTable } from './components/IssuesTable';
import { IssueModal } from './components/IssueModal';
import { ErrorAlert } from './components/ErrorAlert';
import { EmptyUnitAlert } from './components/EmptyUnitAlert';
import type { Run, Unit, MetaData, SummaryResponse, Issue } from './types';

const { Content } = Layout;

export const App: React.FC = () => {
  // Metadata & Options
  const [meta, setMeta] = useState<MetaData | null>(null);
  const [runs, setRuns] = useState<Run[]>([]);
  const [units, setUnits] = useState<Unit[]>([]);

  // Selection
  const [selectedRunId, setSelectedRunId] = useState<string>('latest');
  const [selectedUnitId, setSelectedUnitId] = useState<string>('B2B');

  // Summary State
  const [summary, setSummary] = useState<SummaryResponse | null>(null);
  const [loadingSummary, setLoadingSummary] = useState<boolean>(true);
  const [summaryError, setSummaryError] = useState<{ code?: string; message?: string } | null>(null);

  // Issues Table State
  const [issues, setIssues] = useState<Issue[]>([]);
  const [totalIssues, setTotalIssues] = useState<number>(0);
  const [loadingIssues, setLoadingIssues] = useState<boolean>(false);
  const [page, setPage] = useState<number>(1);
  const [pageSize, setPageSize] = useState<number>(20);

  // Filters
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [selectedRule, setSelectedRule] = useState<string | null>(null);
  const [selectedSeverity, setSelectedSeverity] = useState<string | null>(null);
  const [selectedDeltaStatus, setSelectedDeltaStatus] = useState<string | null>(null);

  // Modal State
  const [modalIssue, setModalIssue] = useState<Issue | null>(null);
  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);

  // 1. Initial Load of Meta and Runs
  useEffect(() => {
    const init = async () => {
      try {
        const [metaData, runsData] = await Promise.all([kkdApi.getMeta(), kkdApi.getRuns(50, 0)]);
        setMeta(metaData);
        setUnits(metaData.units);
        setRuns(runsData.items);
        if (runsData.items.length > 0 && selectedRunId === 'latest') {
          // If latest is selected, default to latest completed run
          const latestCompleted = runsData.items.find((r) => r.status === 'completed') || runsData.items[0];
          setSelectedRunId(latestCompleted.id);
        }
      } catch (err: any) {
        console.error('Failed to initialize metadata', err);
        message.error('Ошибка загрузки метаданных сервиса');
      }
    };
    init();
  }, []);

  // 2. Fetch Summary when Run or Unit changes
  const loadSummary = useCallback(async () => {
    if (!selectedRunId || !selectedUnitId) return;
    setLoadingSummary(true);
    setSummaryError(null);
    try {
      const data = await kkdApi.getSummary(selectedRunId, selectedUnitId);
      setSummary(data);
    } catch (err: any) {
      const errData = err.response?.data?.error;
      setSummaryError(
        errData || {
          code: 'FETCH_ERROR',
          message: err.message || 'Ошибка загрузки сводки ККД',
        }
      );
      setSummary(null);
    } finally {
      setLoadingSummary(false);
    }
  }, [selectedRunId, selectedUnitId]);

  useEffect(() => {
    loadSummary();
  }, [loadSummary]);

  // 3. Fetch Issues when Run, Unit, Pagination, or Filters change
  const loadIssues = useCallback(async () => {
    if (!selectedRunId || !selectedUnitId) return;
    setLoadingIssues(true);
    try {
      const offset = (page - 1) * pageSize;
      const data = await kkdApi.getIssues(selectedRunId, selectedUnitId, {
        limit: pageSize,
        offset,
        rule_code: selectedRule || undefined,
        severity: selectedSeverity || undefined,
        delta_status: selectedDeltaStatus || undefined,
        q: searchQuery.trim() || undefined,
      });
      setIssues(data.items);
      setTotalIssues(data.total);
    } catch (err: any) {
      console.warn('Issues fetch notice:', err.message);
      setIssues([]);
      setTotalIssues(0);
    } finally {
      setLoadingIssues(false);
    }
  }, [selectedRunId, selectedUnitId, page, pageSize, selectedRule, selectedSeverity, selectedDeltaStatus, searchQuery]);

  useEffect(() => {
    if (!summaryError) {
      loadIssues();
    }
  }, [loadIssues, summaryError]);

  // Handlers
  const handleRunChange = (runId: string) => {
    setSelectedRunId(runId);
    setPage(1);
  };

  const handleUnitChange = (unitId: string) => {
    setSelectedUnitId(unitId);
    setPage(1);
  };

  const handlePageChange = (newPage: number, newPageSize: number) => {
    setPage(newPage);
    setPageSize(newPageSize);
  };

  const handleSelectIssue = async (issue: Issue) => {
    try {
      const detailed = await kkdApi.getIssue(issue.id, selectedRunId);
      setModalIssue(detailed);
    } catch {
      setModalIssue(issue);
    }
    setIsModalOpen(true);
  };

  // Check if unit is empty (e.g. TX-NEW)
  const isEmptyUnit =
    summary &&
    summary.metrics.find((m) => m.key === 'clean_goals_share')?.value === null &&
    issues.length === 0 &&
    !loadingIssues;

  return (
    <Layout style={{ minHeight: '100vh', background: '#f7f3f0' }}>
      <Content style={{ padding: '24px 32px', maxWidth: 1440, margin: '0 auto', width: '100%' }}>
        {/* Top Control Header */}
        <Header
          runs={runs}
          units={units}
          selectedRunId={selectedRunId}
          selectedUnitId={selectedUnitId}
          onRunChange={handleRunChange}
          onUnitChange={handleUnitChange}
          comparedToRunId={summary?.compared_to?.id}
          currentRunStatus={summary?.run?.status}
        />

        {/* Loading Spinner for Summary */}
        {loadingSummary && (
          <div style={{ textAlign: 'center', padding: '60px 0' }}>
            <Spin size="large" tip="Загрузка данных процедуры ККД..." />
          </div>
        )}

        {/* Error Handling (e.g. RUN-006 failed 409 or network error) */}
        {!loadingSummary && summaryError && (
          <ErrorAlert
            error={summaryError}
            onSelectRun={(runId) => {
              const latestCompleted = runs.find((r) => r.status === 'completed');
              handleRunChange(latestCompleted ? latestCompleted.id : 'RUN-001');
            }}
            onRetry={loadSummary}
          />
        )}

        {/* Normal Content View */}
        {!loadingSummary && !summaryError && summary && (
          <>
            {/* Empty Unit Banner (TX-NEW) */}
            {isEmptyUnit && <EmptyUnitAlert unitName={summary.unit.name} />}

            {/* 1. Metric Cards Grid */}
            <MetricCards metrics={summary.metrics} />

            {/* 2. Severity & Delta Breakdown Bar with clickable counters */}
            <BreakdownBar
              severities={summary.issues_by_severity}
              delta={summary.delta}
              selectedDeltaStatus={selectedDeltaStatus}
              onDeltaStatusSelect={(status) => {
                setSelectedDeltaStatus(status);
                setPage(1);
              }}
              selectedSeverity={selectedSeverity}
              onSeveritySelect={(sev) => {
                setSelectedSeverity(sev);
                setPage(1);
              }}
            />

            {/* 3. Filterable Paginated Issues Table */}
            <IssuesTable
              issues={issues}
              total={totalIssues}
              loading={loadingIssues}
              page={page}
              pageSize={pageSize}
              onPageChange={handlePageChange}
              selectedRule={selectedRule}
              onRuleChange={(r) => {
                setSelectedRule(r);
                setPage(1);
              }}
              selectedSeverity={selectedSeverity}
              onSeverityChange={(s) => {
                setSelectedSeverity(s);
                setPage(1);
              }}
              selectedDeltaStatus={selectedDeltaStatus}
              onDeltaStatusChange={(d) => {
                setSelectedDeltaStatus(d);
                setPage(1);
              }}
              searchQuery={searchQuery}
              onSearchChange={(q) => {
                setSearchQuery(q);
                setPage(1);
              }}
              onSelectIssue={handleSelectIssue}
              onRefresh={loadIssues}
              meta={meta}
            />
          </>
        )}

        {/* 4. Detailed Issue Modal */}
        <IssueModal
          issue={modalIssue}
          open={isModalOpen}
          onClose={() => setIsModalOpen(false)}
        />
      </Content>
    </Layout>
  );
};
