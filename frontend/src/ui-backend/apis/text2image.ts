import { request } from "@/ui-backend/utils";

// 生成图片，获取图片URL
// 图片生成可能需要较长时间，禁用超时限制
export function getImageUrl(prompt: string, size: string, number: number) {
    return request({
        url: "/api/multimodal/text2image/generate",
        method: "GET",
        params: {
            prompt: prompt,
            size: size,
            number: number
        },
        timeout: 0 // 禁用超时，允许长时间等待
    })
}

// 获取生成图片历史列表
export function getImageList() {
    return request({
        url: "/api/multimodal/text2image/list",
        method: "GET",
        timeout: 0 // 禁用超时，允许长时间等待
    })
}

// 删除图片
export function deleteImage(id: number) {
    return request({
        url: "/api/multimodal/text2image/delete",
        method: "POST",
        data: {
            id: id
        }
    })
}
