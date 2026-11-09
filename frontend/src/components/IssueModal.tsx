import React from 'react';
import { Modal, Typography, Descriptions, Tag, Alert, Timeline, Divider, Space } from 'antd';
import {
  CheckCircleOutlined,
  CloseCircleOutlined,
  ExclamationCircleOutlined,
  WarningOutlined,
  BulbOutlined,
  HistoryOutlined,
} from '@ant-design/icons';
import type { Issue } from '../types';

const { Title, Text, Paragraph } = Typography;

interface IssueModalProps {
  issue: Issue | null;
  open: boolean;
  onClose: () => void;
}

export const IssueModal: React.FC<IssueModalProps> = ({ issue, open, onClose }) => {
  if (!issue) return null;

  const getSeverityColor = (sev: string) => {
    if (sev === 'critical') return '#d43629';
    if (sev === 'major') return '#faad14';
    return '#76726C';
  };

  return (
    <Modal
      open={open}
      onCancel={onClose}
      footer={null}
      width={720}
      style={{ top: 40 }}
      styles={{
        content: {
          borderRadius: 28,
          padding: 28,
          background: '#fefcfa',
        },
        mask: {
          backgroundColor: 'rgba(50, 44, 38, 0.07)',
        },
      }}
      title={
        <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 8 }}>
          <Title level={4} style={{ margin: 0, fontSize: 20, fontWeight: 600, color: '#312921' }}>
            {issue.rule_title}
          </Title>
          <Tag color={getSeverityColor(issue.severity)} style={{ borderRadius: 4, fontWeight: 600 }}>
            {issue.severity.toUpperCase()}
          </Tag>
        </div>
      }
    >
      <Space direction="vertical" size={16} style={{ width: '100%' }}>
        {/* Recommendation Callout */}
        <Alert
          message={
            <div style={{ display: 'flex', alignItems: 'flex-start', gap: 8 }}>
              <BulbOutlined style={{ color: '#056cfd', fontSize: 16, marginTop: 2 }} />
              <div>
                <Text strong style={{ color: '#056cfd', display: 'block', marginBottom: 2 }}>
                  Рекомендация по устранению дефекта:
                </Text>
                <Text style={{ color: '#312921', fontSize: 13 }}>
                  {issue.recommendation}
                </Text>
              </div>
            </div>
          }
          type="info"
          style={{
            background: '#d5edfa',
            border: '1px solid #bce2f7',
            borderRadius: 8,
          }}
        />

        {/* Descriptions */}
        <Descriptions bordered size="small" column={1} style={{ marginTop: 8 }}>
          <Descriptions.Item label="Идентификатор проблемы">
            <Text code>{issue.id}</Text>
          </Descriptions.Item>

          <Descriptions.Item label="Правило ККД">
            <Text strong>{issue.rule_title}</Text> ({issue.rule_code})
          </Descriptions.Item>

          <Descriptions.Item label="Цель">
            <Text strong>{issue.goal_id}</Text>: {issue.goal_title}
          </Descriptions.Item>

          {issue.kr_title && (
            <Descriptions.Item label="Ключевой результат">
              <Text strong>{issue.kr_id}</Text>: {issue.kr_title}
            </Descriptions.Item>
          )}

          <Descriptions.Item label="Подразделение">
            {issue.unit_name} (<Text code>{issue.unit_id}</Text>)
          </Descriptions.Item>

          <Descriptions.Item label="Ответственный сотрудник">
            {issue.owner?.full_name ? (
              <Text>{issue.owner.full_name}</Text>
            ) : (
              <Text type="danger" italic>
                Не назначен
              </Text>
            )}
          </Descriptions.Item>

          <Descriptions.Item label="Подробности нарушения">
            <Text style={{ color: '#312921' }}>{issue.details}</Text>
          </Descriptions.Item>

          {issue.actual && (
            <Descriptions.Item label="Фактическое значение">
              <Text type="danger" strong>
                {issue.actual}
              </Text>
            </Descriptions.Item>
          )}

          {issue.expected && (
            <Descriptions.Item label="Ожидаемое (нормативное)">
              <Text type="success" strong>
                {issue.expected}
              </Text>
            </Descriptions.Item>
          )}

          <Descriptions.Item label="Статус динамики">
            {issue.delta_status === 'new' && <Tag color="error">Новая в текущем прогоне</Tag>}
            {issue.delta_status === 'persisting' && (
              <Tag color="default">Сохраняется ({issue.runs_in_row} прогона подряд)</Tag>
            )}
            {issue.delta_status === 'resolved' && <Tag color="success">Исправлена</Tag>}
          </Descriptions.Item>
        </Descriptions>

        {/* History across runs */}
        {issue.history && issue.history.length > 0 && (
          <div>
            <Divider style={{ margin: '16px 0 12px 0' }} />
            <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginBottom: 12 }}>
              <HistoryOutlined style={{ color: '#76726C' }} />
              <Text strong style={{ fontSize: 14, color: '#312921' }}>
                История фиксации дефекта по прогонам:
              </Text>
            </div>

            <Timeline
              items={issue.history.map((h) => {
                const isFailed = h.run_status === 'failed' || h.present === null;
                const isPresent = h.present === true;
                return {
                  color: isPresent ? 'red' : isFailed ? 'gray' : 'green',
                  dot: isPresent ? (
                    <ExclamationCircleOutlined style={{ fontSize: 13 }} />
                  ) : isFailed ? (
                    <WarningOutlined style={{ fontSize: 13, color: '#faad14' }} />
                  ) : (
                    <CheckCircleOutlined style={{ fontSize: 13 }} />
                  ),
                  children: (
                    <div>
                      <Text strong style={{ color: '#312921' }}>
                        {h.run_id} ({h.snapshot_date}):
                      </Text>{' '}
                      {isPresent ? (
                        <Text type="danger">Дефект зафиксирован</Text>
                      ) : isFailed ? (
                        <Text style={{ color: '#76726C', fontStyle: 'italic' }}>
                          Прогон завершился с ошибкой (данные отсутствуют)
                        </Text>
                      ) : (
                        <Text type="success">Дефект отсутствует (исправлен)</Text>
                      )}
                    </div>
                  ),
                };
              })}
            />
          </div>
        )}
      </Space>
    </Modal>
  );
};
