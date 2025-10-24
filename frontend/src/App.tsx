import React, { useEffect, useMemo, useRef, useState } from "react";
import {
  Avatar,
  Button,
  Card,
  Flex,
  Input,
  Layout,
  Space,
  Typography,
  Checkbox,
  CheckboxOptionType,
} from "antd";
import { UserOutlined, RobotOutlined, SendOutlined } from "@ant-design/icons";

import { useTheme } from "antd-style";

type ChatMessage = {
  id: string;
  role: "user" | "assistant";
  content: string;
  pending?: boolean; // streaming
};

function generateMemoryId(): number {
  // 简单生成一个 6 位数字 id
  return Math.floor(100000 + Math.random() * 900000);
}

export default function App(): JSX.Element {
  const [memoryId] = useState<number>(() => generateMemoryId());
  const [input, setInput] = useState<string>("");
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const sseRef = useRef<EventSource | null>(null);
  const listEndRef = useRef<HTMLDivElement | null>(null);

  const apiBase = useMemo(() => "/api", []);

  // @ts-ignore
  const [check, setCheck] = useState<string>([]);

  useEffect(() => {
    listEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages.length]);

  useEffect(() => {
    return () => {
      if (sseRef.current) {
        sseRef.current.close();
      }
    };
  }, []);

  //TODO 前端发送消息到后端
  const startSSE = (text: string, check: any) => {
    if (sseRef.current) {
      sseRef.current.close();
      sseRef.current = null;
    }
    // 先推入用户消息与一个占位的 assistant 空消息
    const userMsg: ChatMessage = {
      id: `${Date.now()}-user`,
      role: "user",
      content: text,
    };
    const aiMsgId = `${Date.now()}-ai`;
    const aiMsg: ChatMessage = {
      id: aiMsgId,
      role: "assistant",
      content: "",
      pending: true,
    };
    setMessages((prev) => [...prev, userMsg, aiMsg]);

    // const endpoints: Record<string, string> = {
    //   true_true: "/ai/chat",
    //   false_true: "/ai/chatWithMcp",
    //   true_false: "/ai/chatWithRag",
    //   false_false: "/ai/chatWithNone",
    // };
    // console.log(check);
    // const key = `${"rag" in check}_${"mcp" in check}`;
    // const url = new URL(`${apiBase}${endpoints[key]}`, window.location.origin);

    let url;
    if (check.includes("rag") && check.includes("mcp")) {
      url = new URL(`${apiBase}/ai/chat`, window.location.origin);
    } else if (check.includes("rag") && check.includes("mcp") === false) {
      url = new URL(`${apiBase}/ai/rag`, window.location.origin);
    } else if (check.includes("rag") === false && check.includes("mcp")) {
      url = new URL(`${apiBase}/ai/mcp`, window.location.origin);
    } else {
      url = new URL(`${apiBase}/ai/none`, window.location.origin);
    }

    url.searchParams.set("memoryId", String(memoryId));
    url.searchParams.set("message", text);

    // 搭建SSE连接
    console.log("url:", url.toString());
    const es = new EventSource(url.toString());
    sseRef.current = es;

    es.onmessage = (ev) => {
      const chunk = ev.data ?? "";
      setMessages((prev) =>
        prev.map((m) =>
          m.id === aiMsgId ? { ...m, content: m.content + chunk } : m
        )
      );
    };

    es.onerror = () => {
      es.close();
      sseRef.current = null;
      setMessages((prev) =>
        prev.map((m) => (m.id === aiMsgId ? { ...m, pending: false } : m))
      );
      setLoading(false);
    };

    es.onopen = () => {
      setLoading(false);
    };
  };

  const handleSend = async () => {
    const text = input.trim();
    if (!text) return;
    setInput("");
    setLoading(true);
    try {
      startSSE(text, check);
    } catch (e) {
      setLoading(false);
      // 发生错误时仅在控制台输出，避免对 Ant Design App 上下文的依赖
      // eslint-disable-next-line no-console
      console.error("发送失败", e);
    }
  };

  const handleKeyDown: React.KeyboardEventHandler<HTMLInputElement> = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  // 引入多选框 用于表示rag和智谱mcp的选择
  const options: CheckboxOptionType<string>[] = [
    { label: "rag", value: "rag", className: "rag" },
    { label: "搜狗mcp", value: "mcp", className: "mcp" },
  ];

  const onChecked = (checkedValues: any[]) => {
    // @ts-ignore
    setCheck(checkedValues);
  };

  console.log("消息", messages);
  return (
    <div style={{ height: "100vh", display: "flex", flexDirection: "column" }}>
      <Layout.Header style={{ color: "#fff", fontSize: 16, flexShrink: 0 }}>
        React 编程学习小助手（会话ID：{memoryId}）
      </Layout.Header>
      {/* 内容界面，主要用Card实现 */}
      <div
        style={{
          flex: 1,
          overflow: "hidden",
          padding: "16px",
          paddingBottom: "80px", // 为固定输入框留出空间
        }}
      >
        <Card style={{ height: "100%", overflow: "auto" }}>
          <Space direction="vertical" style={{ width: "100%" }} size={16}>
            {messages.map((msg) => (
              <Flex
                key={msg.id}
                justify={msg.role === "user" ? "flex-end" : "flex-start"}
              >
                {msg.role === "assistant" && (
                  <Avatar
                    style={{ background: "#1677ff" }}
                    icon={<RobotOutlined />}
                  />
                )}
                {/* 涉及到流式内容的输出 */}
                <div
                  className={msg.role === "user" ? "bubble user" : "bubble ai"}
                >
                  <Typography.Text style={{ whiteSpace: "pre-wrap" }}>
                    {msg.content || (msg.pending ? "..." : "")}
                  </Typography.Text>
                </div>
                {msg.role === "user" && (
                  <Avatar
                    style={{ background: "#87d068", marginLeft: 8 }}
                    icon={<UserOutlined />}
                  />
                )}
              </Flex>
            ))}
            <div ref={listEndRef} />
          </Space>
        </Card>
      </div>
      {/* 输入框界面 固定不动 故而用fixed */}
      <div
        style={{
          position: "fixed",
          bottom: 0,
          left: 0,
          right: 0,
          padding: "16px",
          backgroundColor: "#fff",
          borderTop: "1px solid #f0f0f0",
          zIndex: 1000,
        }}
      >
        <Flex gap={8}>
          <Input
            placeholder="请输入你的问题..."
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
            disabled={loading}
          />
          <Checkbox.Group options={options} onChange={onChecked} />
          <Button
            type="primary"
            icon={<SendOutlined />}
            onClick={handleSend}
            loading={loading}
          >
            发送
          </Button>
        </Flex>
      </div>
    </div>
  );
}
