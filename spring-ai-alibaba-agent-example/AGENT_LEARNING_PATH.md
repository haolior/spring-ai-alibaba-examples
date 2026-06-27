# Agent 学习顺序建议

本文档整理 `spring-ai-alibaba-agent-example` 模块的推荐学习顺序，适合从基础 Agent 能力逐步过渡到业务场景、多 Agent 协作、A2A 通信和语音 Agent。

## 推荐顺序

1. `react-agent-example`

   先学习基础 ReAct Agent，重点理解推理循环、工具调用、文件工具和人工审批。这是后续 Agent 示例的核心基础。

2. `skills-agent-example`

   学习动态技能加载，理解 Agent 如何通过 Skill 扩展能力，而不是把所有工具硬编码在主流程中。

3. `rag-agent-example`

   学习 Agentic RAG，重点看 Agent 如何自主判断是否需要检索、如何使用知识，以及与普通固定流程 RAG 的区别。

4. `sql-agent-example`

   学习自然语言转 SQL，重点理解 Agent 如何结合数据库 schema、工具调用和查询结果解释。

5. `playground-flight-booking`

   学习完整业务场景，将 RAG、Function Calling、多轮对话和业务流程串联起来，适合作为阶段性综合练习。

6. `adk-samples-llm-auditor`

   学习多 Agent 流水线，理解 Critic 到 Reviser 的协作模式，以及 Agent 如何进行答案审核和修正。

7. `a2a-server-example`

   先学习 A2A 服务端，理解 Agent 如何注册、暴露能力，并作为可被其他应用调用的服务。

8. `a2a-client-example`

   再学习 A2A 客户端，理解如何发现远端 Agent、发起 JSON-RPC 调用，并完成跨进程 Agent 协作。

9. `voice-agent-example`

   学习语音 Agent 的基础链路，重点关注 ASR 到 Agent 再到 TTS 的完整流程。

10. `voice-agent-dashscope-sdk-example`

    最后学习实时语音 Agent，涉及实时交互和 DashScope SDK 集成，复杂度最高，建议放在文本 Agent 理解充分之后。

## 最小学习路径

如果刚开始学习，可以先走下面这条最小路径：

```text
react-agent-example
-> skills-agent-example
-> rag-agent-example
-> playground-flight-booking
-> a2a-server-example
-> a2a-client-example
```

这条路径先建立 Agent 基础，再学习能力扩展和知识增强，最后理解跨 Agent 通信，学习曲线相对平滑。
