import React, { useEffect, useMemo, useRef, useState } from 'react'
import { Avatar, Button, Card, Flex, Input, Layout, Space, Typography } from 'antd'
import { UserOutlined, RobotOutlined, SendOutlined } from '@ant-design/icons'

type ChatMessage = {
  id: string
  role: 'user' | 'assistant'
  content: string
  pending?: boolean // streaming
}

function generateMemoryId(): number {
  // 简单生成一个 6 位数字 id
  return Math.floor(100000 + Math.random() * 900000)
}

export const GeneratorHelper = () => {
  const [memoryId] = useState<number>(() => generateMemoryId())
  const [input, setInput] = useState<string>('')
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [loading, setLoading] = useState<boolean>(false)
  const sseRef = useRef<EventSource | null>(null)
  const listEndRef = useRef<HTMLDivElement | null>(null)

  const apiBase = useMemo(() => '/api', [])

  useEffect(() => {
    return () => {
      if (sseRef.current) {
        sseRef.current.close()
      }
    }
  }, [])

  //TODO 前端发送消息到后端
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const startSSE = (text: string) => {
    if (sseRef.current) {
      sseRef.current.close()
      sseRef.current = null
    }
    // 先推入用户消息与一个占位的 assistant 空消息
    const userMsg: ChatMessage = {
      id: `${Date.now()}-user`,
      role: 'user',
      content: text
    }
    const aiMsgId = `${Date.now()}-ai`
    const aiMsg: ChatMessage = {
      id: aiMsgId,
      role: 'assistant',
      content: '',
      pending: true
    }
    setMessages((prev) => [...prev, userMsg, aiMsg])

    const url = new URL(`${apiBase}/ai/chat`, window.location.origin)

    url.searchParams.set('memoryId', String(memoryId))
    url.searchParams.set('message', text)

    // 搭建SSE连接
    const es = new EventSource(url.toString())
    sseRef.current = es

    es.onmessage = (ev) => {
      const chunk = ev.data ?? ''
      setMessages((prev) => prev.map((m) => (m.id === aiMsgId ? { ...m, content: m.content + chunk } : m)))
    }

    es.onerror = () => {
      es.close()
      sseRef.current = null
      setMessages((prev) => prev.map((m) => (m.id === aiMsgId ? { ...m, pending: false } : m)))
      setLoading(false)
    }

    es.onopen = () => {
      setLoading(false)
    }
  }

  const handleSend = async () => {
    const text = input.trim()
    if (!text) return
    setInput('')
    setLoading(true)
    try {
      startSSE(text)
    } catch (e) {
      setLoading(false)
      // 发生错误时仅在控制台输出，避免对 Ant Design App 上下文的依赖
      // eslint-disable-next-line no-console
      console.error('发送失败', e)
    }
  }

  const handleKeyDown: React.KeyboardEventHandler<HTMLInputElement> = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSend()
    }
  }

  return (
    <Layout style={{ height: 'calc(90vh - 100px)' }}>
      <Layout.Content style={{ display: 'flex', flexDirection: 'column', height: 'calc(100vh - 16px)', padding: '16px' }}>
        <Card style={{ flex: 1, overflow: 'hidden', marginBottom: '16px' }}>
          <Space direction="vertical" style={{ width: '100%' }} size={16}>
            {messages.map((msg) => (
              <Flex key={msg.id} justify={msg.role === 'user' ? 'flex-end' : 'flex-start'}>
                {msg.role === 'assistant' && <Avatar style={{ background: '#1677ff' }} icon={<RobotOutlined />} />}
                <div className={msg.role === 'user' ? 'bubble user' : 'bubble ai'}>
                  <Typography.Text style={{ whiteSpace: 'pre-wrap' }}>
                    {msg.content || (msg.pending ? '...' : '')}
                  </Typography.Text>
                </div>
                {msg.role === 'user' && <Avatar style={{ background: '#87d068', marginLeft: 8 }} icon={<UserOutlined />} />}
              </Flex>
            ))}
            <div ref={listEndRef} />
          </Space>
        </Card>
        <div style={{ padding: '8px 0' }}>
          <Flex gap={8}>
            <Input
              placeholder="请输入你的问题..."
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={handleKeyDown}
              disabled={loading}
            />
            <Button type="primary" icon={<SendOutlined />} onClick={handleSend} loading={loading}>
              发送
            </Button>
          </Flex>
        </div>
      </Layout.Content>
    </Layout>
  )
}
