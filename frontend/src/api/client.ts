import axios from 'axios';
import type { MetaData, Run, SummaryResponse, IssuesResponse, Issue } from '../types';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 10000,
});

export const kkdApi = {
  async getMeta(): Promise<MetaData> {
    const res = await api.get('/api/meta');
    return res.data;
  },

  async getRuns(limit = 20, offset = 0): Promise<{ items: Run[]; total: number }> {
    const res = await api.get('/api/runs', { params: { limit, offset } });
    return res.data;
  },

  async getSummary(runId: string, unitId: string): Promise<SummaryResponse> {
    const res = await api.get('/api/kkd/summary', {
      params: { run_id: runId, unit_id: unitId },
    });
    return res.data;
  },

  async getIssues(
    runId: string,
    unitId: string,
    params: {
      rule_code?: string;
      severity?: string;
      delta_status?: string;
      limit?: number;
      offset?: number;
      q?: string;
      sort?: string;
    } = {}
  ): Promise<IssuesResponse> {
    const res = await api.get('/api/kkd/issues', {
      params: {
        run_id: runId,
        unit_id: unitId,
        ...params,
      },
    });
    return res.data;
  },

  async getIssue(issueId: string, runId: string): Promise<Issue> {
    const res = await api.get(`/api/kkd/issues/${issueId}`, {
      params: { run_id: runId },
    });
    return res.data;
  },
};
