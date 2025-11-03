import { Card } from 'antd'
import { useNavigate } from 'react-router-dom'

const App = () => {
  const navigate = useNavigate()
  return (
    <div className="flex flex-col justify-center items-center h-full p-8">
      <h1 className="text-7xl font-bold mb-12" style={{ fontFamily: 'KaiTi, STKaiti, 楷体, serif' }}>
        AI创智工坊
      </h1>
      <div className="flex items-center gap-8">
        <div style={{ marginTop: 0 }}>
          <Card
            hoverable
            style={{ width: 240 }}
            cover={<img draggable={true} alt="example" src="https://api.r10086.com/樱道随机图片api接口.php?图片系列=风景系列1" />}
            onClick={() => navigate('/unityhelper')}>
            <Card.Meta title="Unity助手" description="可以就Unity和C#遇到的问题与AI对话" />
          </Card>
        </div>
        <div style={{ marginTop: '100px' }}>
          <Card
            hoverable
            style={{ width: 240 }}
            cover={<img draggable={true} alt="example" src="https://api.r10086.com/樱道随机图片api接口.php?图片系列=风景系列4" />}
            onClick={() => navigate('/agent')}>
            <Card.Meta title="AI超级智能体" description="提出一个需求，智能体帮你做事" />
          </Card>
        </div>
        <div style={{ marginTop: 0 }}>
          <Card
            hoverable
            style={{ width: 240 }}
            cover={<img draggable={true} alt="example" src="https://api.r10086.com/樱道随机图片api接口.php?图片系列=风景系列7" />}
            onClick={() => navigate('/text2image')}>
            <Card.Meta title="文字生图片" description="通过输入文字，生成精美图片" />
          </Card>
        </div>
        <div style={{ marginTop: '100px' }}>
          <Card
            hoverable
            style={{ width: 240 }}
            cover={<img draggable={true} alt="example" src="https://api.r10086.com/樱道随机图片api接口.php?图片系列=风景系列6" />}
            onClick={() => navigate('/text2video')}>
            <Card.Meta title="文字生视频" description="通过输入文字，生成生动影片" />
          </Card>
        </div>
      </div>
    </div>
  )
}

export default App
