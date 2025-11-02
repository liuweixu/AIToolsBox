import React from 'react'
import { UserOutlined, AndroidOutlined, HighlightOutlined, CodeSandboxOutlined, VideoCameraOutlined } from '@ant-design/icons'
import { Layout, Menu, theme } from 'antd'
import { Outlet, useLocation, useNavigate } from 'react-router-dom'

const { Sider, Content } = Layout

const App: React.FC = () => {
  //路由跳转
  const navigate = useNavigate()
  //高亮
  //获取当前路径
  const location = useLocation()
  const selectedKey = location.pathname

  const {
    token: { colorBgContainer, borderRadiusLG }
  } = theme.useToken()

  return (
    <Layout>
      <Sider theme="light" trigger={null} collapsible collapsed={true} className="h-[calc(100vh-0px)]">
        <div className="demo-logo-vertical" />
        <Menu
          mode="inline"
          selectedKeys={[selectedKey]}
          items={[
            {
              key: '/',
              icon: <UserOutlined />,
              label: '首页',
              onClick: () => navigate('/')
            },
            {
              key: '/unityhelper',
              icon: <CodeSandboxOutlined />,
              label: 'Unity学习助手',
              onClick: () => navigate('/unityhelper')
            },
            {
              key: '/agent',
              icon: <AndroidOutlined />,
              label: 'AI超级智能体',
              onClick: () => navigate('/agent')
            },
            {
              key: '/text2image',
              icon: <HighlightOutlined />,
              label: '文字生图',
              onClick: () => navigate('/text2image')
            },
            {
              key: '/text2video',
              icon: <VideoCameraOutlined />,
              label: '文字生成视频',
              onClick: () => navigate('/text2video')
            }
          ]}
        />
      </Sider>
      <Layout>
        <Content
          style={{
            margin: '24px 16px',
            padding: 24,
            minHeight: 'calc(100vh-0px)',
            background: colorBgContainer,
            borderRadius: borderRadiusLG
          }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  )
}

export default App
