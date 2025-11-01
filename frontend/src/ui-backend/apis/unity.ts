import { request } from "@/ui-backend/utils";

// 1. 在Unity助手创建对话框
export function createChatUnity(message: string){
    return request({
        url: "/api/unity/chat/create",
        method: "POST",
        data:  { message: message }
    })
}

// 2. 在Unity助手删除对话框
export function deleteChatUnity(id: string) {
    return request({
        url: "/api/unity/chat/delete",
        method: "POST",
        data: {id: id}
    })
}

// 3. 获取Unity对话列表
export function getChatUnityList() {
    return request({
        url: "/api/unity/chat/list",
        method: "GET"
    })
}

// 4. 获取Unity对话历史
export function getChatUnityHistory(id: string, pageSize: number = 10, lastCreateTime?: string) {
    const params: Record<string, string | number> = {
        PageSize: pageSize
    }
    if (lastCreateTime) {
        params.lastCreateTime = lastCreateTime
    }
    return request({
        url: `/api/unity/history/${id}`,
        method: "GET",
        params
    })
}