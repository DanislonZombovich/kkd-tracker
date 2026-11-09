import React from 'react';
import { Card, Row, Col, Typography, Space, Button, Badge } from 'antd';
import {
  ExclamationCircleFilled,
  WarningFilled,
  InfoCircleFilled,
  PlusCircleOutlined,
  ClockCircleOutlined,
  CheckCircleOutlined,
} from '@ant-design/icons';
import type { SeverityCount, DeltaCount } from '../types';

const { Text } = Typography;

interface BreakdownBarProps {
  severities: SeverityCount[];
  delta: DeltaCount;
  selectedDeltaStatus: string | null;
  onDeltaStatusSelect: (status: string | null) => void;
  selectedSeverity: string | null;
  onSeveritySelect: (sev: string | null) => void;
}

export const BreakdownBar: React.FC<BreakdownBarProps> = ({
  severities,
  delta,
  selectedDeltaStatus,
  onDeltaStatusSelect,
  selectedSeverity,
  onSeveritySelect,
}) => {
  const getSevIcon = (code: string) => {
    if (code === 'critical') return <ExclamationCircleFilled style={{ color: '#d43629' }} />;
    if (code === 'major') return <WarningFilled style={{ color: '#faad14' }} />;
    return <InfoCircleFilled style={{ color: '#76726C' }} />;
  };

  return (
    <Card
      style={{
        marginBottom: 16,
        borderRadius: 12,
        border: '1px solid #e8e5e2',
        background: '#fefcfa',
      }}
      bodyStyle={{ padding: '16px 20px' }}
    >
      <Row gutter={[24, 16]} align="middle">
        {/* Severity Breakdown */}
        <Col xs={24} md={12}>
          <Text style={{ fontSize: 13, fontWeight: 600, color: '#312921', display: 'block', marginBottom: 8 }}>
            Проблемы по важности:
          </Text>
          <Space direction="horizontal" size={8} wrap>
            {severities.map((s) => {
              const isSelected = selectedSeverity === s.code;
              return (
                <Button
                  key={s.code}
                  size="small"
                  type={isSelected ? 'primary' : 'default'}
                  onClick={() => onSeveritySelect(isSelected ? null : s.code)}
                  style={{
                    borderRadius: 20,
                    height: 28,
                    display: 'inline-flex',
                    alignItems: 'center',
                    gap: 6,
                    fontSize: 12,
                    fontWeight: 500,
                  }}
                >
                  {getSevIcon(s.code)}
                  <span>{s.title}:</span>
                  <strong>{s.current}</strong>
                  {s.delta !== null && (
                    <span style={{ fontSize: 11, opacity: 0.8 }}>
                      ({s.delta > 0 ? `+${s.delta}` : s.delta})
                    </span>
                  )}
                </Button>
              );
            })}
          </Space>
        </Col>

        {/* Delta Status Breakdown (Clickable counters) */}
        <Col xs={24} md={12}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
            <Text style={{ fontSize: 13, fontWeight: 600, color: '#312921' }}>
              Дельта с прошлым прогоном (клик для фильтра):
            </Text>
            {selectedDeltaStatus && (
              <Button type="link" size="small" onClick={() => onDeltaStatusSelect(null)} style={{ padding: 0, fontSize: 12 }}>
                Сбросить фильтр
              </Button>
            )}
          </div>

          <Space direction="horizontal" size={10} wrap>
            {/* New */}
            <div
              onClick={() => onDeltaStatusSelect(selectedDeltaStatus === 'new' ? null : 'new')}
              style={{
                cursor: 'pointer',
                background: selectedDeltaStatus === 'new' ? '#f5c1b5' : '#fce3d9',
                border: selectedDeltaStatus === 'new' ? '2px solid #d43629' : '1px solid #f5c1b5',
                borderRadius: 20,
                padding: '4px 12px',
                display: 'inline-flex',
                alignItems: 'center',
                gap: 6,
                transition: 'all 0.2s',
              }}
            >
              <PlusCircleOutlined style={{ color: '#b81301', fontSize: 13 }} />
              <span style={{ color: '#b81301', fontSize: 12, fontWeight: 600 }}>Новые:</span>
              <span style={{ color: '#b81301', fontSize: 13, fontWeight: 700 }}>
                {delta.new !== null ? delta.new : '—'}
              </span>
            </div>

            {/* Persisting */}
            <div
              onClick={() => onDeltaStatusSelect(selectedDeltaStatus === 'persisting' ? null : 'persisting')}
              style={{
                cursor: 'pointer',
                background: selectedDeltaStatus === 'persisting' ? '#d3ccc4' : '#eae4de',
                border: selectedDeltaStatus === 'persisting' ? '2px solid #76726C' : '1px solid #d3ccc4',
                borderRadius: 20,
                padding: '4px 12px',
                display: 'inline-flex',
                alignItems: 'center',
                gap: 6,
                transition: 'all 0.2s',
              }}
            >
              <ClockCircleOutlined style={{ color: '#312921', fontSize: 13 }} />
              <span style={{ color: '#312921', fontSize: 12, fontWeight: 600 }}>Сохраняются:</span>
              <span style={{ color: '#312921', fontSize: 13, fontWeight: 700 }}>
                {delta.persisting !== null ? delta.persisting : '—'}
              </span>
            </div>

            {/* Resolved */}
            <div
              onClick={() => onDeltaStatusSelect(selectedDeltaStatus === 'resolved' ? null : 'resolved')}
              style={{
                cursor: 'pointer',
                background: selectedDeltaStatus === 'resolved' ? '#c7dfb0' : '#e3edd8',
                border: selectedDeltaStatus === 'resolved' ? '2px solid #039306' : '1px solid #c7dfb0',
                borderRadius: 20,
                padding: '4px 12px',
                display: 'inline-flex',
                alignItems: 'center',
                gap: 6,
                transition: 'all 0.2s',
              }}
            >
              <CheckCircleOutlined style={{ color: '#007802', fontSize: 13 }} />
              <span style={{ color: '#007802', fontSize: 12, fontWeight: 600 }}>Исправлены:</span>
              <span style={{ color: '#007802', fontSize: 13, fontWeight: 700 }}>
                {delta.resolved !== null ? delta.resolved : '—'}
              </span>
            </div>
          </Space>
        </Col>
      </Row>
    </Card>
  );
};
