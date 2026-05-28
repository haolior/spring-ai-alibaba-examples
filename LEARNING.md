# 🗺️ Spring AI Alibaba 零基础进阶学习计划

## 🎯 学习前置准备
- [ ] **环境准备**：确保本地开发环境为 JDK 17 或 JDK 21，Spring Boot 3.2.x+，Maven。
- [ ] **账号准备**：注册并登录 [阿里云百炼平台](https://bailian.console.aliyun.com/)，开通大模型服务并获取 `API Key`。
- [ ] **安全提醒**：既然已将 Key 写入本地配置文件，**切勿**将包含真实 Key 的配置文件提交推送到公共或共享仓库！
- [ ] **小贴士**：**不要一次性导入所有模块！** 每次只用 IDE 打开当前要学的子项目文件夹，避免依赖索引导致电脑卡死。

---

## 🧭 第一阶段：基础对话与核心 API（新手必看 🏎️）
> **目标**：掌握 Spring AI 的基本编程模型，别管原理，先让 Java 代码和 AI 说上话。

### 📅 Day 1：让 AI 说话（最简 Hello World）
- [ ] **通关项目**：[./spring-ai-alibaba-helloworld](./spring-ai-alibaba-helloworld)
- [ ] **大白话解释**：用最少的配置和代码，让 Java 程序能连上大模型并返回一句回答。适合用来测试网络、API Key 和依赖是否配置正确。
- [ ] **核心任务**：
  - [ ] 跑通第一个接口，在浏览器中看到 AI 的回复。
  - [ ] 观察 `application.yml` 里是如何指定通义千问或其它模型的。
  - [ ] **核心理解**：体会快捷重载方法 `prompt(query)` 的便利性。

### 📅 Day 2：全套聊天模式（流式输出与人设）
- [ ] **通关项目**：[./spring-ai-alibaba-chat-example](./spring-ai-alibaba-chat-example)
- [ ] **大白话解释**：演示了单次对话、流式返回（类似 ChatGPT 逐字蹦出）、以及如何通过配置改变 AI 的扮演角色（System Prompt）。
- [ ] **核心任务**：
  - [ ] 理解 `ChatClient` 的链式调用编程范式。
  - [ ] 对比普通同步对话（`.call()`）与打字机流式对话（`.stream()`）的代码区别。
  - [ ] 学习如何使用 `System Prompt`（系统提示词）给 AI 设定人设（例如：让 AI 扮演代码评审专家）。

---

## 🧩 第二阶段：融入后端业务（数据与提示词交互 🚀）
> **目标**：让 AI 像一个标准的后端接口一样，能够接收动态变量，并返回标准的 Java 对象，而不是乱七八糟的文本。

### 📅 Day 3：提示词（Prompt）管理
- [ ] **通关项目**：[./spring-ai-alibaba-prompt-example](./spring-ai-alibaba-prompt-example)
- [ ] **大白话解释**：演示如何把提示词写成模板（像 MyBatis 的 SQL 模板一样），然后动态传入参数组装给 AI，避免在 Java 中用 `+` 拼接字符串。
- [ ] **核心任务**：
  - [ ] 掌握 `PromptTemplate` 的使用，实现提示词文本与 Java 代码的解耦。
  - [ ] 理解为什么引入 `Prompt` 对象（为了工程化、模板化组合拳，解决复杂企业级开发痛点）。

### 📅 Day 4：结构化输出（最实用重头戏 ⭐️）
- [ ] **通关项目**：[./spring-ai-alibaba-structured-example](./spring-ai-alibaba-structured-example)
- [ ] **大白话解释**：演示如何让 AI 乖乖听话，把回答的内容直接转换成一个标准的 Java Bean（POJO）或 Map/List。比如输入“张三28岁”，AI 直接返回一个给装好数据的 User 对象。
- [ ] **核心任务**：
  - [ ] 深刻理解为什么此时**必须**调用无参 `.prompt()` 转入完全体 Builder 模式（因为快捷方法点不出高级的 `.output()`）。
  - [ ] 学习使用 `BeanOutputConverter`，强制大模型返回符合 JSON Schema 的数据，并利用终止站 `.entity()` 直接接收 Java 实体类。

---

## 🛠️ 第三阶段：能力扩展（工具调用与新协议 🛠️）
> **目标**：打破大模型“只能聊天”的限制，让 AI 能够连接你的后端代码和外部世界。

### 📅 Day 5：让 AI 调用你的代码（Function Calling）
- [ ] **通关项目**：[./spring-ai-alibaba-tool-calling-example](./spring-ai-alibaba-tool-calling-example)
- [ ] **大白话解释**：教你如何把一个普通的 Java 本地方法注册为“工具”，让 AI 自动根据用户的意图去调用它。比如你写个 `getWeather` 方法，用户问天气时，AI 会自动调用这个 Java 方法拿数据。
- [ ] **核心任务**：
  - [ ] 理解函数调用的底层原理（User ➡️ System ➡️ Tool Message ➡️ Assistant）。
  - [ ] 成功将本地一个查询方法注册为 AI 工具，并观察 AI 自发触发该方法的过程。

### 📅 Day 6：对接统一工具协议（MCP 趋势）
- [ ] **通关项目**：[./spring-ai-alibaba-mcp-example](./spring-ai-alibaba-mcp-example)
- [ ] **大白话解释**：演示如何利用最新的模型上下文协议（MCP），让你的 AI 能够像插 U 盘一样，直接连上外部现成的 MCP 服务器（比如读取文件系统、连接外部数据库），体验无需为每种工具写对接代码的便利性。

---

## 📚 第四阶段：知识库建设（RAG 检索增强生成 📚）
> **目标**：解决大模型不知道企业私有机密或即时新闻的问题——先去私有文档里查资料，再让 AI 结合资料回答。

### 📅 Day 7-8：打造私有知识库与向量存储
- [ ] **通关项目（准备）**：[./spring-ai-alibaba-vector-store-example](./spring-ai-alibaba-vector-store-example)
- [ ] **大白话解释**：专门演示如何对接各种主流的向量数据库（如阿里云的 DashVector、开源的 PGVector 等），学习数据是怎么被变成一串数字（Embedding）并存进数据库的。
- [ ] **通关项目（核心）**：[./spring-ai-alibaba-rag-example](./spring-ai-alibaba-rag-example)
- [ ] **大白话解释**：标准 RAG 流程。演示了怎么读取一个本地的 `.txt` 或 `.pdf` 文件，把它切碎、变成向量存起来，最后让 AI 看着这份文件回答问题。实现了最基础的“企业私有知识库/智能客服”。

---

## 🤖 第五阶段：高级业务场景与 Agent（智能体落地 🤖）
> **目标**：把上面的基础拼装在一起，做出具备“自主思考”和“处理复杂业务”的高级 AI 应用。

### 📅 Day 9：构建自主思考的智能体
- [ ] **通关项目**：[./spring-ai-alibaba-agent-example](./spring-ai-alibaba-agent-example)
- [ ] **大白话解释**：把“记忆（Memory）”和“工具（Tools）”塞给 AI，给它一个复杂的目标，让它自己思考、拆解步骤并多次调用工具完成。演示了一个具备自主决策能力的 AI 助理是如何构建的。

### 📅 Day 10+：终极综合压轴大戏
- [ ] **通关项目（数据智能体）**：[./spring-ai-alibaba-nl2sql-example](./spring-ai-alibaba-nl2sql-example)
- [ ] **大白话解释**：大白话转 SQL (DataAgent)。用户输入一句中文（如“查一下上个月上海区域的销售总额”），AI 会自动理解你的数据库表结构，生成正确的 SQL 并去查数据库，最后把结果展示给用户。非常适合用来做企业内部的智能报表系统。
- [ ] **通关项目（高阶业务实战）**：[./spring-ai-alibaba-flight-booking-example](./spring-ai-alibaba-flight-booking-example)
- [ ] **大白话解释**：机票预订系统。一个高度接近真实业务的综合 Sample。里面模拟了查机票、改签、退票等一系列复杂的真实业务流程。它把对话、工具调用、记忆管理和状态机全部融合在了一个订票业务里。

---

## 👑 第六阶段：毕业闭环（实战演练）
- [ ] **终极挑战**：脱离当前大仓库，在 Gitea 上自己从零新建一个独立的 Spring Boot 项目，徒手写一个**“智能个人记账/理财助理”**。
  - **核心流程**：用户输入大白话 ➡️ AI 结构化提取记账字段（第二阶段核心） ➡️ AI 自动调用你的 Java 方法存入数据库（第三阶段核心） ➡️ AI 结合你的历史账单 RAG 给出理财建议（第四阶段核心）。

---

## 💡 刷项目的核心避坑路线总结：
> ⚠️ **警告**：开始学习时，**千万不要先看 `flight-booking` 或者 `agent`**，里面复杂的业务逻辑和配置会让人抓狂。
>
> 🚀 **高效通关捷径**：
> `helloworld` 跑通配置 ➡️ `chat` 掌握对话 ➡️ `structured` 学会数据交互 ➡️ `tool-calling` 学会给 AI 喂工具。
>
> 这四个核心看懂了，你就可以直接开始在公司里做大部分基础的 AI 业务开发了！加油！