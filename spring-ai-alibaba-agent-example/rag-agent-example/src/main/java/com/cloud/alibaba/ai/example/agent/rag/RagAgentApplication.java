/*
 * Copyright 2026-2027 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloud.alibaba.ai.example.agent.rag;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * RAG 智能体示例应用
 * <p>
 * 该应用基于 Spring AI Alibaba 演示 Agentic RAG（智能体增强检索生成）模式。
 * 智能体可以自主从知识库检索相关文档，并为用户问题生成综合回答。
 * </p>
 *
 * @author zth9
 * @since 2026-01-22
 */
@SpringBootApplication
public class RagAgentApplication {

	public static void main(String[] args) {
		SpringApplication.run(RagAgentApplication.class, args);
	}

}
