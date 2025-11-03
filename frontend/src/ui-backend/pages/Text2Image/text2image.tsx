import { useState, useEffect } from 'react'
import { Card, Form, Input, Select, Button, message, Spin } from 'antd'
import { getImageUrl, getImageList, deleteImage } from '@/ui-backend/apis/text2image'

const { TextArea } = Input

interface ImageItem {
  imgUrl: string | undefined
  id: number
  message: string
}

export const ImageGenerator = () => {
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const [imageList, setImageList] = useState<ImageItem[]>([])
  const [listLoading, setListLoading] = useState(false)

  interface FormValues {
    prompt: string
    size?: string
    number?: number
  }

  // 获取图片列表
  const fetchImageList = async () => {
    try {
      setListLoading(true)
      const response = await getImageList()
      // 假设返回格式为 { data: [...] } 或 { data: { data: [...] } }
      let list = response.data?.data || response.data || []
      list = list.reverse()
      setImageList(Array.isArray(list) ? list : [])
    } catch (error: unknown) {
      console.error('获取图片列表失败:', error)
      message.error('获取图片列表失败')
    } finally {
      setListLoading(false)
    }
  }

  // 组件加载时获取图片列表
  useEffect(() => {
    fetchImageList()
  }, [])

  const onFinish = async (values: FormValues) => {
    try {
      setLoading(true)
      const response = await getImageUrl(values.prompt, values.size || '1080*1080', values.number || 1)
      console.log(response)
      // 根据后端返回的数据结构获取URL
      // 假设返回格式为 { data: { data: "url" } } 或 { data: "url" }
      const url = response.data?.data || response.data
      if (url) {
        message.success('图片生成成功！')
        // 生成成功后刷新列表
        await fetchImageList()
        form.resetFields()
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

  // 下载图片
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
    await deleteImage(id)
    await fetchImageList()
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

      {/* 下半部分：图片列表 */}
      <div className="flex-1">
        {loading && (
          <div className="flex justify-center items-center py-8">
            <Spin size="large" tip="生成中..." />
          </div>
        )}

        <Card title="图片列表" loading={listLoading}>
          {imageList.length === 0 && !listLoading ? (
            <div className="text-center text-gray-400 py-8">暂无图片</div>
          ) : (
            <div className="grid grid-cols-3 gap-6">
              {imageList.map((item) => (
                <div key={item.id} className="flex flex-col">
                  <div className="flex justify-center mb-4">
                    <img
                      src={item.imgUrl}
                      alt={item.message || '生成的图片'}
                      className="max-w-full h-auto rounded-lg shadow-md"
                    />
                  </div>
                  <div className="text flex flex-col justify-center items-center">
                    {'提示词：' + item.message}
                    <div>
                      <Button type="link" onClick={() => handleDownload(item.imgUrl || '')}>
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
