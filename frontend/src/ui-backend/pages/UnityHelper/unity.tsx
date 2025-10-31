import { useEffect, useRef, useState } from 'react'
import { Button, Input, Select, Space } from 'antd'
import { PaperClipOutlined, SendOutlined } from '@ant-design/icons'
import { Conversation } from './conversation'
import { marked } from 'marked'
import 'github-markdown-css/github-markdown.css'
import './style.css'
import { createChatUnity } from '@/ui-backend/apis/unity'

const { TextArea } = Input

// 配置 marked
marked.setOptions({
  breaks: true, // 支持换行
  gfm: true // 支持 GitHub 风格的 Markdown
})

type Message = {
  role: 'user' | 'assistant'
  content: string
}

type Chat = {
  id: string
  title: string
  messages: Message[]
}

const MODEL_OPTIONS = [
  { label: '阿里', value: 'DASHSCOPE' },
  { label: 'Ollama', value: 'OLLAMA' },
  { label: '智谱', value: 'ZHIPUAI' },
  { label: 'DeepSeek', value: 'DEEPSEEK' }
]

export const Unity = () => {
  const [chats, setChats] = useState<Chat[]>([])
  const [activeChatId, setActiveChatId] = useState<string>('')
  const [modelName, setModelName] = useState<string>(MODEL_OPTIONS[0].value)
  const [input, setInput] = useState<string>('')
  const [streaming, setStreaming] = useState<boolean>(false)
  const eventSourceRef = useRef<EventSource | null>(null)
  const [refreshTrigger, setRefreshTrigger] = useState<number>(0)

  useEffect(() => {
    return () => {
      // 组件卸载时关闭流
      if (eventSourceRef.current) {
        eventSourceRef.current.close()
        eventSourceRef.current = null
      }
    }
  }, [])

  /**
   * 创建对话框
   */
  const handleNewChat = async () => {
    const res = await createChatUnity()
    console.log(res.data)
    const id = res.data.id
    setActiveChatId(id)
    setChats((prev) => [...prev, { id, title: '新对话', messages: [] }])
    setRefreshTrigger((prev) => prev + 1)
  }

  const stopStream = () => {
    if (eventSourceRef.current) {
      eventSourceRef.current.close()
      eventSourceRef.current = null
    }
    setStreaming(false)
  }

  const handleSend = () => {
    if (!input.trim()) return
    if (!activeChatId) {
      // 没有会话则自动新建一个
      handleNewChat()
    }

    const currentChatId = activeChatId
    if (!activeChatId) setActiveChatId(currentChatId)

    // 先落地用户消息到指定会话
    setChats((prev) => {
      const next = prev.map((c) => {
        if (c.id !== currentChatId) return c
        const updatedMessages: Message[] = [
          ...c.messages,
          { role: 'user' as const, content: input },
          { role: 'assistant' as const, content: '' }
        ]
        const updatedTitle = c.title === '新对话' ? input.slice(0, 20) || '新对话' : c.title
        return { ...c, messages: updatedMessages, title: updatedTitle }
      })
      // 若是刚刚自动创建的会话（active 但列表里还没有），补充创建
      if (!next.find((c) => c.id === currentChatId)) {
        return [
          {
            id: currentChatId,
            title: input.slice(0, 20) || '新对话',
            messages: [
              { role: 'user' as const, content: input },
              { role: 'assistant' as const, content: '' }
            ]
          },
          ...next
        ]
      }
      return next
    })

    // 开始流式请求
    const url = `/api/unity/chat/sse?message=${encodeURIComponent(input)}&chatId=${encodeURIComponent(
      currentChatId
    )}&modelName=${encodeURIComponent(modelName)}`
    const es = new EventSource(url)
    eventSourceRef.current = es
    setStreaming(true)
    setInput('')

    es.onmessage = (ev: MessageEvent<string>) => {
      try {
        // 后端返回的是 JSON 格式：{"datastream": "实际内容"}
        const rawData = ev.data || '{}'
        const data = JSON.parse(rawData)
        const chunk = data.datastream !== undefined ? data.datastream : ''

        // 即使 chunk 为空字符串也更新（可能是流式数据的一部分）
        setChats((prev) =>
          prev.map((c) => {
            if (c.id !== currentChatId) return c
            const msgs = [...c.messages]
            for (let i = msgs.length - 1; i >= 0; i--) {
              if (msgs[i].role === 'assistant') {
                msgs[i] = { ...msgs[i], content: (msgs[i].content || '') + chunk }
                break
              }
            }
            return { ...c, messages: msgs }
          })
        )
      } catch (error) {
        console.error('解析 SSE 数据失败:', error, '原始数据:', ev.data)
        // 如果解析失败，尝试直接使用原始数据
        setChats((prev) =>
          prev.map((c) => {
            if (c.id !== currentChatId) return c
            const msgs = [...c.messages]
            for (let i = msgs.length - 1; i >= 0; i--) {
              if (msgs[i].role === 'assistant') {
                msgs[i] = { ...msgs[i], content: (msgs[i].content || '') + (ev.data || '') }
                break
              }
            }
            return { ...c, messages: msgs }
          })
        )
      }
    }

    // 监听 done 事件，标记流结束
    es.addEventListener('done', () => {
      stopStream()
    })

    es.onerror = () => {
      stopStream()
    }
  }

  return (
    <div className="flex h-[calc(100vh-96px)] min-h-0 bg-white">
      {/* 左侧会话与操作栏（保留会话，可删除） */}
      <div className="w-72 border-r border-gray-200 p-4 flex flex-col gap-3 min-h-0">
        <div className="text-lg font-semibold flex-shrink-0">Unity 助手</div>
        <Button
          size="large"
          onClick={handleNewChat}
          className="w-full !bg-white !text-gray-900 !border-0 !rounded-full !shadow-sm hover:!bg-gray-50 !h-auto !py-3 !px-6 !flex !items-center !justify-center !gap-2 flex-shrink-0">
          开启新对话
        </Button>
        <div className="text-xs text-gray-500 flex-shrink-0">共 {chats.length} 个对话</div>
        <div className="flex-1 min-h-0">
          <Conversation refreshTrigger={refreshTrigger} />
        </div>
      </div>

      {/* 右侧聊天区 */}
      <div className="flex-1 min-w-0 flex flex-col min-h-0">
        <div className="flex-1 min-h-0 overflow-auto p-6 space-y-4">
          {(!activeChatId || !chats.find((c) => c.id === activeChatId)?.messages.length) && (
            <div className="text-center text-gray-400 mt-24">今天有什么可以帮助到你？</div>
          )}
          {chats
            .find((c) => c.id === activeChatId)
            ?.messages.map((m, idx) => (
              <div key={idx} className={m.role === 'user' ? 'text-right' : 'text-left'}>
                <div
                  className={
                    'inline-block max-w-[80%] rounded-2xl px-4 py-2 ' +
                    (m.role === 'user' ? 'bg-blue-100 text-black' : 'bg-white text-black')
                  }>
                  {m.role === 'assistant' && m.content ? (
                    <div
                      className="markdown-body"
                      style={{
                        fontSize: '14px',
                        lineHeight: '1.6',
                        color: '#24292f',
                        backgroundColor: 'transparent'
                      }}
                      dangerouslySetInnerHTML={{
                        __html: marked.parse(m.content) as string
                      }}
                    />
                  ) : m.role === 'user' ? (
                    <div className="whitespace-pre-wrap">{m.content}</div>
                  ) : (
                    <div>{streaming ? '思考中…' : ''}</div>
                  )}
                </div>
              </div>
            ))}
        </div>

        {/* 输入区，固定在底部 */}
        <div className="border-t border-gray-200 p-4 flex-shrink-0">
          {/* 输入框容器（相对定位，按钮绝对定位在其中） */}
          <div className="relative rounded-2xl border border-gray-300 bg-white">
            <TextArea
              className="!rounded-2xl !border-0 !shadow-none !resize-none"
              placeholder={`给 ${MODEL_OPTIONS.find((opt) => opt.value === modelName)?.label || 'AI'} 发送消息`}
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter' && !e.shiftKey) {
                  e.preventDefault()
                  handleSend()
                }
              }}
              disabled={streaming}
              autoSize={false}
              rows={5}
              style={{
                fontSize: '14px',
                padding: '12px 16px',
                paddingBottom: '80px',
                minHeight: '50px',
                maxHeight: '100px'
              }}
            />

            {/* 底部操作栏（绝对定位在输入框内部） */}
            <div className="absolute bottom-3 left-4 right-4 flex items-center justify-between pointer-events-none">
              {/* 左侧功能按钮 */}
              <Space size="small" className="pointer-events-auto">
                <Select
                  className="!w-28 !bg-white"
                  value={modelName}
                  onChange={(value) => setModelName(value)}
                  disabled={streaming}
                  options={MODEL_OPTIONS}
                  size="small"
                />
              </Space>

              {/* 右侧操作按钮 */}
              <Space size="small" className="pointer-events-auto">
                <Button
                  type="text"
                  icon={<PaperClipOutlined />}
                  className="!text-gray-700 hover:!bg-gray-100"
                  disabled={streaming}
                />
                {streaming ? (
                  <Button type="default" className="!border-gray-300" onClick={stopStream}>
                    停止
                  </Button>
                ) : (
                  <Button
                    type="primary"
                    shape="circle"
                    icon={<SendOutlined />}
                    onClick={handleSend}
                    disabled={!input.trim()}
                    className="!bg-blue-500 hover:!bg-blue-600 !border-0 !w-10 !h-10 !flex !items-center !justify-center"
                  />
                )}
              </Space>
            </div>
          </div>
          {/* 调试信息 */}
          <div className="mt-2 flex items-center justify-end">
            <div className="text-xs text-gray-400">后端 SSE 接口：/chat/sse | 当前会话：{activeChatId || '未创建'}</div>
          </div>
        </div>
      </div>
    </div>
  )
}
