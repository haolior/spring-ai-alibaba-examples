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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * 获取指定表结构的工具。
 * <p>
 * 该工具返回指定表的 CREATE TABLE 语句和示例数据，帮助 Agent 在编写 SQL 前理解表结构。
 * </p>
 *
 * @author zth9
 * @since 2026-01-22
 */
@Component
public class GetSchemaTool implements BiFunction<GetSchemaTool.Request, ToolContext, String> {

	private static final Logger logger = LoggerFactory.getLogger(GetSchemaTool.class);

	private static final int SAMPLE_ROWS = 3;

	private final JdbcTemplate jdbcTemplate;

	public GetSchemaTool(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public String apply(Request request, ToolContext toolContext) {
		logger.info("========== 获取表结构工具开始执行 ==========");
		logger.info("请求查看的表：{}", request.tables());

		try {
			List<String> tableNames = Arrays.stream(request.tables().split(","))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.toList();

			if (tableNames.isEmpty()) {
				return "没有提供表名。请使用逗号分隔多个表名。";
			}

			StringBuilder result = new StringBuilder();

			for (String tableName : tableNames) {
				result.append(getTableSchema(tableName));
				result.append("\n\n");
			}

			logger.info("========== 获取表结构工具执行结束 ==========");
			return result.toString().trim();
		}
		catch (Exception e) {
			logger.error("获取表结构失败", e);
			return "获取表结构失败：" + e.getMessage();
		}
	}

	private String getTableSchema(String tableName) {
		StringBuilder sb = new StringBuilder();

		try {
			// 获取 CREATE TABLE 建表语句。
			String createSql = jdbcTemplate.queryForObject(
					"SELECT sql FROM sqlite_master WHERE type='table' AND name=?", String.class, tableName);

			sb.append(createSql).append("\n\n");

			// 获取示例数据。
			List<Map<String, Object>> sampleRows = jdbcTemplate
				.queryForList("SELECT * FROM " + sanitizeTableName(tableName) + " LIMIT " + SAMPLE_ROWS);

			if (!sampleRows.isEmpty()) {
				sb.append("/*\n");
				sb.append(tableName).append(" 表的前 ").append(SAMPLE_ROWS).append(" 条示例数据：\n");

				// 表头。
				String header = String.join("\t", sampleRows.get(0).keySet());
				sb.append(header).append("\n");

				// 数据行。
				for (Map<String, Object> row : sampleRows) {
					String rowStr = row.values()
						.stream()
						.map(v -> v == null ? "NULL" : String.valueOf(v))
						.collect(Collectors.joining("\t"));
					sb.append(rowStr).append("\n");
				}
				sb.append("*/");
			}
		}
		catch (Exception e) {
			sb.append("获取表 '")
				.append(tableName)
				.append("' 的结构失败：")
				.append(e.getMessage())
				.append("\n");
			sb.append("请先调用 list_tables 确认该表是否存在。");
		}

		return sb.toString();
	}

	private String sanitizeTableName(String tableName) {
		// 基础 SQL 注入防护：只允许字母、数字和下划线。
		if (!tableName.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
			throw new IllegalArgumentException("非法表名：" + tableName);
		}
		return tableName;
	}

	public ToolCallback toolCallback() {
		return FunctionToolCallback.builder("get_schema", this)
			.description("获取指定表的结构，包括 CREATE TABLE 建表语句和示例数据。"
					+ "编写 SQL 前应调用该工具理解表结构和字段含义。"
					+ "输入参数是用逗号分隔的表名列表，例如：'customers, orders, products'。")
			.inputType(Request.class)
			.build();
	}

	@JsonClassDescription("获取指定表结构的请求")
	public record Request(@JsonProperty(value = "tables", required = true)
	@JsonPropertyDescription("需要获取结构的表名列表，多个表名用逗号分隔。例如：'customers, orders, products'") String tables) {
	}

}
