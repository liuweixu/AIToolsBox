import { request } from "@/ui-backend/utils";

// 生成图片，获取图片URL
// 图片生成可能需要较长时间，禁用超时限制
export function getImageUrl(prompt: string, size: string, number: number) {
    return request({
        url: "/api/multimodal/text2image/",
        method: "GET",
        params: {
            prompt: prompt,
            size: size,
            number: number
        },
        timeout: 0 // 禁用超时，允许长时间等待
    })
}

