# Practice Agent 学习计划

本文档只记录 `practice` 模块里的 Agent 练习路线。思路是：先跑官方示例，再在 `practice` 中用 `AgentController1`、`AgentController2`、`AgentController3` 这类小控制器手写最小版，做到“看一个，练一个”。

## 当前阶段判断

`AgentController1` 已经完成 Agent 基础调用练习，目标是学会怎么调用 Agent：`call`、`invoke`，以及最小 Tool 如何挂到 Agent 上。

下一步可以把 `RunnableConfig.threadId` 和 `.saver(new MemorySaver())` 放到 Memory 入门里练：先不加 `MemorySaver` 跑一次，再加回 `MemorySaver` 用相同的 `threadId` 跑一次，观察 Agent 是否保留同一会话状态。

## practice Controller 对照表

| practice 练习 | 主要功能 | 练习接口 | 对应官网高级功能 | 对应示例模块 | 练习目标 |
| --- | --- | --- | --- | --- | --- |
| `AgentController1` | Agent 基础调用：`call`、`invoke`、最小 Tool | `GET http://localhost:8765/agent/call`、`GET http://localhost:8765/agent/invoke` | Agents 基础、Tools 基础 | `spring-ai-alibaba-agent-example/react-agent-example` | 会构建最小 `ReactAgent`，知道用户输入如何进入 Agent，结果如何返回，并理解 Tool 放进 Agent 后如何参与推理。 |
| `AgentController2` | `RunnableConfig.threadId`、去掉 / 加回 `.saver(new MemorySaver())` | `GET http://localhost:8765/agent/memory-basic/call` | Memory 入门 | `spring-ai-alibaba-agent-example/react-agent-example`、`rag-agent-example`、`sql-agent-example` | 把 config 调用从基础调用中拆出来，专门理解 `threadId` 和 `MemorySaver` 的关系，区分 Agent 状态保存和普通 Chat Memory。 |
| `AgentController3` | 结构化输出：Bean / JSON / Schema | `GET http://localhost:8765/agent/structured-output/structured` | Structured Output | `spring-ai-alibaba-structured-example`，Agent 模块可自写最小版 | 让 Agent 输出稳定结构，避免只拿一段自然语言文本。 |
| `AgentController4` | Tool Calling 进阶：多工具选择、工具上下文、工具返回值 | `GET http://localhost:8765/agent/tool-calling/chat` | Tools、Agent Tool | `spring-ai-alibaba-agent-example/react-agent-example`、`spring-ai-alibaba-tool-calling-example` | 在已经会挂一个 Tool 后，练多个 Tool 同时注册时模型如何选择，以及 ToolContext 如何传递业务上下文。 |
| `AgentController5` | `invokeAndGetOutput`、`NodeOutput`、`InterruptionMetadata` | `GET http://localhost:8765/agent/output-control/output` | 控制流程基础 | `spring-ai-alibaba-agent-example/react-agent-example` | 从“拿最终文本”升级到“拿完整执行输出”，为人工审批做准备。 |
| `AgentController6` | Human-in-the-Loop：工具调用前审批、反馈后恢复 | `GET http://localhost:8765/agent/human-in-loop/invoke`、`POST http://localhost:8765/agent/human-in-loop/feedback` | Human-in-the-Loop、Hooks | `spring-ai-alibaba-agent-example/react-agent-example` | 用一个有副作用工具触发审批，再用 `HUMAN_FEEDBACK_METADATA_KEY` 恢复执行。 |
| `AgentController7` | Tool Interceptor：日志、参数观察、异常兜底 | `GET http://localhost:8765/agent/interceptor/tool` | Interceptors | `spring-ai-alibaba-agent-example/react-agent-example` 的 `LogToolInterceptor` | 理解拦截器主要作用在工具调用链路，适合做审计和调试。 |
| `AgentController8` | Model / Agent Hook：执行前后改消息、改状态、清洗输出 | `GET http://localhost:8765/agent/hook/model` | Hooks | `spring-ai-alibaba-agent-example/adk-samples-llm-auditor` | 理解 Hook 更偏 Agent/Model 状态控制，比 Interceptor 更深入。 |
| `AgentController9` | 控制和流式输出：`stream`、SSE、增量结果 | `GET http://localhost:8765/agent/streaming/stream` | 控制和流式输出 | `spring-ai-alibaba-agent-example/a2a-client-example`、`voice-agent-example` | 先练文本流式输出，再看语音事件流。 |
| `AgentController10` | Agentic RAG：Agent 自主决定是否检索 | `GET http://localhost:8765/agent/rag/search` | RAG | `spring-ai-alibaba-agent-example/rag-agent-example` | 把检索工具交给 Agent，而不是固定每次都走 RAG。 |
| `AgentController11` | SQL Agent：Schema 工具、SQL 检查、查询执行 | `GET http://localhost:8765/agent/sql/query` | Agent Tool、结构化工具链 | `spring-ai-alibaba-agent-example/sql-agent-example` | 练真实业务工具链：列库表、查 Schema、检查 SQL、执行查询。 |
| `AgentController12` | Skills：动态技能加载、能力组织 | `GET http://localhost:8765/agent/skills/chat` | Skills | `spring-ai-alibaba-agent-example/skills-agent-example` | 在已经会写 Tool 和 Agent 后，再学如何把能力组织成 Skill。 |
| `AgentController13` | 多智能体 / 工作流 / A2A | `GET http://localhost:8765/agent/multi-agent/audit` | Multi-agent、Workflow、A2A Agent | `spring-ai-alibaba-agent-example/adk-samples-llm-auditor`、`playground-flight-booking`、`a2a-server-example`、`a2a-client-example` | 最后做综合练习，理解多个 Agent 如何协作或跨进程通信。 |

## 推荐最小路径

```text
AgentController1 基础调用
-> AgentController2：RunnableConfig + MemorySaver
-> Agent 结构化输出
-> Agent Memory + threadId
-> invokeAndGetOutput
-> Human-in-the-Loop
-> Interceptor / Hook
-> Agentic RAG
-> Skills
-> Multi-agent / A2A / 语音 Agent
```

## 每一步验收标准

每个阶段都按“官网文档 -> 示例模块 -> practice 手写最小版”的方式完成。不要只看能不能运行，要能回答三个问题：这个能力解决什么问题、它在 Agent 执行链路的哪个位置生效、如果不用它会出现什么限制。
