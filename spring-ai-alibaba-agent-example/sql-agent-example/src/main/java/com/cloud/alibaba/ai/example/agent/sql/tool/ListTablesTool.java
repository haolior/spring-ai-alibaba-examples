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
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.BiFunction;

/**
 * 列出数据库中所有表的工具。
 * <p>
 * 该工具返回所有可用表名，帮助 Agent 先了解数据库结构。
 * </p>
 *
 * @author zth9
 * @since 2026-01-22
 */
@Component
public class ListTablesTool implements BiFunction<ListTablesTool.Request, ToolContext, String> {

	private static final Logger logger = LoggerFactory.getLogger(ListTablesTool.class);

	private final JdbcTemplate jdbcTemplate;

	public ListTablesTool(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public String apply(Request request, ToolContext toolContext) {
		logger.info("========== 列出数据表工具开始执行 ==========");

		try {
			List<String> tables = jdbcTemplate.queryForList(
					"SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' ORDER BY name",
					String.class);

			if (tables.isEmpty()) {
				logger.info("数据库中没有找到表");
				return "数据库中没有找到任何表。";
			}

			String result = String.join(", ", tables);
			logger.info("找到 {} 张表：{}", tables.size(), result);
			logger.info("========== 列出数据表工具执行结束 ==========");

			return result;
		}
		catch (Exception e) {
			logger.error("列出数据表失败", e);
			return "列出数据表失败：" + e.getMessage();
		}
	}

	public ToolCallback toolCallback() {
		return FunctionToolCallback.builder("list_tables", this)
			.description("列出数据库中所有可用表。生成 SQL 前应先调用该工具，了解当前数据库有哪些表。"
					+ "返回值是用逗号分隔的表名列表。")
			.inputType(Request.class)
			.build();
	}

	@JsonClassDescription("列出所有数据库表的请求")
	public record Request(
			@JsonProperty(value = "dummy", required = false)
			@JsonPropertyDescription("占位参数，不实际使用，传空字符串即可。") String dummy) {
	}

}
