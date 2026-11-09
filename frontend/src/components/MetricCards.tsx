import React from 'react';
import { Row, Col, Card, Typography, Tooltip, Space } from 'antd';
import {
  ArrowUpOutlined,
  ArrowDownOutlined,
  MinusOutlined,
  QuestionCircleOutlined,
} from '@ant-design/icons';
import type { MetricItem } from '../types';

const { Text } = Typography;

interface MetricCardsProps {
  metrics: MetricItem[];
}

export const MetricCards: React.FC<MetricCardsProps> = ({ metrics }) => {
  // Select key metrics to show in prominent top cards
  const targetKeys = ['clean_goals_share', 'cascade_coverage', 'critical_issues', 'issues_total'];
  const displayMetrics = targetKeys
    .map((k) => metrics.find((m) => m.key === k))
    .filter((m): m is MetricItem => Boolean(m));

  // Fallback to first 4 metrics if specific keys aren't all present
  const cardsToShow = displayMetrics.length > 0 ? displayMetrics : metrics.slice(0, 4);

  const getTrendStyle = (trend?: 'better' | 'worse' | 'same' | null) => {
    if (trend === 'better') return { color: '#039306' };
    if (trend === 'worse') return { color: '#d43629' };
    return { color: '#76726C' };
  };

  const getTrendIcon = (delta: number | null) => {
    if (delta === null) return null;
    if (delta > 0) return <ArrowUpOutlined />;
    if (delta < 0) return <ArrowDownOutlined />;
    return <MinusOutlined />;
  };

  return (
    <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
      {cardsToShow.map((m) => {
        const trendStyle = getTrendStyle(m.trend);
        const hasComparison = m.previous !== null && m.delta !== null;

        return (
          <Col xs={24} sm={12} lg={6} key={m.key}>
            <Card
              style={{
                borderRadius: 12,
                border: '1px solid #e8e5e2',
                background: '#fefcfa',
                boxShadow: 'none',
              }}
              bodyStyle={{ padding: '20px 24px' }}
            >
              {/* Metric Title & Tooltip */}
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 12 }}>
                <Text style={{ color: '#76726C', fontSize: 13, fontWeight: 500 }}>
                  {m.title}
                </Text>
                {m.formula && (
                  <Tooltip title={`Формула: ${m.formula}`}>
                    <QuestionCircleOutlined style={{ color: '#9d9b99', fontSize: 13, cursor: 'help' }} />
                  </Tooltip>
                )}
              </div>

              {/* Big bold metric value */}
              <div style={{ marginBottom: 10 }}>
                {m.value !== null ? (
                  <span style={{ fontSize: 28, fontWeight: 600, color: '#312921', lineHeight: 1.1 }}>
                    {m.value}
                    <span style={{ fontSize: 16, fontWeight: 400, marginLeft: 4, color: '#76726C' }}>
                      {m.unit}
                    </span>
                  </span>
                ) : (
                  <span style={{ fontSize: 28, fontWeight: 600, color: '#9d9b99' }}>—</span>
                )}
              </div>

              {/* Honest Delta & Trend display */}
              <div style={{ minHeight: 22, display: 'flex', alignItems: 'center' }}>
                {hasComparison ? (
                  <Space size={6} direction="horizontal">
                    <span
                      style={{
                        ...trendStyle,
                        fontSize: 13,
                        fontWeight: 600,
                        display: 'inline-flex',
                        alignItems: 'center',
                        gap: 2,
                      }}
                    >
                      {getTrendIcon(m.delta)}
                      {m.delta !== null && m.delta > 0 ? `+${m.delta}` : m.delta}
                      {m.unit === '%' ? ' п.п.' : ` ${m.unit}`}
                    </span>
                    <Text style={{ fontSize: 12, color: '#9d9b99' }}>
                      (пред.: {m.previous}{m.unit})
                    </Text>
                  </Space>
                ) : (
                  <Text style={{ fontSize: 12, color: '#9d9b99', fontStyle: 'italic' }}>
                    нет данных для сравнения
                  </Text>
                )}
              </div>
            </Card>
          </Col>
        );
      })}
    </Row>
  );
};
