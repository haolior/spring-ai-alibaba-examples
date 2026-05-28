# 🗺️ Spring AI Alibaba 零基础进阶学习计划

## 🎯 学习前置准备
- [ ] **环境准备**：确保本地开发环境为 JDK 17 或 JDK 21，Spring Boot 3.2.x+，Maven。
- [ ] **账号准备**：注册并登录 [阿里云百炼平台](https://bailian.console.aliyun.com/)，开通大模型服务并获取 `API Key`。
- [ ] **注意**：**不要一次性导入所有模块！** 每次只用 IDE 打开当前要学的子项目文件夹，避免依赖索引导致电脑卡死。

---

## 🧭 第一阶段：破冰与跑通（预计：1-2 天）
> **目标**：别管原理，先让 Java 代码和 AI 说上话，掌握最基础的聊天 API。

### 📅 Day 1：让 AI 说话
- [ ] **通关项目**：[./spring-ai-alibaba-helloworld](./spring-ai-alibaba-helloworld)
- [ ] **核心任务**：
    - [ ] 配置本地环境变量 `AI_DASHSCOPE_API_KEY`。
    - [ ] 跑通第一个接口，在浏览器中看到 AI 的回复。
    - [ ] 观察 `application.yml` 里是如何指定通义千问或其它模型的。

### 📅 Day 2：流式输出与人设
- [ ] **通关项目**：[./spring-ai-alibaba-chat-example](./spring-ai-alibaba-chat-example)
- [ ] **核心任务**：
    - [ ] 理解 `ChatClient` 的链式调用编程范式。
    - [ ] 对比普通对话（`.call()`）与打字机流式对话（`.stream()`）的代码区别。
    - [ ] 学习如何使用 `System Prompt`（系统提示词）给 AI 设定人设（例如：让 AI 扮演代码评审专家）。

---

## 🧩 第二阶段：让 AI 融入后端业务（预计：2 天）
> **目标**：让 AI 像一个标准的后端接口一样，能够接收变量，并返回标准的 Java 对象，而不是乱七八糟的文本。

### 📅 Day 3：优雅管理提示词
- [ ] **通关项目**：[./spring-ai-alibaba-prompt-example](./spring-ai-alibaba-prompt-example)
- [ ] **核心任务**：
    - [ ] 学习如何像写 MyBatis 的动态 SQL 一样，把提示词写成模板（`PromptTemplate`）。
    - [ ] 掌握如何把 Java 中的业务变量（如用户名、商品信息）动态替换到提示词模板中。

### 📅 Day 4：结构化输出（重头戏 ⭐️）
- [ ] **通关项目**：[./spring-ai-alibaba-structured-example](./spring-ai-alibaba-structured-example)
- [ ] **核心任务**：
    - [ ] 学习使用 `BeanOutputConverter`。
    - [ ] 强制要求 AI 将回答内容直接转换成标准的 Java Bean（POJO）、List 或 Map。
    - [ ] **效果达成**：输入一串杂乱的文本，Java 直接接收到一个装满数据的 `User` 对象。

---

## 🛠️ 第三阶段：能力扩展（给 AI 喂工具和数据）（预计：2-3 天）
> **目标**：打破 AI “只能聊天”的局限，让 AI 能够调用你写的 Java 代码，或者查阅你提供的私有文档。

### 📅 Day 5：让 AI 调用你的代码（Tool）
- [ ] **通关项目**：[./spring-ai-alibaba-tool-calling-example](./spring-ai-alibaba-tool-calling-example)
- [ ] **核心任务**：
    - [ ] 理解 **Function Calling（函数调用）** 的基本原理。
    - [ ] 学习如何把一个普通的 Java 本地方法（如 `queryInventory()`）注册为 AI 的工具。
    - [ ] 测试当问 AI 库存时，AI 是否会自动触发并执行你写的 Java 方法。

### 📅 Day 6-7：打造私有知识库（RAG ⭐️）
- [ ] **通关项目**：可先看 [./spring-ai-alibaba-vector-store-example](./spring-ai-alibaba-vector-store-example) 了解向量库，随后攻克 [./spring-ai-alibaba-rag-example](./spring-ai-alibaba-rag-example)
- [ ] **核心任务**：
    - [ ] 了解什么是文本向量化（Embedding）。
    - [ ] 学习如何读取一个本地的 `.txt` 或 `.pdf` 企业文档并切碎存入向量数据库。
    - [ ] **效果达成**：大模型在不泄露隐私的前提下，结合这份私有文档进行精准回答（智能客服原型）。

---

## 🤖 第四阶段：迈向智能体与高阶实战（预计：3 天以上）
> **目标**：接触行业最前沿的概念，学习如何构建能够自主思考、拆解任务的复杂 AI系统。

### 📅 Day 8：解锁统一工具协议
- [ ] **通关项目**：[./spring-ai-alibaba-mcp-example](./spring-ai-alibaba-mcp-example)
- [ ] **核心任务**：
    - [ ] 了解最新的 **MCP（模型上下文协议）** 概念。
    - [ ] 体验如何像插 U 盘一样，让 AI 无缝连接外部现成的 MCP 服务器，直接读取文件系统或 GitHub 数据。

### 📅 Day 9：构建自主思考的智能体
- [ ] **通关项目**：[./spring-ai-alibaba-agent-example](./spring-ai-alibaba-agent-example)
- [ ] **核心任务**：
    - [ ] 理解 **Agent（智能体）** 的构成：大模型 + 记忆（Memory） + 工具（Tools）。
    - [ ] 观察给 AI 一个复杂长任务时，它是如何自己规划步骤、连续多次调用工具并最终完成任务的。

### 📅 Day 10+：终极综合大戏
- [ ] **通关项目**：[./spring-ai-alibaba-nl2sql-example](./spring-ai-alibaba-nl2sql-example)（大白话转 SQL 报表）
- [ ] **通关项目**：[./spring-ai-alibaba-flight-booking-example](./spring-ai-alibaba-flight-booking-example)（企业级机票预订综合实战）
- [ ] **核心任务**：
    - [ ] 仔细研读其架构设计，看看状态机、复杂业务流和多工具是如何融合在真实业务场景中的，作为公司项目的落地参考。

---

## 👑 第五阶段：毕业闭环（实战演练）
- [ ] **终极挑战**：脱离当前大仓库，在 Gitea 上自己从零新建一个独立的 Spring Boot 项目，徒手写一个**“智能个人记账/理财助理”**。
    - **核心流程**：用户输入大白话 ➡️ AI 结构化提取记账字段 ➡️ AI 自动调用你的 Java 方法存入数据库 ➡️ AI 结合你的历史账单（RAG）给出理财建议。

---

> 💡 **通关小贴士**：前三个阶段（Day 1~4）是性价比最高的，只要掌握了，你就能应付日常开发中 70% 的 AI 对话与数据交互需求了！祝你本地演练场搭建顺利，开启你的 Java AI 之路！