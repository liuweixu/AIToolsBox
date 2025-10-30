import { useEffect, useRef, useState } from 'react'

type Message = {
  role: 'user' | 'assistant'
  content: string
}

const MODEL_OPTIONS = [
  { label: '阿里', value: 'Ali' },
  { label: 'Ollama', value: 'Ollama' },
  { label: '智谱', value: 'Zhipu' },
  { label: 'DeepSeek', value: 'DeepSeek' }
]

export const Unity = () => {
  const [chatId, setChatId] = useState<string>('')
  const [modelName, setModelName] = useState<string>(MODEL_OPTIONS[0].value)
  const [input, setInput] = useState<string>('')
  const [messages, setMessages] = useState<Message[]>([])
  const [streaming, setStreaming] = useState<boolean>(false)
  const eventSourceRef = useRef<EventSource | null>(null)

  useEffect(() => {
    return () => {
      // 组件卸载时关闭流
      if (eventSourceRef.current) {
        eventSourceRef.current.close()
        eventSourceRef.current = null
      }
    }
  }, [])

  const handleNewChat = () => {
    const id = (window.crypto?.randomUUID?.() || `${Date.now()}-${Math.random().toString(16).slice(2)}`)
    setChatId(id)
    setMessages([])
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
    if (!chatId) {
      // 没有会话则自动新建一个
      handleNewChat()
    }

    const currentChatId = chatId || (window.crypto?.randomUUID?.() || `${Date.now()}-${Math.random().toString(16).slice(2)}`)
    if (!chatId) setChatId(currentChatId)

    // 先落地用户消息
    setMessages(prev => [...prev, { role: 'user', content: input } as Message, { role: 'assistant', content: '' }])

    // 开始流式请求
    const url = `/chat/sse?message=${encodeURIComponent(input)}&chatId=${encodeURIComponent(currentChatId)}&modelName=${encodeURIComponent(modelName)}`
    const es = new EventSource(url)
    eventSourceRef.current = es
    setStreaming(true)
    setInput('')

    es.onmessage = (ev: MessageEvent<string>) => {
      const chunk = ev.data || ''
      setMessages(prev => {
        const next = [...prev]
        // 将流式片段追加到最后一条 assistant 消息
        for (let i = next.length - 1; i >= 0; i--) {
          if (next[i].role === 'assistant') {
            next[i] = { ...next[i], content: (next[i].content || '') + chunk }
            break
          }
        }
        return next
      })
    }

    es.onerror = () => {
      stopStream()
    }

    es.onopen = () => {
      // 连接建立
    }
  }

  return (
    <div className="flex h-[calc(100vh-0px)] bg-white">
      {/* 左侧会话与操作栏（简约） */}
      <div className="w-64 border-r border-gray-200 p-4 flex flex-col gap-3">
        <div className="text-lg font-semibold">Unity 助手</div>
        <button
          className="px-3 py-2 rounded-md bg-black text-white disabled:opacity-60"
          onClick={handleNewChat}
        >
          新建对话
        </button>
        <div className="text-xs text-gray-500 break-all">ChatID：{chatId || '未创建'}</div>
        <div>
          <label className="text-sm text-gray-600">选择模型</label>
          <select
            className="mt-1 w-full border border-gray-300 rounded-md p-2"
            value={modelName}
            onChange={e => setModelName(e.target.value)}
          >
            {MODEL_OPTIONS.map(opt => (
              <option key={opt.value} value={opt.value}>{opt.label}</option>
            ))}
          </select>
        </div>
      </div>

      {/* 右侧聊天区 */}
      <div className="flex-1 flex flex-col">
        <div className="flex-1 overflow-auto p-6 space-y-4">
          {messages.length === 0 && (
            <div className="text-center text-gray-400 mt-24">今天有什么可以帮助到你？</div>
          )}
          {messages.map((m, idx) => (
            <div key={idx} className={m.role === 'user' ? 'text-right' : 'text-left'}>
              <div
                className={
                  'inline-block max-w-[80%] whitespace-pre-wrap rounded-2xl px-4 py-2 ' +
                  (m.role === 'user' ? 'bg-black text-white' : 'bg-gray-100 text-gray-900')
                }
              >
                {m.content || (m.role === 'assistant' && streaming ? '思考中…' : '')}
              </div>
            </div>
          ))}
        </div>

        {/* 输入区 */}
        <div className="border-t border-gray-200 p-4">
          <div className="flex items-end gap-2">
            <textarea
              className="flex-1 border border-gray-300 rounded-md p-3 min-h-[48px] max-h-40 resize-y"
              placeholder="输入你的问题…"
              value={input}
              onChange={e => setInput(e.target.value)}
              disabled={streaming}
            />
            <button
              className="px-4 py-2 rounded-md bg-black text-white disabled:opacity-60"
              onClick={handleSend}
              disabled={streaming || !input.trim()}
            >
              发送
            </button>
            {streaming && (
              <button
                className="px-3 py-2 rounded-md border border-gray-300"
                onClick={stopStream}
              >
                停止
              </button>
            )}
          </div>
          <div className="text-xs text-gray-400 mt-2">后端 SSE 接口：/chat/sse</div>
        </div>
      </div>
    </div>
  )
}
