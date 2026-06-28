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
package com.cloud.alibaba.ai.example.agent.sql.tool;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.stereotype.Component;

import java.util.function.BiFunction;

/**
 * 检查 SQL 查询常见问题的工具。
 * <p>
 * 该工具在执行前使用 LLM 检查 SQL 查询，帮助发现语法错误和常见问题。
 * </p>
 *
 * @author zth9
 * @since 2026-01-22
 */
@Component
public class QueryCheckerTool implements BiFunction<QueryCheckerTool.Request, ToolContext, String> {

	private static final Logger logger = LoggerFactory.getLogger(QueryCheckerTool.class);

	private final ChatModel chatModel;

	private static final String CHECK_PROMPT_TEMPLATE = """
			你是一个 SQL 查询检查器。请检查下面的 SQLite 查询是否存在常见问题：

			```sql
			%s
			```

			请重点检查：
			1. 语法错误
			2. 表名或字段名是否可能错误
			3. 字符串值是否缺少引号
			4. JOIN 条件是否正确
			5. GROUP BY 子句是否合理
			6. 是否存在潜在 SQL 注入风险

			如果查询看起来正确，请只回复：
			"正确：查询看起来是正确的。"

			如果发现问题，请回复：
			"发现问题："，然后用编号列表说明问题和修复建议。

			请保持回答简洁。
			""";

	public QueryCheckerTool(ChatModel chatModel) {
		this.chatModel = chatModel;
	}

	@Override
	public String apply(Request request, ToolContext toolContext) {
		logger.info("========== SQL 检查工具开始执行 ==========");
		logger.info("待检查 SQL：{}", request.query());

		try {
			String promptText = String.format(CHECK_PROMPT_TEMPLATE, request.query());
			Prompt prompt = new Prompt(promptText);

			String result = chatModel.call(prompt).getResult().getOutput().getText();

			logger.info("SQL 检查结果：{}", result);
			logger.info("========== SQL 检查工具执行结束 ==========");

			return result;
		}
		catch (Exception e) {
			logger.error("检查 SQL 查询失败", e);
			return "检查 SQL 查询失败：" + e.getMessage();
		}
	}

	public ToolCallback toolCallback() {
		return FunctionToolCallback.builder("check_query", this)
			.description("在执行前检查 SQL 查询中的常见问题。调用 execute_query 前应先使用该工具检查 SQL，"
					+ "它会识别语法错误、潜在问题并给出修复建议。")
			.inputType(Request.class)
			.build();
	}

	@JsonClassDescription("检查 SQL 查询错误的请求")
	public record Request(@JsonProperty(value = "query", required = true)
	@JsonPropertyDescription("需要检查常见问题的 SQL 查询语句") String query) {
	}

}
