# Agent 学习顺序建议

本文档整理 `spring-ai-alibaba-agent-example` 模块的推荐学习顺序，并结合 `practice`
模块中已经完成的 `AgentController1` 到 `AgentController3` 练习，形成“先跑真实示例，再抽取最小 Controller
练习”的路线。

## 当前阶段判断

目前已经跑通 `react-agent-example`，并在 `practice` 模块完成了三个基础练习：

| practice 练习 | 已掌握能力 | 对应示例/文档 |
| --- | --- | --- |
| `AgentController1` | `ReactAgent` 创建、`call` / `invoke`、最小 Tool 调用 | `react-agent-example`、Tools 基础 |
| `AgentController2` | `RunnableConfig.threadId` + `MemorySaver`，验证会话隔离 | `react-agent-example`、Memory |
| `AgentController3` | `outputType` / `outputSchema`，理解结构化输出和 Tool Calling 的配合 | Structured Output |

这个阶段已经具备“最小 Agent 开发能力”：能创建 Agent、挂工具、观察执行状态、做会话隔离，并让 Agent
输出稳定结构。下一步不建议为了编号继续堆 Controller，而是先跑更完整的官方示例，遇到值得拆解的能力再回
`practice` 写最小版。

## 学习策略

后续按下面节奏推进：

```text
跑官方示例
-> 看懂它解决什么业务问题
-> 找出新的 Agent 能力点
-> 回 practice 抽一个最小 Controller
-> 用 call / invoke / messages 验证执行链路
```

每一阶段都要能回答三个问题：

1. 这个能力解决什么问题？
2. 它在 Agent 执行链路的哪个位置生效？
3. 如果不用它，会出现什么限制？

## 推荐顺序

1. `react-agent-example`

   已跑通。建议回看一次 `invoke` 返回的 `messages`，重点把 ReAct 循环串起来：
   用户输入、模型生成 `toolCalls`、框架执行 Tool、Tool 结果回填、模型生成最终答案、状态写入 Saver。

   对应 practice：`AgentController1` 到 `AgentController3` 已覆盖基础调用、Memory 入门和结构化输出。

2. `rag-agent-example`

   下一步优先跑这个示例。重点理解 Agentic RAG：检索能力不是固定流程，而是作为工具交给 Agent，由模型判断是否需要检索。

   如果跑通后仍然说不清“什么时候检索、检索结果如何进入最终回答”，再建立
   `AgentController4`：Agentic RAG 最小练习。

3. `sql-agent-example`

   学习更真实的多工具链路：列库表、查 schema、生成 SQL、检查 SQL、执行查询、解释结果。

   这个示例适合作为 `AgentController5` 的来源，因为它比单个天气工具更接近业务 Agent。

4. 回看 `react-agent-example` 的 Human-in-the-Loop

   这里不要只看“人工审批”这个概念，要重点理解中断和恢复：什么时候中断、状态保存在哪里、人工反馈如何让 Agent 继续执行。

   对应 practice：`AgentController6`，只保留一个有副作用的工具，练审批前中断和反馈后恢复。

5. Interceptors 与 Hooks

   先学 Interceptor，再学 Hook。Interceptor 更适合工具调用日志、参数观察、异常兜底；Hook 更偏 Agent / Model
   执行前后的状态控制、消息改写和输出清洗。

   对应 practice：`AgentController7` 练 Interceptor，`AgentController8` 练 Hook。

6. 控制和流式输出

   在能看懂 `invoke`、`NodeOutput`、中断状态之后，再学习流式输出。重点不是“能不能流”，而是前端或调用方如何消费增量事件。

   对应 practice：`AgentController9`，先做文本流式，再考虑语音或 A2A 事件流。

7. `skills-agent-example`

   在已经会写 Tool 后再学 Skills。重点看它如何把能力组织成 Skill，而不是把所有工具都硬编码在一个 Agent 构建器里。

   对应 practice：`AgentController10`，练动态技能加载和能力组织。

8. `playground-flight-booking`

   作为阶段性综合练习。重点看业务流程如何把 RAG、Function Calling、多轮对话、状态和业务规则组合起来。

9. `adk-samples-llm-auditor`

   学习多 Agent 流水线，理解 Critic 到 Reviser 的协作模式，以及 Agent 如何进行答案审核和修正。

   对应 practice：可以作为 `AgentController11` 的多 Agent 最小练习来源。

10. `a2a-server-example`

    先学习 A2A 服务端，理解 Agent 如何注册、暴露能力，并作为可被其他应用调用的服务。

11. `a2a-client-example`

    再学习 A2A 客户端，理解如何发现远端 Agent、发起 JSON-RPC 调用，并完成跨进程 Agent 协作。

12. `voice-agent-example`

    学习语音 Agent 的基础链路，重点关注 ASR 到 Agent 再到 TTS 的完整流程。

13. `voice-agent-dashscope-sdk-example`

    最后学习实时语音 Agent，涉及实时交互和 DashScope SDK 集成，复杂度最高，建议放在文本 Agent
    和 A2A 理解充分之后。

## 当前最小路径

结合当前进度，最小路径建议调整为：

```text
已完成：
react-agent-example
-> practice AgentController1
-> practice AgentController2
-> practice AgentController3

接下来：
rag-agent-example
-> sql-agent-example
-> react-agent-example Human-in-the-Loop
-> Interceptors / Hooks
-> Streaming
-> skills-agent-example
-> playground-flight-booking
-> adk-samples-llm-auditor
-> a2a-server-example
-> a2a-client-example
```

## 什么时候新建 practice Controller

不是每跑一个示例都必须新建 Controller。只有满足下面任意条件时，才值得回 `practice` 抽最小练习：

- 示例里出现了新的 Agent 能力点，例如 Agentic RAG、SQL 多工具链、人工审批恢复、Hook、Interceptor。
- 跑通了示例，但还说不清执行链路。
- 示例太复杂，需要拆出一个最小版本单独验证。
- 这个能力未来可能在业务项目里复用。

如果只是换了一个业务场景，但底层能力还是 `ReactAgent + Tool + MemorySaver`，就先不用新增 Controller。
