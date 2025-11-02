import { useState } from 'react'
import { Card, Form, Input, Select, Button, message, Spin } from 'antd'
import { getVideoUrl } from '@/ui-backend/apis/text2video'

const { TextArea } = Input

export const VideoGenerator = () => {
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const [videoUrl, setVideoUrl] = useState<string>('')

  interface FormValues {
    prompt: string
    size?: string
    duration?: number
  }

  const onFinish = async (values: FormValues) => {
    try {
      setLoading(true)
      // 确保 duration 是数字类型
      const duration = typeof values.duration === 'number' ? values.duration : Number(values.duration) || 10
      console.log('发送参数:', { prompt: values.prompt, size: values.size || '832*480', duration })
      const response = await getVideoUrl(values.prompt, values.size || '832*480', duration)
      console.log(response)
      // 根据后端返回的数据结构获取URL
      // 假设返回格式为 { data: { data: "url" } } 或 { data: "url" }
      const url = response.data?.data || response.data
      if (url) {
        setVideoUrl(url)
        message.success('视频生成成功！')
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
              <TextArea rows={4} placeholder="请输入视频详细描述，例如：时崎狂三" />
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

        {videoUrl && !loading && (
          <Card>
            <div className="mb-4">
              <strong>提示词：</strong>
              <span className="ml-2">{form.getFieldValue('prompt')}</span>
            </div>
            <div className="flex justify-center">
              <video src={videoUrl} controls className="max-w-full h-auto" style={{ maxHeight: '600px' }}>
                您的浏览器不支持视频播放
              </video>
            </div>
            <div className="mt-4 text-center">
              <Button type="link" href={videoUrl} download target="_blank">
                下载
              </Button>
            </div>
          </Card>
        )}
      </div>
    </div>
  )
}
