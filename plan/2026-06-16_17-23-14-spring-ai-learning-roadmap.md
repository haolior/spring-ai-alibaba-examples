---
mode: plan
cwd: C:\Users\Administrator\Desktop\spring-ai-alibaba-examples
task: Spring AI Alibaba 学习路线规划
complexity: medium
planning_method: builtin
created_at: 2026-06-16T17:23:14+08:00
---

# Plan: Spring AI Alibaba 学习路线规划

🎯 任务概述

基于当前 `practice` 模块和 `liwenhao` 提交轨迹，整理下一阶段的 Spring AI Alibaba 学习路线。学习方式固定为：先结合 `README.md` 和代码确认模块用途，再运行源码观察实际能力，最后在 `practice` 中手写一个最小可运行版本，确保做到“跑一个，练一个”。

📋 执行计划

1. 从 Agent 主线开始，先补齐模型自主决策、工具调用和检索协作能力。
2. 进入 Graph / Workflow 主线，理解多节点状态流转、反思、并行和人工介入流程。
3. 深化 MCP，重点从“已经跑通过”升级到“理解设计并能自己实现最小闭环”。
4. 补齐 Observability / Evaluation，建立 AI 应用生产化视角。
5. 最后扩展多模态能力，补齐对 Spring AI Alibaba 能力边界的整体认识。

## 学习总原则

每个阶段都按同一套路推进：

1. 先看模块 `README.md`
2. 再看启动类、核心配置、主流程代码，确认这个模块实际上在演示什么
3. 跑通模块，观察核心能力边界
4. 在 `practice` 中手写一个最小版
5. 跑完一个，练一个，再进入下一个

判断模块用途时，不能只看目录名或 README，必须同时看：

- `README.md`
- 启动类
- `application.yml` / `application.yaml`
- Controller / Service / Tool / Agent / Graph 主流程代码

## Phase 1：Agent 主线

### 模块与用途

- `spring-ai-alibaba-agent-example/react-agent-example`
  作用：最基础的 ReAct Agent，重点看模型如何决定是否调工具。
- `spring-ai-alibaba-agent-example/rag-agent-example`
  作用：把检索能力交给 Agent，由 Agent 自主决定何时查询知识库。
- `spring-ai-alibaba-agent-example/skills-agent-example`
  作用：观察技能装载和能力组织方式。
- `spring-ai-alibaba-agent-example/playground-flight-booking`
  作用：看更接近真实业务场景的 Agent 组合案例。

### 先看哪些内容

- 每个模块的 `README.md`
- 启动类
- Agent 配置类
- Tool 定义类
- `application.yml`

### 跑源码时重点观察

- Agent 的输入输出形式
- 模型什么时候决定调用工具
- 工具调用结束后如何继续推理
- 普通 `ChatClient + tools` 和 Agent 的差异

### 在 practice 里要练什么

- 一个最小 ReAct Agent
- 一个带检索能力的 Agent

### 验收标准

- 能说清楚 Agent 解决什么问题
- 能跑通至少一个基础 Agent 和一个 RAG Agent
- 能在 `practice` 中手写一个最小 Agent 闭环

## Phase 2：Graph / Workflow 主线

### 模块与用途

- `spring-ai-alibaba-graph-example/react`
  作用：看 ReAct 思路如何落进 Graph。
- `spring-ai-alibaba-graph-example/reflection`
  作用：看“生成 -> 评审 -> 修正”的反思模式。
- `spring-ai-alibaba-graph-example/parallel-node`
  作用：看并行节点执行。
- `spring-ai-alibaba-graph-example/parallel-stream-node`
  作用：看并行和流式输出组合。
- `spring-ai-alibaba-graph-example/human-node`
  作用：看人工介入节点。
- `spring-ai-alibaba-graph-example/interruptable-action-example`
  作用：看工作流的中断与恢复。

### 先看哪些内容

- 每个模块的 `README.md`
- Graph 定义类
- State / Node 相关类
- 调用入口
- `application.yml`

### 跑源码时重点观察

- 节点怎么定义
- 状态怎么在节点之间流转
- 串行、并行、反思分别解决什么问题
- 为什么复杂 AI 流程更适合 Graph，而不是单个 Controller

### 在 practice 里要练什么

- 一个 2 到 3 节点的最小工作流
- 一个最小 reflection 或 human-in-the-loop 例子

### 验收标准

- 能区分 Agent 和 Workflow 的边界
- 能跑通一个基础 Graph 场景
- 能在 `practice` 中手写一个最小工作流

## Phase 3：MCP 深化

### 模块与用途

- `spring-ai-alibaba-mcp-example/spring-ai-alibaba-mcp-starter-example`
  作用：最快理解 Spring AI Alibaba 怎样接 MCP。
- `spring-ai-alibaba-mcp-example/spring-ai-alibaba-mcp-manual-example`
  作用：看手工接入第三方 MCP 服务。
- `spring-ai-alibaba-mcp-example/spring-ai-alibaba-mcp-build-example`
  作用：看 MCP 服务端从零构建。
- `spring-ai-alibaba-mcp-example/spring-ai-alibaba-mcp-config-example`
  作用：看多源 MCP 配置管理。
- `spring-ai-alibaba-mcp-example/spring-ai-alibaba-mcp-auth-example`
  作用：看 MCP 认证授权。
- `spring-ai-alibaba-mcp-example/spring-ai-alibaba-mcp-nacos-example`
  作用：看 MCP 注册发现。

### 先看哪些内容

- 每个模块的 `README.md`
- MCP Server / Client 配置类
- Tool / Handler 暴露位置
- 协议传输相关配置
- 启动与调用入口

### 跑源码时重点观察

- MCP server 暴露能力的最小单位
- `stdio` 和 `SSE` 的差异
- MCP tool 和 Spring AI Tool Calling 的关系
- 自动装配方式和手工构建方式的差异

### 在 practice 里要练什么

- 一个最小 MCP Server
- 一个最小 MCP Client 接入
- 一个你自己定义的 MCP Tool

### 验收标准

- 能解释 MCP 的定位和适用场景
- 能跑通一个 starter 例子和一个手工构建例子
- 能在 `practice` 中写出自己的最小 MCP 闭环

## Phase 4：Observability / Evaluation

### 模块与用途

- `spring-ai-alibaba-observability-example/observability-example`
  作用：基础观测埋点。
- `spring-ai-alibaba-observability-example/observability-langfuse-example`
  作用：看链路追踪和可观测平台接入。
- `spring-ai-alibaba-observability-example/observationhandler-example`
  作用：看自定义观测处理逻辑。
- `spring-ai-alibaba-evaluation-example`
  作用：看 AI 输出效果评估。

### 先看哪些内容

- 每个模块的 `README.md`
- 观测配置类
- 评估执行入口
- 指标 / Handler / 回调定义
- `application.yml`

### 跑源码时重点观察

- 调用链怎么追踪
- Prompt、RAG、Agent 的效果怎么评估
- 为什么 AI 应用不能只看“能跑”

### 在 practice 里要练什么

- 给一个 chat 或 rag 接口加基础观测
- 给一个 RAG 场景做最小评估

### 验收标准

- 能说清楚观测和评估分别解决什么问题
- 能跑通一个基础观测示例和一个评估示例
- 能把最小观测能力接回 `practice`

## Phase 5：多模态扩展

### 模块与用途

- `spring-ai-alibaba-multi-model-example/ark-multi-model`
  作用：图片、视频帧理解。
- `spring-ai-alibaba-multi-model-example/dashscope-multi-model`
  作用：图片、视频、音频理解。
- `spring-ai-alibaba-image-example/dashscope-image`
  作用：图像生成。
- `spring-ai-alibaba-audio-example/dashscope-audio`
  作用：ASR / TTS。
- `spring-ai-alibaba-video-example/dashscope-video`
  作用：视频生成。

### 先看哪些内容

- 每个模块的 `README.md`
- 启动类
- 多模态调用入口
- 模型参数配置
- `application.yml`

### 跑源码时重点观察

- 输入输出介质的变化
- 模型参数和能力边界
- 多模态和普通 Chat 场景的差异

### 在 practice 里要练什么

- 从图片、音频、视频里选一个方向做最小 demo

### 验收标准

- 能说清楚选中模块的核心能力
- 能跑通一个多模态示例
- 能在 `practice` 中做出最小复现

⚠️ 风险与注意事项

- `practice/src/main/resources/application.yml` 当前包含明文数据库与 Redis 配置，后续如果推送远端，建议改为环境变量。
- 学习过程中不要只看 README 或目录名，必须结合代码入口确认模块真实用途。
- 每个阶段都以“跑通 + 手写最小版”为完成标准，避免只停留在阅读层。

📎 参考

- `practice/src/main/java/cn/haohaoli/cloud/ai/practice/chat/Chat1Controller.java:18`
- `practice/src/main/java/cn/haohaoli/cloud/ai/practice/chat/Chat3Controller.java:55`
- `practice/src/main/java/cn/haohaoli/cloud/ai/practice/chat/Chat4Controller.java:60`
- `practice/src/main/java/cn/haohaoli/cloud/ai/practice/chat/Chat5Controller.java:35`
- `practice/src/main/java/cn/haohaoli/cloud/ai/practice/tool/Tool1Controller.java:49`
- `practice/src/main/java/cn/haohaoli/cloud/ai/practice/rag/RagVector1Controller.java:42`
- `practice/src/main/java/cn/haohaoli/cloud/ai/practice/rag/RagSimpleController.java:138`
- `practice/src/main/java/cn/haohaoli/cloud/ai/practice/rag/RagAdvisor6Controller.java:50`
