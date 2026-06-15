# Spring AI Alibaba 学习笔记

## 什么是 Spring AI

Spring AI 是一个面向 AI 工程的应用框架，它提供了可移植的 API 和抽象层，
使得开发者可以方便地接入各种 AI 模型和服务，而无需修改业务代码。

## 核心概念

### ChatClient
ChatClient 是 Spring AI 中用于与 AI 模型进行对话的核心接口。
支持同步调用（call）和流式调用（stream）两种方式。

```java
ChatClient chatClient = chatClientBuilder.build();
String response = chatClient.prompt()
    .user("你好")
    .call()
    .content();
```

### Advisor
Advisor 是 Spring AI 的拦截器机制，可以在 AI 调用前后注入逻辑。
常见的 Advisor 包括：
- SimpleLoggerAdvisor：记录请求和响应日志
- MessageChatMemoryAdvisor：对话记忆管理
- QuestionAnswerAdvisor：RAG 问答增强
- RetrievalAugmentationAdvisor：检索增强生成

### RAG（检索增强生成）
RAG 是一种将信息检索与文本生成结合的 AI 技术架构。
核心流程：用户提问 → 向量检索 → 上下文增强 → 生成回答

## 向量数据库

Spring AI 支持多种向量数据库：
1. SimpleVectorStore：内存向量库，适合开发和测试
2. RedisVectorStore：基于 Redis 的向量存储
3. PGVectorStore：基于 PostgreSQL 的向量存储
4. MilvusVectorStore：基于 Milvus 的向量存储
5. ElasticsearchVectorStore：基于 ES 的向量存储

## ETL 管线

ETL 是 RAG 系统的数据导入管线：
1. Extract：从各种格式文件中提取文本
2. Transform：对文本进行分块、清洗、元数据丰富
3. Load：将处理后的数据写入向量数据库
