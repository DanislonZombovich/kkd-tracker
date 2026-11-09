import React from 'react';
import { Result, Button, Typography } from 'antd';
import { WarningOutlined, ReloadOutlined } from '@ant-design/icons';

const { Paragraph, Text } = Typography;

interface ErrorAlertProps {
  error: {
    code?: string;
    message?: string;
  } | null;
  onSelectRun: (runId: string) => void;
  onRetry: () => void;
}

export const ErrorAlert: React.FC<ErrorAlertProps> = ({ error, onSelectRun, onRetry }) => {
  const isRunFailed = error?.code === 'RUN_FAILED';

  return (
    <div
      style={{
        background: '#fefcfa',
        borderRadius: 12,
        border: '1px solid #e8e5e2',
        padding: '32px 24px',
        marginBottom: 16,
      }}
    >
      <Result
        status={isRunFailed ? 'warning' : 'error'}
        icon={isRunFailed ? <WarningOutlined style={{ color: '#faad14' }} /> : undefined}
        title={isRunFailed ? 'Прогон завершился с ошибкой' : 'Ошибка загрузки данных'}
        subTitle={
          error?.message ||
          'Не удалось получить данные с сервера. Пожалуйста, проверьте подключение или выберите другой прогон.'
        }
        extra={[
          isRunFailed && (
            <Button
              type="primary"
              key="latest"
              onClick={() => onSelectRun('latest')}
              style={{ borderRadius: 6 }}
            >
              Перейти к последнему успешному прогону
            </Button>
          ),
          <Button key="retry" icon={<ReloadOutlined />} onClick={onRetry} style={{ borderRadius: 6 }}>
            Повторить попытку
          </Button>,
        ].filter(Boolean)}
      >
        {isRunFailed && (
          <div style={{ maxWidth: 520, margin: '0 auto', textAlign: 'left' }}>
            <Paragraph style={{ color: '#76726C', fontSize: 13 }}>
              <Text strong style={{ color: '#312921' }}>Почему это произошло:</Text>
              <br />
              При выполнении еженедельной процедуры ККД произошёл технический сбой на этапе расчёта
              снимка данных. Результаты для данного прогона отсутствуют. В трендах метрик данный
              прогон пропускается, а последующий успешный прогон сопоставляется с предыдущим
              завершённым срезом.
            </Paragraph>
          </div>
        )}
      </Result>
    </div>
  );
};
