import {request} from "@/ui-backend/utils";

//2. 添加文章
export function addArticleAPI(formData: {
    title: string;
    content: string;
    channel: string;
    image_type: number;
    image_url: string;
}) {
    return request({
        url: "/api/backend/article",
        method: "POST",
        data: formData,
    });
}

//3. 获取文章列表
//7. 筛选文章
export function getArticleListAPI(formData: {
    channel_name: string;
}) {
    return request({
        url: `/api/backend/articlelist`,
        method: 'PUT',
        data: formData
    });
}

//4. 后台删除文章
export function delArticleAPI(id: string) {
    return request({
        url: `/api/backend/article/${id}`,
        method: "DELETE",
    });
}

//5. 获取id的文章
export function getArticleById(id: string) {
    return request({
        url: `/api/backend/article/${id}`,
        method: "GET",
    });
}

//6. 对id的文章删改（类似添加文章逻辑）
export function editArticleAPI(
    id: string,
    formData: {
        title: string;
        content: string;
        channel: string;
        image_type: number;
        image_url: string;
    }
) {
    return request({
        url: `/api/backend/article/${id}`,
        method: "PUT",
        data: formData,
    });
}

//7. 从id中在频道列表中获取相应的频道名字
export function getChannelById(
    id: string
) {
    return request({
        url: `/api/backend/channel/${id}`,
        method: "GET"
    })
}
