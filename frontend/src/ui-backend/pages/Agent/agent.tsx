import { useRef } from 'react'
import { Button, Input, Select, Space } from 'antd'
import { PaperClipOutlined, SendOutlined } from '@ant-design/icons'
import { marked } from 'marked'
import 'github-markdown-css/github-markdown.css'
import './style.css'
import { createChatAgent, getChatAgentHistory } from '@/ui-backend/apis/agent'
import { useParams, useNavigate, useLocation } from 'react-router-dom'

import { DeleteOutlined } from '@ant-design/icons'
import { Conversations } from '@ant-design/x'
import type { ConversationsProps } from '@ant-design/x'
import { theme, type GetProp } from 'antd'
import { useEffect, useState, useCallback, useRef as useReactRef } from 'react'
import InfiniteScroll from 'react-infinite-scroll-component'
import { deleteChatAgent, getChatAgentList } from '@/ui-backend/apis/agent'

const { TextArea } = Input

// 配置 marked
marked.setOptions({
  breaks: true, // 支持换行
  gfm: true // 支持 GitHub 风格的 Markdown
})

type Message = {
  role: 'user' | 'assistant'
  content: string
  createTime?: string // 消息创建时间，用于分页
}

type Chat = {
  id: string
  title: string
  messages: Message[]
  // 分页信息（基于时间戳）
  hasMore: boolean
  loadingHistory: boolean
  lastCreateTime?: string // 最早一条消息的创建时间，用于加载更多
}

type HistoryRecord = {
  id: string
  message: string
  messageType: 'user' | 'assistant'
  agentId: string
  createTime: string
  updateTime: string
  isDelete: number
}

const MODEL_OPTIONS = [
  { label: '阿里', value: 'DASHSCOPE' },
  { label: 'Ollama', value: 'OLLAMA' },
  { label: '智谱', value: 'ZHIPUAI' },
  { label: 'DeepSeek', value: 'DEEPSEEK' }
]

// 处理消息内容，如果"结果"后面的内容太长，只保留前50个字
const truncateAfterResult = (content: string): string => {
  if (!content) return content

  // 查找"结果"关键字的位置（支持"结果"、"结果:"、"结果："等格式）
  const resultPattern = /结果[：:]?\s*/
  const resultMatch = content.search(resultPattern)

  if (resultMatch === -1) {
    // 如果没有找到"结果"，直接返回原内容
    return content
  }

  // 找到"结果"后面的内容的起始位置（跳过"结果"和可能的冒号及空白字符）
  const matchResult = content.match(resultPattern)
  if (!matchResult) {
    return content
  }

  const resultPrefixEnd = resultMatch + matchResult[0].length
  const beforeResult = content.substring(0, resultPrefixEnd) // "结果"及其后面的冒号和空白
  const afterResult = content.substring(resultPrefixEnd) // "结果"后面的实际内容

  // 如果"结果"后面的内容超过50个字，截断并添加省略号
  if (afterResult.length > 200) {
    return beforeResult + afterResult.substring(0, 200) + '...'
  }

  // 如果不超过50个字，直接返回原内容
  return content
}

export const AgentHelper = () => {
  const { id } = useParams<{ id?: string }>()
  const navigate = useNavigate()
  const location = useLocation()
  const [chats, setChats] = useState<Chat[]>([])
  const [activeChatId, setActiveChatId] = useState<string>(id || '')
  const [modelName, setModelName] = useState<string>(MODEL_OPTIONS[0].value)
  const [input, setInput] = useState<string>('')
  const [streaming, setStreaming] = useState<boolean>(false)
  const eventSourceRef = useRef<EventSource | null>(null)
  const [refreshTrigger, setRefreshTrigger] = useState<number>(0)
  const chatScrollRef = useRef<HTMLDivElement | null>(null)

  // 加载历史记录（首次加载或加载更多）
  const getHistory = async (id: string, lastCreateTime?: string, append: boolean = false) => {
    if (!id) return
    try {
      const currentChat = chats.find((c) => c.id === id)

      // 如果正在加载，避免重复请求
      if (currentChat?.loadingHistory) return

      // 设置加载状态
      setChats((prev) =>
        prev.map((c) => {
          if (c.id === id) {
            return { ...c, loadingHistory: true }
          }
          return c
        })
      )

      const res = await getChatAgentHistory(id, 10, lastCreateTime)
      const historyData = res.data.data
      console.log('获取对话历史', historyData)

      // 将后端返回的历史记录转换为前端的 Message 格式
      const newMessages: Message[] = (historyData.records || [])
        .map((record: HistoryRecord) => ({
          role: record.messageType === 'user' ? 'user' : 'assistant',
          content: record.message || '',
          createTime: record.createTime
        }))
        .reverse() // 反转数组，因为后端返回的是最新的在前，前端需要旧的在前

      // 判断是否还有更多数据：如果返回的记录数等于 PageSize，可能还有更多
      // 如果返回的记录数小于 PageSize，说明已经到底了
      const pageSize = 10
      const hasMore = (historyData.records || []).length >= pageSize

      // 获取最早一条消息的创建时间，作为下次加载的 lastCreateTime
      const earliestCreateTime = newMessages.length > 0 ? newMessages[0].createTime : undefined

      // 更新 chats state
      setChats((prev) => {
        const existingChat = prev.find((c) => c.id === id)
        if (existingChat) {
          if (append) {
            // 追加模式：将新消息添加到现有消息的前面（因为是更早的历史）
            return prev.map((c) => {
              if (c.id === id) {
                return {
                  ...c,
                  messages: [...newMessages, ...c.messages],
                  hasMore,
                  lastCreateTime: earliestCreateTime,
                  loadingHistory: false
                }
              }
              return c
            })
          } else {
            // 首次加载或重置：只有当本地消息为空时才设置历史记录
            if (existingChat.messages.length === 0 || !existingChat.messages.some((m) => m.content)) {
              return prev.map((c) => {
                if (c.id === id) {
                  return {
                    ...c,
                    messages: newMessages,
                    hasMore,
                    lastCreateTime: earliestCreateTime,
                    loadingHistory: false
                  }
                }
                return c
              })
            }
            // 如果已有消息，只更新分页信息
            return prev.map((c) => {
              if (c.id === id) {
                return { ...c, hasMore, lastCreateTime: earliestCreateTime, loadingHistory: false }
              }
              return c
            })
          }
        } else {
          // 如果会话不存在，创建新会话并添加历史记录
          return [
            ...prev,
            {
              id,
              title: '对话',
              messages: newMessages,
              hasMore,
              lastCreateTime: earliestCreateTime,
              loadingHistory: false
            }
          ]
        }
      })
    } catch (error) {
      console.error('获取对话历史失败:', error)
      // 清除加载状态
      setChats((prev) =>
        prev.map((c) => {
          if (c.id === id) {
            return { ...c, loadingHistory: false }
          }
          return c
        })
      )
    }
  }

  // 使用 ref 来存储 chats，避免闭包问题
  const chatsRef = useReactRef(chats)
  useEffect(() => {
    chatsRef.current = chats
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [chats])

  // 加载更多历史记录
  const loadMoreHistory = useCallback(async () => {
    if (!activeChatId) return
    const currentChat = chatsRef.current.find((c) => c.id === activeChatId)
    if (!currentChat || !currentChat.hasMore || currentChat.loadingHistory) return

    // 保存当前滚动位置（从底部计算）
    const scrollContainer = chatScrollRef.current
    const previousScrollHeight = scrollContainer?.scrollHeight || 0

    // 使用当前最早消息的创建时间作为 lastCreateTime
    const lastCreateTime = currentChat.lastCreateTime
    await getHistory(activeChatId, lastCreateTime, true)

    // 加载完成后，恢复滚动位置
    setTimeout(() => {
      if (scrollContainer) {
        const newScrollHeight = scrollContainer.scrollHeight
        const scrollDiff = newScrollHeight - previousScrollHeight
        scrollContainer.scrollTop = scrollContainer.scrollTop + scrollDiff
      }
    }, 100)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeChatId])

  // 监听滚动事件，实现向上滚动加载更多
  useEffect(() => {
    const scrollContainer = chatScrollRef.current
    if (!scrollContainer) return

    let loading = false
    const handleScroll = () => {
      // 当滚动到顶部附近（距离顶部小于100px）时，加载更多
      if (scrollContainer.scrollTop < 100 && !loading) {
        loading = true
        loadMoreHistory().finally(() => {
          loading = false
        })
      }
    }

    scrollContainer.addEventListener('scroll', handleScroll)
    return () => {
      scrollContainer.removeEventListener('scroll', handleScroll)
    }
  }, [activeChatId, loadMoreHistory])
  useEffect(() => {
    if (activeChatId) {
      getHistory(activeChatId, undefined, false)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeChatId])

  // 监听路由参数变化，同步到 activeChatId
  useEffect(() => {
    if (id) {
      // 验证会话是否存在
      getChatAgentList()
        .then((res) => {
          // eslint-disable-next-line @typescript-eslint/no-explicit-any
          const chatIds = res.data.data.map((item: any) => item.id)
          if (chatIds.includes(id)) {
            // 会话存在，设置 activeChatId
            setActiveChatId(id)
            // 如果本地没有该会话的消息，初始化一个空的会话数据结构
            setChats((prev) => {
              if (!prev.find((c) => c.id === id)) {
                return [
                  ...prev,
                  {
                    id,
                    title: '对话',
                    messages: [],
                    hasMore: false,
                    loadingHistory: false
                  }
                ]
              }
              return prev
            })
          } else {
            // 会话不存在，重定向到 /agent
            console.warn(`会话 ${id} 不存在，重定向到 /agent`)
            navigate('/agent', { replace: true })
            setActiveChatId('')
          }
        })
        .catch((err) => {
          console.error('获取会话列表失败:', err)
          // 出错时也重定向到 /agent
          navigate('/agent', { replace: true })
          setActiveChatId('')
        })
    } else {
      setActiveChatId('')
    }
  }, [id, navigate])

  // 当 activeChatId 改变时，同步更新 URL
  useEffect(() => {
    // 只有当 activeChatId 和 id 不一致时才更新 URL，避免循环更新
    if (activeChatId && activeChatId !== id) {
      navigate(`/agent/${activeChatId}`, { replace: true })
    } else if (!activeChatId && id && id !== '') {
      // 只有当 id 存在但 activeChatId 为空时才跳转，避免删除时的循环
      // 添加 id !== '' 检查，确保不会在已经导航到 /agent 时再次导航
      navigate('/agent', { replace: true })
    }
  }, [activeChatId, id, navigate])

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
    // 清空当前激活的会话
    setActiveChatId('')
    // 清空输入框
    setInput('')
    // 如果当前不在 /agent，则跳转；如果已经在 /agent，强制刷新状态
    if (location.pathname !== '/agent') {
      navigate('/agent', { replace: true })
    } else {
      // 如果已经在 /agent，强制重置状态（通过设置一个临时值再清空来触发更新）
      setActiveChatId('')
      setInput('')
    }
  }

  const stopStream = () => {
    if (eventSourceRef.current) {
      eventSourceRef.current.close()
      eventSourceRef.current = null
    }
    setStreaming(false)
  }

  const handleSend = async () => {
    if (!input.trim()) return
    let currentChatId = activeChatId
    const messageInput = input

    // 如果没有会话则自动新建一个，并等待完成，同时传入第一个消息
    if (!currentChatId) {
      const res = await createChatAgent(messageInput)
      currentChatId = res.data.data.id // 修正：使用 res.data.data.id，与 handleNewChat 保持一致
      setActiveChatId(currentChatId)
      // 先添加到 chats 中
      setChats((prev) => [
        ...prev,
        {
          id: currentChatId,
          title: '新对话',
          messages: [],
          hasMore: false,
          loadingHistory: false
        }
      ])
      setRefreshTrigger((prev) => prev + 1)
      // 跳转到新会话的 URL
      navigate(`/agent/${currentChatId}`, { replace: true })
    }

    // 先落地用户消息到指定会话
    setChats((prev) => {
      const next = prev.map((c) => {
        if (c.id !== currentChatId) return c
        const updatedMessages: Message[] = [...c.messages, { role: 'user' as const, content: messageInput }]
        const updatedTitle = c.title === '新对话' ? messageInput.slice(0, 20) || '新对话' : c.title
        return { ...c, messages: updatedMessages, title: updatedTitle }
      })
      // 若是刚刚自动创建的会话（active 但列表里还没有），补充创建
      if (!next.find((c) => c.id === currentChatId)) {
        return [
          {
            id: currentChatId,
            title: messageInput.slice(0, 20) || '新对话',
            messages: [{ role: 'user' as const, content: messageInput }],
            hasMore: false,
            loadingHistory: false
          },
          ...next
        ]
      }
      return next
    })

    // 用于存储当前正在构建的消息缓冲区（处理分块数据）
    let messageBuffer = ''
    let currentStepNumber: number | null = null

    // 开始流式请求 - 使用 agent 的 SSE 端点
    const url = `/api/agent/manus/chat?message=${encodeURIComponent(messageInput)}&agentId=${encodeURIComponent(
      currentChatId
    )}&modelName=${encodeURIComponent(modelName)}`
    const es = new EventSource(url)
    eventSourceRef.current = es
    setStreaming(true)
    setInput('')
    es.onmessage = (ev: MessageEvent<string>) => {
      try {
        // 后端返回的格式可能是纯文本，如 "Step 1: xxx"
        // 也可能是 JSON 格式：{"datastream": "实际内容"}
        const rawData = ev.data || ''
        let chunk = ''

        // 尝试解析 JSON
        try {
          const data = JSON.parse(rawData)
          chunk = data.datastream !== undefined ? data.datastream : rawData
        } catch {
          // 如果不是 JSON，直接使用原始数据
          chunk = rawData
        }

        if (!chunk.trim()) return

        // 将新数据追加到缓冲区
        messageBuffer += chunk

        // 检测是否包含新的 step（格式如 "Step 1:"、"Step 2:" 等）
        // 使用正则表达式匹配完整的 step 格式
        const stepRegex = /Step\s+(\d+):\s*([\s\S]*?)(?=Step\s+\d+:|$)/g
        let match
        let lastMatchEnd = 0
        let hasNewStep = false

        // 重置正则表达式
        stepRegex.lastIndex = 0

        // 查找所有完整的 step
        while ((match = stepRegex.exec(messageBuffer)) !== null) {
          const stepNum = parseInt(match[1], 10)
          const stepContent = match[2]
          lastMatchEnd = stepRegex.lastIndex

          // 如果是新的 step（step 号不同），创建新消息
          if (currentStepNumber === null || stepNum !== currentStepNumber) {
            hasNewStep = true
            currentStepNumber = stepNum

            setChats((prev) =>
              prev.map((c) => {
                if (c.id !== currentChatId) return c
                return {
                  ...c,
                  messages: [...c.messages, { role: 'assistant' as const, content: `Step ${stepNum}: ${stepContent}` }]
                }
              })
            )
          } else {
            // 同一个 step 的更新，更新最后一条 assistant 消息
            setChats((prev) =>
              prev.map((c) => {
                if (c.id !== currentChatId) return c
                const msgs = [...c.messages]
                const lastIndex = msgs.length - 1
                if (msgs[lastIndex]?.role === 'assistant') {
                  msgs[lastIndex] = {
                    ...msgs[lastIndex],
                    content: `Step ${stepNum}: ${stepContent}`
                  }
                }
                return { ...c, messages: msgs }
              })
            )
          }
        }

        // 如果没有匹配到完整的 step，但缓冲区开头有 "Step X:" 格式
        if (!hasNewStep && messageBuffer.trim()) {
          const incompleteStepMatch = messageBuffer.match(/^Step\s+(\d+):\s*(.*)$/s)
          if (incompleteStepMatch) {
            const stepNum = parseInt(incompleteStepMatch[1], 10)

            // 如果是新的 step，创建新消息
            if (currentStepNumber === null || stepNum !== currentStepNumber) {
              currentStepNumber = stepNum
              setChats((prev) =>
                prev.map((c) => {
                  if (c.id !== currentChatId) return c
                  return {
                    ...c,
                    messages: [...c.messages, { role: 'assistant' as const, content: messageBuffer }]
                  }
                })
              )
            } else {
              // 更新当前 step 的内容
              setChats((prev) =>
                prev.map((c) => {
                  if (c.id !== currentChatId) return c
                  const msgs = [...c.messages]
                  const lastIndex = msgs.length - 1
                  if (msgs[lastIndex]?.role === 'assistant') {
                    msgs[lastIndex] = {
                      ...msgs[lastIndex],
                      content: messageBuffer
                    }
                  }
                  return { ...c, messages: msgs }
                })
              )
            }
          } else {
            // 可能是 step 内容的继续部分，追加到最后一个 assistant 消息
            setChats((prev) =>
              prev.map((c) => {
                if (c.id !== currentChatId) return c
                const msgs = [...c.messages]
                const lastIndex = msgs.length - 1
                if (msgs[lastIndex]?.role === 'assistant') {
                  msgs[lastIndex] = {
                    ...msgs[lastIndex],
                    content: (msgs[lastIndex].content || '') + chunk
                  }
                } else {
                  // 如果还没有 assistant 消息，创建一个
                  msgs.push({ role: 'assistant' as const, content: chunk })
                  currentStepNumber = null // 重置 step 号
                }
                return { ...c, messages: msgs }
              })
            )
          }
        }

        // 清除已处理的数据（保留未完整的部分）
        if (lastMatchEnd > 0 && lastMatchEnd < messageBuffer.length) {
          messageBuffer = messageBuffer.substring(lastMatchEnd)
        } else if (lastMatchEnd >= messageBuffer.length) {
          messageBuffer = ''
        }
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

  const { token } = theme.useToken()
  const [chatList, setChatList] = useState<GetProp<ConversationsProps, 'items'>>([])

  const style = {
    width: '100%',
    background: token.colorBgContainer,
    borderRadius: token.borderRadius
  }

  // 提取获取列表的逻辑为独立函数
  const getChatList = async () => {
    const res = await getChatAgentList()
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const data = res.data.data.map((item: any) => ({
      key: item.id,
      label: `${item.summary}`,
      disabled: false
    }))
    setChatList(data)
  }

  // 初始化加载：只在组件挂载时执行
  useEffect(() => {
    getChatList()
  }, [])

  // 监听刷新触发器：当 refreshTrigger 变化时刷新
  useEffect(() => {
    if (refreshTrigger !== undefined) {
      getChatList()
    }
  }, [refreshTrigger])

  const menuConfig: ConversationsProps['menu'] = (conversation) => ({
    items: [
      {
        label: '删除对话',
        key: 'deleteConversation',
        icon: <DeleteOutlined />,
        danger: true
      }
    ],
    onClick: (menuInfo) => {
      menuInfo.domEvent.stopPropagation()
      const deletedChatId = conversation.key
      const isActiveChat = deletedChatId === activeChatId

      // 如果删除的是当前激活的会话，先清空 activeChatId（会触发 useEffect 自动导航）
      if (isActiveChat) {
        setActiveChatId('')
        // 同时从本地 chats 中删除
        setChats((prev) => prev.filter((c) => c.id !== deletedChatId))
      }

      // 更新列表（先更新，避免列表显示延迟）
      setChatList((prev) => prev.filter((c) => c.key !== deletedChatId))

      deleteChatAgent(deletedChatId)
        .then(() => {
          // 重新获取列表以同步最新状态
          return getChatList()
        })
        .catch((err) => {
          console.log(err)
          // 如果删除失败，恢复列表
          getChatList()
        })
    }
  })

  // 处理会话切换 - 使用 onActiveChange
  const handleActiveChange = (chatId: string) => {
    setActiveChatId(chatId)
    navigate(`/agent/${chatId}`)
  }

  return (
    <div className="flex h-[calc(100vh-96px)] min-h-0 bg-white">
      {/* 左侧会话与操作栏（保留会话，可删除） */}
      <div className="w-72 border-r border-gray-200 p-4 flex flex-col gap-3 min-h-0 bg-gray-100">
        <div className="text-lg font-semibold flex-shrink-0">AI 超级智能体</div>
        <Button
          size="large"
          onClick={handleNewChat}
          className="w-full !bg-white !text-gray-900 !border-0 !rounded-full !shadow-sm hover:!bg-gray-50 !h-auto !py-3 !px-6 !flex !items-center !justify-center !gap-2 flex-shrink-0">
          开启新智能体
        </Button>
        <div className="text-xs text-gray-500 flex-shrink-0"> 共计 {chatList.length} 个智能体</div>
        <div className="flex-1 min-h-0">
          <div id="scrollableDiv" style={{ height: '100%', overflow: 'auto' }}>
            <InfiniteScroll
              dataLength={chatList.length}
              hasMore={false}
              next={() => {}}
              loader={<div>Loading...</div>}
              scrollableTarget="scrollableDiv">
              <Conversations
                className="!bg-gray-100"
                activeKey={activeChatId}
                onActiveChange={handleActiveChange}
                menu={menuConfig}
                items={chatList}
                style={style}
              />
            </InfiniteScroll>
          </div>
        </div>
      </div>

      {/* 右侧聊天区 */}
      <div className="flex-1 min-w-0 flex flex-col min-h-0">
        <div ref={chatScrollRef} className="flex-1 min-h-0 overflow-auto p-6 space-y-4">
          {(() => {
            const currentChat = chats.find((c) => c.id === activeChatId)
            const messages = currentChat?.messages || []
            const hasMore = currentChat?.hasMore || false
            const loadingHistory = currentChat?.loadingHistory || false

            if (!activeChatId || messages.length === 0) {
              return <div className="text-center text-gray-400 mt-24">今天有什么可以帮助到你？</div>
            }

            return (
              <>
                {hasMore && (
                  <div className="text-center text-gray-400 py-4">
                    {loadingHistory ? '加载历史记录中...' : <div className="text-xs text-gray-400">向上滚动加载更多历史</div>}
                  </div>
                )}
                {messages.map((m, idx) => (
                  <div key={idx} className={m.role === 'user' ? 'text-right' : 'text-left'}>
                    <div
                      className={
                        'inline-block max-w-[80%] rounded-2xl px-4 py-2 ' +
                        (m.role === 'user' ? 'bg-blue-200 text-black' : 'bg-gray-100 text-black')
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
                            __html: marked.parse(truncateAfterResult(m.content)) as string
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
              </>
            )
          })()}
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
            <div className="text-xs text-gray-400">当前会话：{activeChatId || '未创建'}</div>
          </div>
        </div>
      </div>
    </div>
  )
}
