import React from 'react';
import { Table, Tag, Input, Select, Space, Button, Typography, Tooltip, Empty } from 'antd';
import {
  SearchOutlined,
  EyeOutlined,
  ExclamationCircleOutlined,
  WarningOutlined,
  InfoCircleOutlined,
  ReloadOutlined,
} from '@ant-design/icons';
import type { Issue, MetaData } from '../types';

const { Text } = Typography;

interface IssuesTableProps {
  issues: Issue[];
  total: number;
  loading: boolean;
  page: number;
  pageSize: number;
  onPageChange: (page: number, pageSize: number) => void;
  selectedRule: string | null;
  onRuleChange: (rule: string | null) => void;
  selectedSeverity: string | null;
  onSeverityChange: (sev: string | null) => void;
  selectedDeltaStatus: string | null;
  onDeltaStatusChange: (status: string | null) => void;
  searchQuery: string;
  onSearchChange: (q: string) => void;
  onSelectIssue: (issue: Issue) => void;
  onRefresh: () => void;
  meta: MetaData | null;
}

export const IssuesTable: React.FC<IssuesTableProps> = ({
  issues,
  total,
  loading,
  page,
  pageSize,
  onPageChange,
  selectedRule,
  onRuleChange,
  selectedSeverity,
  onSeverityChange,
  selectedDeltaStatus,
  onDeltaStatusChange,
  searchQuery,
  onSearchChange,
  onSelectIssue,
  onRefresh,
  meta,
}) => {
  const getSeverityTag = (sev: string) => {
    if (sev === 'critical') {
      return (
        <Tag color="#d43629" style={{ color: '#fff', fontWeight: 600, borderRadius: 4 }}>
          <ExclamationCircleOutlined style={{ marginRight: 4 }} />
          Критичная
        </Tag>
      );
    }
    if (sev === 'major') {
      return (
        <Tag color="#faad14" style={{ color: '#312921', fontWeight: 600, borderRadius: 4 }}>
          <WarningOutlined style={{ marginRight: 4 }} />
          Существенная
        </Tag>
      );
    }
    return (
      <Tag style={{ background: '#eae4de', color: '#312921', borderRadius: 4 }}>
        <InfoCircleOutlined style={{ marginRight: 4 }} />
        Незначительная
      </Tag>
    );
  };

  const getDeltaTag = (status: string, runsInRow: number) => {
    if (status === 'new') {
      return (
        <Tag style={{ background: '#fce3d9', color: '#b81301', border: '1px solid #f5c1b5', borderRadius: 12 }}>
          Новая
        </Tag>
      );
    }
    if (status === 'resolved') {
      return (
        <Tag style={{ background: '#e3edd8', color: '#007802', border: '1px solid #c7dfb0', borderRadius: 12 }}>
          Исправлена
        </Tag>
      );
    }
    return (
      <Tag style={{ background: '#eae4de', color: '#312921', border: '1px solid #d3ccc4', borderRadius: 12 }}>
        {runsInRow > 1 ? `${runsInRow} прогона подряд` : 'Сохраняется'}
      </Tag>
    );
  };

  const columns = [
    {
      title: 'Важность',
      dataIndex: 'severity',
      key: 'severity',
      width: 140,
      render: (sev: string) => getSeverityTag(sev),
    },
    {
      title: 'Правило ККД',
      dataIndex: 'rule_title',
      key: 'rule_title',
      width: 220,
      render: (title: string, record: Issue) => (
        <div>
          <Text strong style={{ fontSize: 13, color: '#312921', display: 'block' }}>
            {title}
          </Text>
          <Text code style={{ fontSize: 11, color: '#76726C' }}>
            {record.rule_code}
          </Text>
        </div>
      ),
    },
    {
      title: 'Объект / Цель',
      key: 'object',
      render: (_: any, record: Issue) => (
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginBottom: 2 }}>
            <Tag color="geekblue" style={{ fontSize: 10, lineHeight: '16px', padding: '0 4px', margin: 0 }}>
              {record.goal_id}
            </Tag>
            <Text style={{ fontSize: 13, color: '#312921', fontWeight: 500 }}>
              {record.goal_title}
            </Text>
          </div>
          {record.kr_title && (
            <div style={{ fontSize: 12, color: '#76726C', paddingLeft: 8, borderLeft: '2px solid #eae4de', marginTop: 4 }}>
              КР: {record.kr_title}
            </div>
          )}
          <div style={{ fontSize: 11, color: '#9d9b99', marginTop: 2 }}>
            Подразделение: {record.unit_name}
          </div>
        </div>
      ),
    },
    {
      title: 'Ответственный',
      dataIndex: 'owner',
      key: 'owner',
      width: 170,
      render: (owner: any) =>
        owner?.full_name ? (
          <Text style={{ fontSize: 13, color: '#312921' }}>{owner.full_name}</Text>
        ) : (
          <Text type="danger" style={{ fontSize: 12, fontStyle: 'italic' }}>
            Не назначен
          </Text>
        ),
    },
    {
      title: 'Детали нарушения',
      dataIndex: 'details',
      key: 'details',
      render: (details: string, record: Issue) => (
        <div>
          <Text style={{ fontSize: 13, color: '#312921' }}>{details}</Text>
          {record.actual && (
            <div style={{ fontSize: 11, color: '#76726C', marginTop: 2 }}>
              факт: <b>{record.actual}</b> | норма: <b>{record.expected}</b>
            </div>
          )}
        </div>
      ),
    },
    {
      title: 'Статус дельты',
      key: 'delta_status',
      width: 150,
      render: (_: any, record: Issue) => getDeltaTag(record.delta_status, record.runs_in_row),
    },
    {
      title: 'Действие',
      key: 'action',
      width: 110,
      render: (_: any, record: Issue) => (
        <Button
          size="small"
          type="link"
          icon={<EyeOutlined />}
          onClick={() => onSelectIssue(record)}
          style={{ fontSize: 13, padding: 0 }}
        >
          Детали
        </Button>
      ),
    },
  ];

  return (
    <div
      style={{
        background: '#fefcfa',
        borderRadius: 12,
        border: '1px solid #e8e5e2',
        padding: '20px 24px',
      }}
    >
      {/* Table Toolbar / Filters */}
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: 12,
          marginBottom: 16,
        }}
      >
        <Space size={12} wrap>
          {/* Search bar */}
          <Input
            placeholder="Поиск по цели, КР или сотруднику..."
            prefix={<SearchOutlined style={{ color: '#9d9b99' }} />}
            value={searchQuery}
            onChange={(e) => onSearchChange(e.target.value)}
            style={{ width: 280, borderRadius: 6 }}
            allowClear
          />

          {/* Rule Filter */}
          <Select
            placeholder="Фильтр по правилу"
            style={{ width: 220 }}
            value={selectedRule}
            onChange={onRuleChange}
            allowClear
            options={meta?.rules.map((r) => ({
              value: r.code,
              label: r.title,
            }))}
          />

          {/* Severity Filter */}
          <Select
            placeholder="Важность"
            style={{ width: 150 }}
            value={selectedSeverity}
            onChange={onSeverityChange}
            allowClear
            options={[
              { value: 'critical', label: 'Критичная' },
              { value: 'major', label: 'Существенная' },
              { value: 'minor', label: 'Незначительная' },
            ]}
          />

          {/* Delta Status Filter */}
          <Select
            placeholder="Статус дельты"
            style={{ width: 160 }}
            value={selectedDeltaStatus}
            onChange={onDeltaStatusChange}
            allowClear
            options={[
              { value: 'new', label: 'Новые' },
              { value: 'persisting', label: 'Сохраняющиеся' },
              { value: 'resolved', label: 'Исправленные' },
            ]}
          />
        </Space>

        <Space>
          <Text style={{ fontSize: 13, color: '#76726C' }}>
            Всего записей: <b>{total}</b>
          </Text>
          <Button icon={<ReloadOutlined />} onClick={onRefresh} size="small" />
        </Space>
      </div>

      {/* Main Table */}
      <Table
        dataSource={issues}
        columns={columns}
        rowKey={(r) => `${r.id}-${r.delta_status}`}
        loading={loading}
        pagination={{
          current: page,
          pageSize: pageSize,
          total: total,
          showSizeChanger: true,
          pageSizeOptions: ['10', '20', '50', '100'],
          onChange: onPageChange,
        }}
        locale={{
          emptyText: (
            <Empty
              description="Нет проблем, соответствующих выбранным критериям фильтрации"
              image={Empty.PRESENTED_IMAGE_SIMPLE}
            />
          ),
        }}
        size="middle"
      />
    </div>
  );
};
