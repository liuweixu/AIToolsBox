import { DeleteOutlined } from '@ant-design/icons';
import { Conversations } from '@ant-design/x';
import type { ConversationsProps } from '@ant-design/x';
import { App, type GetProp, theme } from 'antd';
import InfiniteScroll from 'react-infinite-scroll-component';

const items: GetProp<ConversationsProps, 'items'> = Array.from({ length: 20 }).map((_, index) => ({
  key: `item${index + 1}`,
  label: `Conversation Item ${index + 1}`,
  disabled: index === 3,
}));

export const Conversation = () => {
  const { message } = App.useApp();
  const { token } = theme.useToken();

  const style = {
    width: '100%',
    background: token.colorBgContainer,
    borderRadius: token.borderRadius,
  };

  const menuConfig: ConversationsProps['menu'] = (conversation) => ({
    items: [
      {
        label: '删除对话',
        key: 'deleteConversation',
        icon: <DeleteOutlined />,
        danger: true,
      },
    ],
    onClick: (menuInfo) => {
      menuInfo.domEvent.stopPropagation();
      message.info(`Click ${conversation.key} - ${menuInfo.key}`);
    },
  });

  return (
    <div id="scrollableDiv" style={{ height: '100%', overflow: 'auto' }}>
        <InfiniteScroll 
            dataLength={items.length} 
            next={() => {}} 
            hasMore={items.length < 10}
            loader={<div>Loading...</div>} 
            scrollableTarget="scrollableDiv">
        <Conversations defaultActiveKey="item1" menu={menuConfig} items={items} style={style} />
        </InfiniteScroll>
    </div>
  )
};