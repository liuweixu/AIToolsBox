import { useEffect, useRef, useState } from 'react'

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
  { label: '阿里', value: 'Ali' },
  { label: 'Ollama', value: 'Ollama' },
  { label: '智谱', value: 'Zhipu' },
  { label: 'DeepSeek', value: 'DeepSeek' }
]

export const Unity = () => {
  const [chats, setChats] = useState<Chat[]>([])
  const [activeChatId, setActiveChatId] = useState<string>('')
  const [modelName, setModelName] = useState<string>(MODEL_OPTIONS[0].value)
  const [input, setInput] = useState<string>('')
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
    const newChat: Chat = { id, title: '新对话', messages: [] }
    setChats(prev => [newChat, ...prev])
    setActiveChatId(id)
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

    const currentChatId = activeChatId || (window.crypto?.randomUUID?.() || `${Date.now()}-${Math.random().toString(16).slice(2)}`)
    if (!activeChatId) setActiveChatId(currentChatId)

    // 先落地用户消息到指定会话
    setChats(prev => {
      const next = prev.map(c => {
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
      if (!next.find(c => c.id === currentChatId)) {
        return [{ id: currentChatId, title: input.slice(0, 20) || '新对话', messages: [{ role: 'user' as const, content: input }, { role: 'assistant' as const, content: '' }] }, ...next]
      }
      return next
    })

    // 开始流式请求
    const url = `/chat/sse?message=${encodeURIComponent(input)}&chatId=${encodeURIComponent(currentChatId)}&modelName=${encodeURIComponent(modelName)}`
    const es = new EventSource(url)
    eventSourceRef.current = es
    setStreaming(true)
    setInput('')

    es.onmessage = (ev: MessageEvent<string>) => {
      const chunk = ev.data || ''
      setChats(prev => prev.map(c => {
        if (c.id !== currentChatId) return c
        const msgs = [...c.messages]
        for (let i = msgs.length - 1; i >= 0; i--) {
          if (msgs[i].role === 'assistant') {
            msgs[i] = { ...msgs[i], content: (msgs[i].content || '') + chunk }
            break
          }
        }
        return { ...c, messages: msgs }
      }))
    }

    es.onerror = () => {
      stopStream()
    }

    es.onopen = () => {
      // 连接建立
    }
  }

  return (
    <div className="flex h-full min-h-0 bg-white">
      {/* 左侧会话与操作栏（保留会话，可删除） */}
      <div className="w-72 border-r border-gray-200 p-4 flex flex-col gap-3 overflow-hidden">
        <div className="text-lg font-semibold">Unity 助手</div>
        <button
          className="px-3 py-2 rounded-md bg-black text-white disabled:opacity-60"
          onClick={handleNewChat}
        >
          新建对话
        </button>
        <div className="text-xs text-gray-500">共 {chats.length} 个对话</div>
        <div className="mt-1 -mx-2 overflow-auto">
          {chats.map(chat => (
            <div
              key={chat.id}
              className={
                'mx-2 mb-2 rounded-md border flex items-center justify-between gap-2 px-3 py-2 cursor-pointer ' +
                (chat.id === activeChatId ? 'border-black bg-gray-50' : 'border-gray-200 hover:bg-gray-50')
              }
              onClick={() => setActiveChatId(chat.id)}
            >
              <div className="truncate text-sm flex-1" title={chat.title}>{chat.title || '新对话'}</div>
              <button
                className="text-gray-400 hover:text-red-500 px-2"
                title="删除对话"
                onClick={(e) => {
                  e.stopPropagation()
                  setChats(prev => prev.filter(c => c.id !== chat.id))
                  if (activeChatId === chat.id) {
                    setActiveChatId(prev => (prev === chat.id ? (chats.find(c => c.id !== chat.id)?.id || '') : prev))
                  }
                }}
              >
                …
              </button>
            </div>
          ))}
          {chats.length === 0 && (
            <div className="text-xs text-gray-400 px-2">暂无对话，点击“新建对话”。</div>
          )}
        </div>
      </div>

      {/* 右侧聊天区 */}
      <div className="flex-1 min-w-0 flex flex-col min-h-0">
        <div className="flex-1 min-h-0 overflow-auto p-6 space-y-4">
          {(!activeChatId || !chats.find(c => c.id === activeChatId)?.messages.length) && (
            <div className="text-center text-gray-400 mt-24">今天有什么可以帮助到你？</div>
          )}
          {chats
            .find(c => c.id === activeChatId)?.messages.map((m, idx) => (
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

        {/* 输入区（模型选择在发送左侧），固定在底部 */}
        <div className="border-t border-gray-200 p-4">
          <div className="flex items-end gap-2">
            <textarea
              className="flex-1 border border-gray-300 rounded-md p-3 min-h-[48px] max-h-40 resize-y"
              placeholder="输入你的问题…"
              value={input}
              onChange={e => setInput(e.target.value)}
              disabled={streaming}
            />
            <select
              className="border border-gray-300 rounded-md p-2"
              value={modelName}
              onChange={e => setModelName(e.target.value)}
              disabled={streaming}
            >
              {MODEL_OPTIONS.map(opt => (
                <option key={opt.value} value={opt.value}>{opt.label}</option>
              ))}
            </select>
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
          <div className="text-xs text-gray-400 mt-2">后端 SSE 接口：/chat/sse | 当前会话：{activeChatId || '未创建'}</div>
        </div>
      </div>
    </div>
  )
}
