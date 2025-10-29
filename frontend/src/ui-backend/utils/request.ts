import axios from "axios";

// 创建 axios 实例
const request = axios.create({
  baseURL: "/", // 根据你的后端地址修改
  timeout: 5000,
  withCredentials: true
});

// 请求拦截器：在每个请求头加上 token
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem("token"); // 假设 token 存在 localStorage
    if (token) {
      // Sa-Token 默认读取 Authorization 或 satoken cookie，这里用 Header 方式
      config.headers["Authorization"] = `Bearer ${token}`;
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// 响应拦截器（可选）：统一处理登录失效
request.interceptors.response.use(
  response => response,
  error => {
    if (error.response && error.response.status === 401) {
      // token 无效或未登录，可跳转登录页
      console.log("未登录或 token 过期");
    }
    return Promise.reject(error);
  }
);

export { request };
