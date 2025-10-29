import React, { useEffect, useState } from 'react'
import {
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  UserOutlined,
  VideoCameraOutlined,
  AndroidOutlined,
  HighlightOutlined,
  DashboardOutlined
} from '@ant-design/icons'
import { Button, Layout, Menu, theme } from 'antd'
import { Outlet, useLocation, useNavigate } from 'react-router-dom'
import { isLoginAPI } from '@/ui-backend/apis/user'

const { Header, Sider, Content } = Layout

const App: React.FC = () => {
  const [collapsed, setCollapsed] = useState(false)
  //路由跳转
  const navigate = useNavigate()
  //高亮
  //获取当前路径
  const location = useLocation()
  const selectedKey = location.pathname

  const {
    token: { colorBgContainer, borderRadiusLG }
  } = theme.useToken()

  //处理未登录时，同样后端界面的情况
  const [isLogin, setIsLogin] = useState(true)
  useEffect(() => {
    const getIsLogin = async () => {
      const res = await isLoginAPI()
      setIsLogin(res?.data.code == 0 ? false : true)
    }
    getIsLogin()
  }, [])
  return (
    <>
      {isLogin ? (
        <Layout>
          <Sider trigger={null} collapsible collapsed={collapsed}>
            <div className="demo-logo-vertical" />
            <Menu
              theme="dark"
              mode="inline"
              // defaultSelectedKeys={['1']}
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
                  icon: <VideoCameraOutlined />,
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
                  key: '/generator',
                  icon: <HighlightOutlined />,
                  label: '文生图/视频',
                  onClick: () => navigate('/generator')
                },
                {
                  key: '/logging',
                  icon: <DashboardOutlined />,
                  label: '日志记录',
                  onClick: () => navigate('/logging')
                }
              ]}
            />
          </Sider>
          <Layout>
            <Header style={{ padding: 0, background: colorBgContainer }}>
              <div style={{ display: 'flex' }}>
                <div>
                  <Button
                    type="text"
                    icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
                    onClick={() => setCollapsed(!collapsed)}
                    style={{
                      fontSize: '16px',
                      width: 64,
                      height: 64
                    }}
                  />
                </div>
              </div>
            </Header>
            <Content
              style={{
                margin: '24px 16px',
                padding: 24,
                minHeight: '100vh',
                background: colorBgContainer,
                borderRadius: borderRadiusLG
              }}>
              <Outlet />
            </Content>
          </Layout>
        </Layout>
      ) : (
        navigate('/backend/login')
      )}
    </>
  )
}

export default App
