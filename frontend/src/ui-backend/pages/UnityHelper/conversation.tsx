import { DeleteOutlined } from '@ant-design/icons'
import { Conversations } from '@ant-design/x'
import type { ConversationsProps } from '@ant-design/x'
import { theme, type GetProp } from 'antd'
import { useEffect, useState } from 'react'
import InfiniteScroll from 'react-infinite-scroll-component'
import { deleteChatUnity, getChatUnityList } from '@/ui-backend/apis/unity'

interface ConversationProps {
  refreshTrigger?: number // 添加刷新触发器
}

export const Conversation = ({ refreshTrigger }: ConversationProps) => {
  const { token } = theme.useToken()
  const [chatList, setChatList] = useState<GetProp<ConversationsProps, 'items'>>([])

  const style = {
    width: '100%',
    background: token.colorBgContainer,
    borderRadius: token.borderRadius
  }

  // 提取获取列表的逻辑为独立函数
  const getChatList = async () => {
    const res = await getChatUnityList()
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const data = res.data.data.map((item: any) => ({
      key: item.id,
      label: `对话${item.summary}`,
      disabled: false
    }))
    console.log(data)
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
      console.log(conversation.key)
      deleteChatUnity(conversation.key)
        .then(() => {
          return getChatList()
        })
        .catch((err) => {
          console.log(err)
        })
    }
  })

  return (
    <div id="scrollableDiv" style={{ height: '100%', overflow: 'auto' }}>
      <InfiniteScroll
        dataLength={chatList.length}
        hasMore={false}
        next={() => {}}
        loader={<div>Loading...</div>}
        scrollableTarget="scrollableDiv">
        <Conversations defaultActiveKey="item1" menu={menuConfig} items={chatList} style={style} />
      </InfiniteScroll>
    </div>
  )
}
