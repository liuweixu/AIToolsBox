import { Breadcrumb, Card } from 'antd'
import { BarChartOutlined } from '@ant-design/icons'
import CountUp from 'react-countup'
import { Col, Row, Statistic } from 'antd'
import { useEffect, useState } from 'react'

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const formatter = (value: any) => <CountUp end={value} separator="," />
const App = () => {
  // 统计文章数量和频道数量
  const [articleCount] = useState(0)
  const [channelCount] = useState(0)
  useEffect(() => {
    const getCount = async () => {}
    getCount()
  }, [])
  return (
    <div>
      <Breadcrumb
        separator=">"
        items={[
          {
            title: '首页',
            href: '/backend/'
          }
        ]}
      />
      <Card
        style={{
          width: '100%',
          marginTop: 20,
          borderRadius: 12,
          boxShadow: '0 2px 8px rgba(0,0,0,0.05)'
        }}
        title={
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <BarChartOutlined style={{ color: '#1890ff', fontSize: 20 }} />
            <span
              style={{
                fontSize: 18,
                fontWeight: 600,
                color: '#333',
                fontFamily: "'KaiTi', 'KaiTi_GB2312', 'STKaiti', serif"
              }}>
              统计信息
            </span>
          </div>
        }>
        <Row gutter={16}>
          <Col span={12}>
            <Statistic title="文章数量" value={articleCount} formatter={formatter} />
          </Col>
          <Col span={12}>
            <Statistic title="频道数量" value={channelCount} precision={2} formatter={formatter} />
          </Col>
        </Row>
      </Card>
    </div>
  )
}

export default App
