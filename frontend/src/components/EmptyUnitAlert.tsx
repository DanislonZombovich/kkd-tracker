import React from 'react';
import { Alert, Typography } from 'antd';
import { InfoCircleOutlined } from '@ant-design/icons';

const { Text } = Typography;

interface EmptyUnitAlertProps {
  unitName: string;
}

export const EmptyUnitAlert: React.FC<EmptyUnitAlertProps> = ({ unitName }) => {
  return (
    <Alert
      message={
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <InfoCircleOutlined style={{ color: '#056cfd', fontSize: 16 }} />
          <div>
            <Text strong style={{ color: '#056cfd' }}>
              Подразделение без активных целей ({unitName})
            </Text>
            <div style={{ color: '#76726C', fontSize: 13, marginTop: 2 }}>
              В данном подразделении и его дочерних структурах на момент прогона нет активных целей.
              Относительные метрики качества данных не рассчитываются (значение «—»), дефекты не
              зафиксированы.
            </div>
          </div>
        </div>
      }
      type="info"
      style={{
        background: '#fefcfa',
        border: '1px solid #EAE7E5',
        borderRadius: 8,
        marginBottom: 16,
      }}
    />
  );
};
