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
package com.cloud.alibaba.ai.example.agent.rag.tool;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.jsoup.JsoupDocumentReader;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * RAG 智能体的知识检索工具
 * <p>
 * 该工具通过向量相似度搜索从知识库中检索相关文档。它遵循 Agentic RAG 模式，
 * 由智能体自主判断何时以及如何使用检索工具。
 * </p>
 *
 * @author zth9
 * @since 2026-01-22
 */
@Component
public class KnowledgeRetrievalTool implements BiFunction<KnowledgeRetrievalTool.Request, ToolContext, String> {

	private static final Logger logger = LoggerFactory.getLogger(KnowledgeRetrievalTool.class);

	private static final int DEFAULT_TOP_K = 4;

	private final SimpleVectorStore vectorStore;

	@Value("#{'${rag.knowledge.sources}'.split(',')}")
	private List<String> knowledgeSourceUrls;

	public KnowledgeRetrievalTool(EmbeddingModel embeddingModel) {
		this.vectorStore = SimpleVectorStore.builder(embeddingModel).build();
	}

	@PostConstruct
	void initKnowledgeBase() {
		logger.info("开始从 {} 个知识源初始化知识库...", knowledgeSourceUrls.size());

		for (String sourceUrl : knowledgeSourceUrls) {
			String url = sourceUrl.trim();
			try {
				JsoupDocumentReader reader = new JsoupDocumentReader(url);
				List<Document> documents = reader.get();
				logger.info("已从 {} 加载 {} 个文档", url, documents.size());

				TokenTextSplitter splitter = new TokenTextSplitter();
				List<Document> splitDocuments = splitter.apply(documents);
				logger.info("已切分为 {} 个片段", splitDocuments.size());

				vectorStore.add(splitDocuments);
				logger.info("已向向量库写入 {} 个片段", splitDocuments.size());
			}
			catch (Exception e) {
				logger.warn("从 {} 加载文档失败：{}", url, e.getMessage());
			}
		}

		logger.info("知识库初始化完成");
	}

	@Override
	public String apply(Request request, ToolContext toolContext) {
		logger.info("========== 知识检索工具开始执行 ==========");
		logger.info("查询内容：{}", request.query());

		int topK = request.topK() != null ? request.topK() : DEFAULT_TOP_K;

		SearchRequest searchRequest = SearchRequest.builder().query(request.query()).topK(topK).build();

		List<Document> documents = vectorStore.similaritySearch(searchRequest);

		if (documents.isEmpty()) {
			logger.info("未找到与查询相关的文档：{}", request.query());
			return "知识库中没有找到与该问题相关的信息。";
		}

		String result = documents.stream()
			.map(doc -> "---\n" + doc.getFormattedContent() + "\n---")
			.collect(Collectors.joining("\n\n"));

		logger.info("已检索到 {} 个相关文档", documents.size());
		logger.info("========== 知识检索工具执行结束 ==========");

		return result;
	}

	public ToolCallback toolCallback() {
		return FunctionToolCallback.builder("knowledge_retrieval", this)
			.description("从 Spring AI Alibaba 知识库检索相关信息。当需要回答 Spring AI Alibaba 的功能、配置或使用方式问题时使用该工具。"
					+ "工具会基于查询内容进行语义搜索，找到最相关的文档。")
			.inputType(Request.class)
			.build();
	}

	@JsonClassDescription("从文档中检索知识的请求")
	public record Request(@JsonProperty(value = "query", required = true)
	@JsonPropertyDescription("用于查找相关文档的搜索问题。请尽量具体，并包含与问题相关的关键词。") String query,

			@JsonProperty(value = "top_k")
			@JsonPropertyDescription("需要返回的最相关结果数量，默认值为 4") Integer topK) {
	}

}
