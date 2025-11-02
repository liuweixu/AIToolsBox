import { useState } from 'react'
import { Card, Form, Input, Select, Button, message, Spin } from 'antd'
import { getImageUrl } from '@/ui-backend/apis/text2image'

const { TextArea } = Input

export const ImageGenerator = () => {
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const [imageUrl, setImageUrl] = useState<string>('')

  interface FormValues {
    prompt: string
    size?: string
    number?: number
  }

  const onFinish = async (values: FormValues) => {
    try {
      setLoading(true)
      const response = await getImageUrl(values.prompt, values.size || '1080*1080', values.number || 1)
      console.log(response)
      // 根据后端返回的数据结构获取URL
      // 假设返回格式为 { data: { data: "url" } } 或 { data: "url" }
      const url = response.data?.data || response.data
      if (url) {
        setImageUrl(url)
        message.success('图片生成成功！')
      } else {
        message.error('获取图片URL失败')
      }
    } catch (error: unknown) {
      console.error('生成图片失败:', error)
      const errorMessage =
        error && typeof error === 'object' && 'response' in error
          ? (error as { response?: { data?: { message?: string } } }).response?.data?.message
          : undefined
      message.error(errorMessage || '生成图片失败，请重试')
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
              size: '1664*928',
              number: 1
            }}>
            <Form.Item label="提示词" name="prompt" rules={[{ required: true, message: '请输入提示词' }]}>
              <TextArea rows={4} placeholder="请输入图片描述，例如：一张柯南与灰原哀图像" />
            </Form.Item>

            <Form.Item label="分辨率" name="size">
              <Select>
                <Select.Option value="1664*928">1664*928</Select.Option>
                <Select.Option value="1472*1140">1472*1140</Select.Option>
                <Select.Option value="1328*1328">1328*1328</Select.Option>
                <Select.Option value="1140*1472">1140*1472</Select.Option>
                <Select.Option value="928*1664">928*1664</Select.Option>
              </Select>
            </Form.Item>

            <Form.Item>
              <Button type="primary" htmlType="submit" loading={loading} block>
                生成图片
              </Button>
            </Form.Item>
          </Form>
        </Card>
      </div>

      {/* 下半部分：显示图片 */}
      <div className="flex-1">
        {loading && (
          <div className="flex justify-center items-center py-8">
            <Spin size="large" tip="生成中..." />
          </div>
        )}

        {imageUrl && !loading && (
          <Card>
            <div className="mb-4">
              <strong>提示词：</strong>
              <span className="ml-2">{form.getFieldValue('prompt')}</span>
            </div>
            <div className="flex justify-center">
              <img src={imageUrl} alt="生成的图片" className="max-w-full h-auto" />
            </div>
            <div className="mt-4 text-center">
              <Button type="link" href={imageUrl} download target="_blank">
                下载
              </Button>
            </div>
          </Card>
        )}
      </div>
    </div>
  )
}
