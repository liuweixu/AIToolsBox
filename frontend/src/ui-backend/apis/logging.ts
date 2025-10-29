import { request } from "@/ui-backend/utils";

// 1. 获取日志记录信息
export function getLoggingAPI(){
    return request({
        url: "/api/backend/logging",
        method: "GET"
    })
}

// 2. 全部删除日志记录
export function deleteLoggingAPI(){
    return request({
        url: "/api/backend/logging",
        method: "DELETE"
    })
}