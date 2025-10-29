import axios from "axios"

// 获取COS密钥信息
export const getUploadKeyAPI = () => {
    return axios.get("/api/cos");
};
