import React from 'react';
import { Card, Select, TreeSelect, Space, Typography, Tag, Tooltip } from 'antd';
import { SyncOutlined, CheckCircleOutlined, CloseCircleOutlined, InfoCircleOutlined } from '@ant-design/icons';
import type { Run, Unit } from '../types';

const { Title, Text } = Typography;

interface HeaderProps {
  runs: Run[];
  units: Unit[];
  selectedRunId: string;
  selectedUnitId: string;
  onRunChange: (runId: string) => void;
  onUnitChange: (unitId: string) => void;
  comparedToRunId?: string | null;
  currentRunStatus?: string;
}

export const Header: React.FC<HeaderProps> = ({
  runs,
  units,
  selectedRunId,
  selectedUnitId,
  onRunChange,
  onUnitChange,
  comparedToRunId,
  currentRunStatus,
}) => {
  // Build tree data for TreeSelect
  const buildTree = (parentId: string | null = null): any[] => {
    return units
      .filter((u) => u.parent_id === parentId || (parentId === null && !u.parent_id))
      .map((u) => ({
        value: u.id,
        title: u.name,
        children: buildTree(u.id),
      }));
  };

  const treeData = buildTree(null);

  const getStatusTag = (status?: string) => {
    if (status === 'completed') {
      return (
        <Tag color="success" icon={<CheckCircleOutlined />}>
          Завершён
        </Tag>
      );
    }
    if (status === 'failed') {
      return (
        <Tag color="error" icon={<CloseCircleOutlined />}>
          Ошибка прогона
        </Tag>
      );
    }
    return (
      <Tag color="processing" icon={<SyncOutlined spin />}>
        Выполняется
      </Tag>
    );
  };

  return (
    <Card
      style={{
        marginBottom: 16,
        borderRadius: 8,
        border: '1px solid #EAE7E5',
        background: '#fefcfa',
      }}
      bodyStyle={{ padding: '16px 24px' }}
    >
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 16 }}>
        <div>
          <Title level={4} style={{ margin: 0, color: '#312921', fontWeight: 600 }}>
            Контроль качества данных (ККД)
          </Title>
          <Text style={{ color: '#76726C', fontSize: 13 }}>
            Проект «Автотрекинг целей B2B+» • Еженедельный аудит целей и ключевых результатов
          </Text>
        </div>

        <Space direction="horizontal" size={16} wrap>
          {/* Unit selector */}
          <div style={{ display: 'flex', flexDirection: 'column' }}>
            <Text style={{ fontSize: 11, color: '#76726C', marginBottom: 2 }}>Подразделение</Text>
            <TreeSelect
              style={{ minWidth: 280 }}
              value={selectedUnitId}
              dropdownStyle={{ maxHeight: 400, overflow: 'auto' }}
              treeData={treeData}
              placeholder="Выберите подразделение"
              treeDefaultExpandAll
              onChange={onUnitChange}
            />
          </div>

          {/* Run selector */}
          <div style={{ display: 'flex', flexDirection: 'column' }}>
            <Text style={{ fontSize: 11, color: '#76726C', marginBottom: 2 }}>
              Прогон снимка данных
            </Text>
            <Select
              style={{ minWidth: 220 }}
              value={selectedRunId}
              onChange={onRunChange}
              options={runs.map((r) => ({
                value: r.id,
                label: (
                  <span>
                    <strong>{r.id}</strong> ({r.snapshot_date})
                  </span>
                ),
              }))}
            />
          </div>

          {/* Status & comparison info */}
          <div style={{ display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
            <Text style={{ fontSize: 11, color: '#76726C', marginBottom: 2 }}>Статус</Text>
            <Space size={6}>
              {getStatusTag(currentRunStatus)}
              {comparedToRunId ? (
                <Tooltip title={`Результаты сравниваются с предыдущим успешным прогоном ${comparedToRunId}`}>
                  <Tag color="default" style={{ background: '#f7f3f0', color: '#76726C' }}>
                    База: {comparedToRunId} <InfoCircleOutlined style={{ marginLeft: 4 }} />
                  </Tag>
                </Tooltip>
              ) : (
                <Tag color="default" style={{ background: '#f7f3f0', color: '#9d9b99' }}>
                  Первый прогон (без базы)
                </Tag>
              )}
            </Space>
          </div>
        </Space>
      </div>
    </Card>
  );
};
