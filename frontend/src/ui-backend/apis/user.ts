//用户所有请求
import { request } from "@/ui-backend/utils";
import type { FieldValues } from "react-hook-form";

//登录请求
export function loginAPI(formData: FieldValues) {
  return request({
    url: "/api/backend/login",
    method: "POST",
    data: formData,
  });
}

//退出请求
export function logoutAPI() {
  return request({
    url: "/api/backend/logout",
    method: "GET",
  })
}

//判断是否成功登录
export function isLoginAPI() {
  return request({
    url: "/api/backend/islogin",
    method: "GET"
  })
}
