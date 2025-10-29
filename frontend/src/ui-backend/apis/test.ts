import axios from "axios";
import type { FieldValues } from "react-hook-form";

export const getTestAPI = () => {
  // 使用相对路径，让Vite代理处理请求
  return axios.get("/api/test");
};

export const userTestAPI = () => {
  return axios.get("/api/backend/users");
};

export const userTestPostAPI = (formData: FieldValues) => {
  return axios.post("/api/backend/users", formData);
};
