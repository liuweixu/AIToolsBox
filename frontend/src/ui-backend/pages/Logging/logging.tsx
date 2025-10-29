import { deleteLoggingAPI, getLoggingAPI } from '@/ui-backend/apis/logging'
import { Breadcrumb, Button, Card } from 'antd'

import { Table } from 'antd'
import { useEffect, useState } from 'react'

export const LoggingPage = () => {
  // 获取日志记录信息
  const [logging, setLogging] = useState([])
  const [count, setCount] = useState(0)
  useEffect(() => {
    const getLoggingList = async () => {
      const resLogging = await getLoggingAPI()
      setLogging(resLogging?.data)
      setCount(resLogging?.data.length)
    }
    getLoggingList()
  }, [])

  const columns = [
    {
      title: '操作时间',
      dataIndex: 'operate_time',
      width: 220
    },
    {
      title: '操作名称',
      dataIndex: 'operate_name',
      width: 220
    },
    {
      title: '消耗时间（单位：毫秒）',
      dataIndex: 'cost_time',
      width: 220
    }
  ]

  const onClearLog = async () => {
    await deleteLoggingAPI()
    setLogging([])
  }

  return (
    <div>
      <Breadcrumb
        separator=">"
        items={[
          {
            title: '首页',
            href: '/backend/'
          },
          {
            title: '日志记录',
            href: '/backend/logging'
          }
        ]}
      />
      {/**表格区域 */}
      <Card
        style={{
          width: '100%',
          marginTop: 20,
          borderRadius: 12,
          boxShadow: '0 2px 8px rgba(0,0,0,0.05)',
          fontFamily: "'KaiTi', 'KaiTi_GB2312', 'STKaiti', serif"
        }}
        title={
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <span
              style={{
                fontSize: 18,
                fontWeight: 600,
                color: '#333',
                fontFamily: "'KaiTi', 'KaiTi_GB2312', 'STKaiti', serif"
              }}>
              日志列表
            </span>
            <div className="ml-auto">
              <Button danger onClick={onClearLog}>
                Clear
              </Button>
            </div>
          </div>
        }>
        <Table
          rowKey="id"
          columns={columns}
          dataSource={logging}
          pagination={{
            total: count,
            pageSize: 10
          }}
        />
      </Card>
    </div>
  )
}
