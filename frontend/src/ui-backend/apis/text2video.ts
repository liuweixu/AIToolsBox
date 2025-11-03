import { request } from "@/ui-backend/utils";

// 生成视频，获取视频URL
// 视频生成可能需要较长时间，禁用超时限制
export function getVideoUrl(prompt: string, size: string, duration: number) {
    return request({
        url: "/api/multimodal/text2video/generate",
        method: "GET",
        params: {
            prompt: prompt,
            size: size,
            duration: duration
        },
        timeout: 0 // 禁用超时，允许长时间等待
    })
}

// 获取生成视频历史列表
export function getVideoList() {
    return request({
        url: "/api/multimodal/text2video/list",
        method: "GET",
        timeout: 0 // 禁用超时，允许长时间等待
    })
}

// 删除视频
export function deleteVideo(id: number) {
    return request({
        url: "/api/multimodal/text2video/delete",
        method: "POST",
        data: {
            id: id
        }
    })
}

