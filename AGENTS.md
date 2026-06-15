# 仓库指南

> Spring AI Alibaba 示例仓库的贡献者指南。
> 最后更新：2026-06-03

## 一、项目结构与模块组织

本项目是一个多模块 Maven 工程，每个顶层目录是一个独立的示例模块，均可单独构建和运行。以下列出所有模块及其实际用途（基于源代码和 README 分析）。

```
spring-ai-alibaba-examples/
├── pom.xml                 # 父 POM，仅编排 practice、rag、agent 三个活跃模块
├── tools/                  # 共享 Makefile、linter 配置、脚本
├── docker-compose/         # 示例所需 Docker 服务（ES、Milvus、pgvector、Redis 等）
│
├── 入门
│   └── spring-ai-alibaba-helloworld/   # 基础对话：同步/流式、ChatClient、ChatMemory、自定义 Advisor
│
├── 多平台对话
│   ├── spring-ai-alibaba-chat-example/
│   │   ├── dashscope-chat/       # DashScope 简单/流式对话，集成 Logger Advisor 与图片分析
│   │   ├── openai-chat/          # OpenAI 简单/流式/SSE 对话，集成 ChatMemory + Logger Advisor
│   │   ├── deepseek-chat/        # DeepSeek 简单/流式对话 + 自定义 Options
│   │   ├── minimax-chat/         # MiniMax 简单/流式对话，集成 ChatMemory + Logger Advisor
│   │   ├── ollama-chat/          # Ollama 简单/流式对话（默认 llama3），集成 Logger Advisor
│   │   ├── qwq-chat/             # 通义千问 QWQ 推理模型流式对话，展示思考链与 ChatMemory
│   │   ├── vllm-chat/            # VLLM（OpenAI 兼容 API）简单/流式对话
│   │   ├── zhipuai-chat/         # 智谱 AI 简单/流式对话，集成 ChatMemory + Logger Advisor
│   │   └── azure-openai-chat/    # Azure OpenAI ChatModel 和 ChatClient 两种方式的对话
│   ├── spring-ai-alibaba-more-platform-and-model-example/ # 3 平台（DashScope/Ollama/OpenAI）+ 4 模型灵活切换
│   └── spring-ai-alibaba-multi-model-example/
│       ├── ark-multi-model/           # Ark 平台多模态：图片+视频帧理解
│       ├── dashscope-multi-model/     # DashScope 多模态：图片+视频+音频理解
│       └── openai-dashscope-multi-model/  # OpenAI 接口调用 Qwen-VL 图片分析
│
├── 提示词与记忆
│   ├── spring-ai-alibaba-prompt-example/        # Prompt 模板（Stuff、Role 等）
│   ├── spring-ai-alibaba-nacos-prompt-example/  # Nacos 动态管理 Prompt
│   ├── spring-ai-alibaba-chat-memory-example/   # 对话记忆持久化：InMemory、MySQL、Redis、SQLite
│   └── spring-ai-alibaba-mem0-example/          # Mem0 外部记忆服务集成
│
├── 结构化输出与工具调用
│   ├── spring-ai-alibaba-structured-example/    # BeanOutputConverter、JSON 模式、Map/List 输出
│   ├── spring-ai-alibaba-tool-calling-example/  # 四种工具调用方式：方法/回调/函数名/FunctionCallback
│   └── spring-ai-alibaba-sandbox-example/
│       ├── sandbox-browser-fullstack/   # 浏览器操作 Agent：AI 控制浏览器执行任务
│       └── sandbox-simple-tool/         # 简单沙箱环境，集成计算器和天气两个工具
│
├── 多模态（图片/音频/视频）
│   ├── spring-ai-alibaba-image-example/
│   │   ├── dashscope-image/    # DashScope 图像生成（单/多图、多条件）
│   │   └── openai-image/       # 占位模块，暂未实现
│   ├── spring-ai-alibaba-audio-example/
│   │   └── dashscope-audio/    # 语音识别（ASR）和语音合成（TTS），支持 WebSocket
│   └── spring-ai-alibaba-video-example/
│       └── dashscope-video/    # 通义万相 12 种视频生成场景
│
├── RAG 与向量数据库
│   └── spring-ai-alibaba-rag-example/
│       ├── bailian-agent/                   # 百炼 AI 智能体（含知识库检索）
│       ├── bailian-rag-knowledge/           # 百炼云端知识库 RAG
│       ├── langgraph-custom-rag-example/    # LangGraph 自校正 RAG 工作流
│       ├── module-rag/                      # 模块化 RAG 架构（基于 ES）
│       ├── rag-component-example/           # spring-ai-alibaba-rag 组件库全面演示
│       ├── rag-elasticsearch-autoconfigure-example/  # ES 自动配置混合检索
│       ├── rag-elasticsearch-example/       # 完整 RAG 管线（本地 ES + 百炼云端双模式）
│       ├── rag-etl-pipeline-example/        # ETL 数据管线（多格式文档读取/转换/写入）
│       ├── rag-milvus-example/              # Milvus 向量库集成
│       ├── rag-openai-dashscope-pgvector-example/   # pgvector + OpenAI 兼容模式
│       ├── rag-parallel-example/            # 多路并行召回
│       ├── rag-pgvector-example/            # pgvector 基本 RAG 流程
│       ├── spring-ai-alibaba-vector-databases-example/  # 多种向量库 CRUD 操作
│       └── web-search/                      # 阿里云 IQS 联网搜索（占位模块）
│
├── AI 智能体
│   ├── spring-ai-alibaba-agent-example/
│   │   ├── a2a-client-example/               # A2A 协议客户端（Nacos 发现 + JSON-RPC）
│   │   ├── a2a-server-example/               # A2A 协议服务端（ReactAgent 注册到 Nacos）
│   │   ├── adk-samples-llm-auditor/          # LLM 答案审核：Critic→Reviser 双 Agent 流水线
│   │   ├── playground-flight-booking/        # AI 机票预订专家（RAG + Function Calling）
│   │   ├── rag-agent-example/                # Agentic RAG：ReactAgent 自主检索
│   │   ├── react-agent-example/              # 基础 ReactAgent + 文件工具 + 人工审批
│   │   ├── skills-agent-example/             # 动态技能加载 ReactAgent
│   │   ├── sql-agent-example/                # 自然语言转 SQL Agent
│   │   ├── voice-agent-dashscope-sdk-example/ # 实时语音 Agent（DashScope SDK）
│   │   └── voice-agent-example/              # 语音 Agent（paraformer→ReactAgent→cosyvoice）
│   └── python-a2a-agent-example/             # Python A2A 翻译 Agent（跨语言互操作）
│
├── MCP（模型上下文协议）
│   └── spring-ai-alibaba-mcp-example/
│       ├── spring-ai-alibaba-mcp-starter-example/ # 快速入门：注解驱动 + 多传输协议
│       ├── spring-ai-alibaba-mcp-manual-example/  # 第三方 MCP 集成（GitHub/文件系统/SQLite）
│       ├── spring-ai-alibaba-mcp-build-example/   # 从零手动构建 MCP 服务端
│       ├── spring-ai-alibaba-mcp-nacos-example/   # Nacos 注册发现 MCP 服务
│       ├── spring-ai-alibaba-mcp-auth-example/    # MCP 认证授权
│       └── spring-ai-alibaba-mcp-config-example/  # 多源 MCP 配置管理
│
├── 工作流与图编排
│   └── spring-ai-alibaba-graph-example/
│       ├── big-tool/                  # 向量库动态检索 Java Math 方法作为 Tool
│       ├── chatflow/                  # 智能待办事项多轮对话工作流
│       ├── graph-observability-langfuse/  # Graph + Langfuse 可观测性
│       ├── human-node/                # Human-in-the-Loop 节点
│       ├── interruptable-action-example/  # 可中断/恢复工作流（订单审批）
│       ├── issue-clarify-graph-example/   # 机票客服多轮对话工作流
│       ├── mcp-node/                  # MCP Tool 分配给 Graph 节点
│       ├── multiagent-openmanus/      # 多智能体协作（规划→监督→执行）
│       ├── parallel-node/             # 并行节点执行模式
│       ├── parallel-stream-node/      # 并行 + 流式输出
│       ├── product-analysis-graph/    # 产品信息增强分析
│       ├── react/                     # ReAct 循环模式
│       ├── reflection/                # 反思模式（生成→评审→迭代）
│       ├── stream-node/               # 节点流式处理
│       ├── usecase-field-classifier/  # 字段分类工作流（敏感词→LLM→人工）
│       ├── workflow-review-classifier/ # 评价反馈分类路由
│       └── workflow-writing-assistant/ # 智能写作助手
│
├── 评估与可观测性
│   ├── spring-ai-alibaba-evaluation-example/  # AI 内容评估（相关性/事实性/正确性等）
│   ├── spring-ai-alibaba-observability-example/
│   │   ├── observability-example/            # 基础可观测性（Micrometer 埋点）
│   │   ├── observability-arms-example/       # 阿里云 ARMS LLM 监控
│   │   ├── observability-langfuse-example/   # Langfuse + OpenTelemetry 链路追踪
│   │   └── observationhandler-example/       # 自定义 Micrometer ObservationHandler
│   └── spring-ai-alibaba-bailian-example/    # 百炼平台集成（知识库/智能体/文档解析）
│
├── NL2SQL
│   └── spring-ai-alibaba-nl2sql-example/
│       ├── chat/               # 自然语言数据库对话接口
│       ├── mcp/                # NL2SQL 能力以 MCP Tool 暴露
│       └── vector-management/  # 数据库 Schema 和业务解释管理
│
├── 综合应用
│   └── spring-ai-alibaba-usecase-example/
│       ├── spring-ai-alibaba-classification-grading-example/  # 数据字段分类分级
│       ├── spring-ai-alibaba-comprehensive-example/           # 全栈 AI 应用（多模态+RAG+爬虫+客服）
│       ├── spring-ai-alibaba-scene-example/                   # 多模型对比对话
│       ├── spring-ai-alibaba-sql-example/                     # NL2SQL 查询
│       ├── spring-ai-alibaba-text-classification-example/     # 5 种 Prompt 文本分类方法
│       ├── spring-ai-alibaba-text-summarizer-example/         # 文件上传 + AI 摘要
│       └── spring-ai-alibaba-translate-example/               # DashScope/Ollama 文件翻译
│
├── 其他
│   ├── spring-ai-alibaba-playground/   # 全功能 Web 演示平台（对话/工具/多模态/RAG/MCP）
│   └── practice/                       # [liwenhao] 个人学习沙箱，已完成 Chat/Basic→Chat/Memory→Structured→Tool→RAG 全链路动手练习
```

**构建说明**：根 `mvn package` 仅构建 `practice`、`rag-example`、`agent-example` 三个活跃模块。其余模块使用 `mvn -pl <路径> clean package -DskipTests` 独立构建。子模块使用 `mvn -pl <父模块>/<子模块> spring-boot:run` 运行。

## 二、构建、测试与开发命令

除非特别说明，所有命令在仓库根目录执行。

| 命令 | 用途 |
|------|------|
| `make build` | 构建所有活跃模块，跳过测试 |
| `make test` | 运行所有活跃模块的测试 |
| `make format-fix` | 通过 `spring-javaformat` 自动格式化 Java 代码 |
| `make format-check` | 校验代码格式 |
| `make checkstyle-check` | 运行 Checkstyle 检查 |
| `make lint` | 运行 YAML 校验、拼写检查、换行符检查 |
| `make licenses-check` | 验证 Apache License 头文件 |
| `make licenses-fix` | 补全缺失的 License 头文件 |
| `mvn -pl <模块> clean package -DskipTests` | 构建单个模块 |
| `mvn -pl <模块> spring-boot:run` | 运行指定示例模块 |
| `mvn -pl <父>/<子> spring-boot:run` | 运行子模块（如 `dashscope-chat`） |

**前置依赖**：Java 17+、Maven 3.6+。

## 三、编码风格与命名规范

- **语言**：Java 17，源文件编码 UTF-8（无 BOM）。
- **格式化**：通过 `spring-javaformat` Maven 插件强制执行（`make format-check`）。提交前执行 `make format-fix`。
- **License 头**：每个源文件必须包含 Apache License 2.0 头（具体模板见 `tools/linter/license/.licenserc.yaml`）。通过 `make licenses-check` 验证。
- **缩进**：Java 使用 4 个空格；YAML 文件遵循 `.yamllint` 规则（`tools/linter/yamllint/.yamllint`）。
- **命名**：类名 `PascalCase`、方法/变量 `camelCase`、常量 `UPPER_SNAKE_CASE`。模块名使用 kebab-case。
- **JSON 序列化**：优先使用 Jackson（`com.fasterxml.jackson`）。
- **工具类**：优先使用 Apache Commons Utils 或 Spring 提供的工具类。
- **示例模块必须完整**：包含 `README.md`、方法参数代码注释、可运行的入口点。

## 四、测试指南

- **框架**：Spring Boot Test（JUnit 5 + Spring Test 扩展）。
- **执行**：根目录 `mvn test`，或进入模块目录 `mvn test`。
- **约定**：测试代码位于 `src/test/java`，测试类命名 `*Test.java`。
- **覆盖**：依赖外部服务（向量数据库、Redis 等）的集成测试应使用 `@EnabledIf` 或 profile 条件跳过。
- **断言**：优先使用 AssertJ 链式断言。

## 五、提交与 Pull Request 指南

### 提交信息

遵循 [Conventional Commits](https://www.conventionalcommits.org/) 格式：

```
<类型>(<范围>): <描述>

feat(rag): add pgvector similarity search example
fix: correct API key env variable name in README
docs: improve quickstart onboarding
chore(mcp): add .env.example
```

常用类型：`feat`、`fix`、`docs`、`chore`、`refactor`、`test`、`style`、`ci`。范围通常为模块名。

### Pull Request

- 标题使用提交风格格式（如 `Feat: Add DashScope LLMs chat example`）。
- 描述清楚说明 PR 内容、影响的模块和测试方式。
- 提交前自审：代码格式、拼写、License Header、示例可运行。
- 关联 Issue 时在描述中链接。

## 六、安全与配置建议

- **API 密钥**：通过 `application.yml` 引用环境变量，切勿提交真实密钥。需要密钥的模块应提供 `.env.example`。
- **Gitignore**：`.env`、`target/`、IDE 文件、`node_modules/` 默认排除。
- **本地服务**：依赖 Docker 的示例应提供 `docker-compose/` 目录或内联 `docker-compose.yml`。
- **端口冲突**：同时运行多个示例前检查各模块 `application.yml` 中的 `server.port`。
- **`.env.example`**：在模块根目录放置，列出所有必需的环境变量及占位值。

