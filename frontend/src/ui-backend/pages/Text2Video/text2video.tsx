import { useEffect, useState } from 'react'
import { Card, Form, Input, Select, Button, message, Spin } from 'antd'
import { deleteVideo, getVideoList, getVideoUrl } from '@/ui-backend/apis/text2video'

const { TextArea } = Input

interface VideoItem {
  videoUrl: string | undefined
  id: number
  message: string
}

export const VideoGenerator = () => {
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const [videoList, setVideoList] = useState<VideoItem[]>([])
  const [listLoading, setListLoading] = useState(false)

  interface FormValues {
    prompt: string
    size?: string
    duration?: number
  }

  // 获取视频列表
  const fetchVideoList = async () => {
    try {
      setListLoading(true)
      const response = await getVideoList()
      // 假设返回格式为 { data: [...] } 或 { data: { data: [...] } }
      let list = response.data?.data || response.data || []
      list = list.reverse()
      setVideoList(Array.isArray(list) ? list : [])
    } catch (error: unknown) {
      console.error('获取视频列表失败:', error)
      message.error('获取视频列表失败')
    } finally {
      setListLoading(false)
    }
  }

  // 组件加载时获取视频列表
  useEffect(() => {
    fetchVideoList()
  }, [])

  const onFinish = async (values: FormValues) => {
    try {
      setLoading(true)
      // 确保 duration 是数字类型
      console.log('发送参数:', { prompt: values.prompt, size: values.size || '832*480', duration: values.duration || 10 })
      const response = await getVideoUrl(values.prompt, values.size || '832*480', Number(values.duration) || 10)
      console.log(response)
      // 根据后端返回的数据结构获取URL
      // 假设返回格式为 { data: { data: "url" } } 或 { data: "url" }
      const url = response.data?.data || response.data
      if (url) {
        message.success('视频生成成功！')
        // 生成成功后刷新列表
        await fetchVideoList()
        form.resetFields()
      } else {
        message.error('获取视频URL失败')
      }
    } catch (error: unknown) {
      console.error('生成视频失败:', error)
      const errorMessage =
        error && typeof error === 'object' && 'response' in error
          ? (error as { response?: { data?: { message?: string } } }).response?.data?.message
          : undefined
      message.error(errorMessage || '生成视频失败，请重试')
    } finally {
      setLoading(false)
    }
  }

  // 下载视频
  const handleDownload = (url: string) => {
    const link = document.createElement('a')
    link.href = url
    link.download = ''
    link.target = '_blank'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  }
  const handleDelete = async (id: number) => {
    await deleteVideo(id)
    await fetchVideoList()
  }

  return (
    <div className="flex flex-col h-full p-8">
      {/* 上半部分：输入表单 */}
      <div className="mb-8">
        <Card>
          <Form
            form={form}
            layout="vertical"
            onFinish={onFinish}
            initialValues={{
              size: '832*480',
              duration: 10
            }}>
            <Form.Item label="提示词" name="prompt" rules={[{ required: true, message: '请输入提示词' }]}>
              <TextArea
                rows={4}
                placeholder="请输入视频详细描述，例如：一幅史诗级可爱的场景。一只小巧可爱的卡通小猫将军，身穿细节精致的金色盔甲，头戴一个稍大的头盔，勇敢地站在悬崖上。他骑着一匹虽小但英勇的战马，说：“青海长云暗雪山，孤城遥望玉门关。黄沙百战穿金甲，不破楼兰终不还”。悬崖下方，一支由老鼠组成的、数量庞大、无穷无尽的军队正带着临时制作的武器向前冲锋。这是一个戏剧性的、大规模的战斗场景，灵感来自中国古代的战争史诗。远处的雪山上空，天空乌云密布。整体氛围是“可爱”与“霸气”的搞笑和史诗般的融合。"
              />
            </Form.Item>

            <Form.Item label="分辨率" name="size">
              <Select>
                <Select.Option value="1920*1080">1920*1080</Select.Option>
                <Select.Option value="1280*720">1280*720</Select.Option>
                <Select.Option value="832*480">832*480</Select.Option>
              </Select>
            </Form.Item>

            <Form.Item label="持续时长" name="duration">
              <Select>
                <Select.Option value={10}>10</Select.Option>
                <Select.Option value={5}>5</Select.Option>
                <Select.Option value={1}>1</Select.Option>
              </Select>
            </Form.Item>

            <Form.Item>
              <Button type="primary" htmlType="submit" loading={loading} block>
                生成视频
              </Button>
            </Form.Item>
          </Form>
        </Card>
      </div>

      {/* 下半部分：显示视频 */}
      <div className="flex-1">
        {loading && (
          <div className="flex justify-center items-center py-8">
            <Spin size="large" tip="生成中..." />
          </div>
        )}

        <Card title="视频列表" loading={listLoading}>
          {videoList.length === 0 && !listLoading ? (
            <div className="text-center text-gray-400 py-8">暂无视频</div>
          ) : (
            <div className="grid grid-cols-3 gap-6">
              {videoList.map((item) => (
                <div key={item.id} className="flex flex-col">
                  <div className="flex justify-center mb-4">
                    <video src={item.videoUrl} controls className="max-w-full h-auto rounded-lg shadow-md" />
                  </div>
                  <div className="text flex flex-col justify-center items-center">
                    {'提示词：' + (item.message.length > 20 ? item.message.slice(0, 20) + '...' : item.message)}
                    <div>
                      <Button type="link" onClick={() => handleDownload(item.videoUrl || '')}>
                        下载
                      </Button>
                      <Button type="link" onClick={() => handleDelete(item.id)}>
                        删除
                      </Button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </Card>
      </div>
    </div>
  )
}
