import { request } from "@/ui-backend/utils";

// 1. 获取日志记录信息
export function chatWithUnity(){
    return request({
        url: "/api/backend/logging",
        method: "GET"
    })
}