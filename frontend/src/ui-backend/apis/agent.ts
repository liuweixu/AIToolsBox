import { request } from "@/ui-backend/utils";

// 1. 在AI智能体创建对话框
export function createChatAgent(message: string){
    return request({
        url: "/api/agent/manus/create",
        method: "POST",
        data:  { message: message }
    })
}

// 2. 在AI智能体删除对话框
export function deleteChatAgent(id: string) {
    return request({
        url: "/api/agent/manus/delete",
        method: "POST",
        data: {id: id}
    })
}

// 3. 获取AI智能体列表
export function getChatAgentList() {
    return request({
        url: "/api/agent/manus/list",
        method: "GET"
    })
}

// 4. 获取AI智能体对话历史
export function getChatAgentHistory(id: string, pageSize: number = 30, lastCreateTime?: string) {
    const params: Record<string, string | number> = {
        PageSize: pageSize
    }
    if (lastCreateTime) {
        params.lastCreateTime = lastCreateTime
    }
    return request({
        url: `/api/agent/history/${id}`,
        method: "GET",
        params
    })
}