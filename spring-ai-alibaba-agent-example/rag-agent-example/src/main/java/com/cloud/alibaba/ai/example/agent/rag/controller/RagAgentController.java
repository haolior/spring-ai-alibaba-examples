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
package com.cloud.alibaba.ai.example.agent.rag.controller;

import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;
import java.util.UUID;

/**
 * RAG 智能体控制器
 * <p>
 * 提供与 RAG 智能体交互的 REST API，同时提供一个用于演示的简单 Web 页面。
 * </p>
 *
 * @author zth9
 * @since 2026-01-22
 */
@Controller
@RequestMapping("/api/rag")
public class RagAgentController {

	private static final Logger logger = LoggerFactory.getLogger(RagAgentController.class);

	private final ReactAgent ragAgent;

	public RagAgentController(ReactAgent ragAgent) {
		this.ragAgent = ragAgent;
	}

	@GetMapping("/")
	public String index() {
		return "index";
	}

	@PostMapping("/chat")
	@ResponseBody
	public ChatResponse chat(@RequestBody ChatRequest request) {
		logger.info("收到对话请求：{}", request.message());

		String threadId = request.threadId();
		if (threadId == null || threadId.isEmpty()) {
			threadId = UUID.randomUUID().toString();
		}

		try {
			RunnableConfig config = RunnableConfig.builder().threadId(threadId).build();

			// 这里用 call 也可以
			NodeOutput result = ragAgent.invokeAndGetOutput(request.message(), config).orElse(null);

			String response = extractResponse(result);

			logger.info("智能体回答：{}", response);
			return new ChatResponse(response, threadId, true);
		}
		catch (Exception e) {
			logger.error("处理对话请求失败", e);
			return new ChatResponse("抱歉，处理请求时发生错误：" + e.getMessage(), threadId, false);
		}
	}

	@GetMapping("/chat")
	@ResponseBody
	public ChatResponse chatGet(@RequestParam("message") String message,
			@RequestParam(value = "threadId", required = false) String threadId) {
		return chat(new ChatRequest(message, threadId));
	}

	private String extractResponse(NodeOutput result) {
		if (result == null) {
			return "未生成回答。";
		}

		OverAllState state = result.state();

		// 优先读取 ReactAgent 常用的 output 字段
		Optional<Object> output = state.value("output");
		if (output.isPresent()) {
			return String.valueOf(output.get());
		}

		// 回退读取 messages 字段
		Optional<List<AbstractMessage>> messages = state.value("messages");
		if (messages.isPresent() && !messages.get().isEmpty()) {
			List<AbstractMessage> msgList = messages.get();
			return msgList.get(msgList.size() - 1).getText();
		}

		// 最后回退为状态对象的字符串表示
		return state.toString();
	}

	public record ChatRequest(String message, String threadId) {
	}

	public record ChatResponse(String response, String threadId, boolean success) {
	}

}
