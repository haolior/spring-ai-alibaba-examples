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
package com.cloud.alibaba.ai.example.agent.rag.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.cloud.alibaba.ai.example.agent.rag.tool.KnowledgeRetrievalTool;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RAG 智能体配置
 * <p>
 * 该配置创建具备知识检索能力的 ReactAgent，实现 Agentic RAG 模式。
 * 智能体可以自主决定何时查询知识库，并综合检索结果生成回答。
 * </p>
 *
 * @author zth9
 * @since 2026-01-22
 */
@Configuration
public class RagAgentConfiguration {

	private final ChatModel chatModel;

	private final KnowledgeRetrievalTool knowledgeRetrievalTool;

	public RagAgentConfiguration(ChatModel chatModel, KnowledgeRetrievalTool knowledgeRetrievalTool) {
		this.chatModel = chatModel;
		this.knowledgeRetrievalTool = knowledgeRetrievalTool;
	}

	@Bean
	public ReactAgent ragAgent() throws GraphStateException {
		return ReactAgent.builder()
			.name("rag-agent")
			.description("一个 RAG（检索增强生成）智能体，可以通过检索知识库中的相关文档回答 Spring AI Alibaba 相关问题。"
					+ "智能体会使用语义搜索找到最相关的信息，并综合生成完整回答。")
			.model(chatModel)
			.saver(new MemorySaver())
			.tools(knowledgeRetrievalTool.toolCallback())
			.build();
	}

}
