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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 执行 SQL 查询的工具。
 * <p>
 * 该工具执行 SELECT 查询并返回结果。出于安全考虑，只允许 SELECT 语句，并阻止 DML / DDL 操作。
 * </p>
 *
 * @author zth9
 * @since 2026-01-22
 */
@Component
public class ExecuteQueryTool implements BiFunction<ExecuteQueryTool.Request, ToolContext, String> {

	private static final Logger logger = LoggerFactory.getLogger(ExecuteQueryTool.class);

	private static final Pattern DML_PATTERN = Pattern
		.compile("^\\s*(INSERT|UPDATE|DELETE|DROP|CREATE|ALTER|TRUNCATE|REPLACE)\\s+", Pattern.CASE_INSENSITIVE);

	private final JdbcTemplate jdbcTemplate;

	@Value("${sql-agent.max-results:10}")
	private int maxResults;

	public ExecuteQueryTool(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public String apply(Request request, ToolContext toolContext) {
		logger.info("========== 执行 SQL 查询工具开始执行 ==========");
		logger.info("待执行 SQL：{}", request.query());

		String query = request.query().trim();

		// 安全检查：阻止 DML / DDL 语句。
		if (DML_PATTERN.matcher(query).find()) {
			String errorMsg = "错误：不允许执行 INSERT、UPDATE、DELETE、DROP 等 DML / DDL 语句。"
					+ "为了安全，该 Agent 只支持 SELECT 查询。";
			logger.warn(errorMsg);
			return errorMsg;
		}

		try {
			// 如果 SQL 中没有 LIMIT，则自动追加 LIMIT，避免返回过多数据。
			String limitedQuery = addLimitIfNeeded(query);

			List<Map<String, Object>> results = jdbcTemplate.queryForList(limitedQuery);

			if (results.isEmpty()) {
				logger.info("查询没有返回结果");
				return "查询执行成功，但没有找到结果。";
			}

			String resultStr = formatResults(results);
			logger.info("查询返回 {} 行结果", results.size());
			logger.info("========== 执行 SQL 查询工具执行结束 ==========");

			return resultStr;
		}
		catch (Exception e) {
			logger.error("执行 SQL 查询失败", e);
			return "执行 SQL 查询失败：" + e.getMessage()
					+ "\n\n请检查 SQL 语法后重试。"
					+ "可以先使用 get_schema 确认表名和字段名。";
		}
	}

	private String addLimitIfNeeded(String query) {
		String lowerQuery = query.toLowerCase();
		if (!lowerQuery.contains(" limit ") && !lowerQuery.contains("\nlimit ")) {
			// 如果末尾有分号，先移除再追加 LIMIT。
			if (query.endsWith(";")) {
				query = query.substring(0, query.length() - 1);
			}
			return query + " LIMIT " + maxResults;
		}
		return query;
	}

	private String formatResults(List<Map<String, Object>> results) {
		if (results.isEmpty()) {
			return "没有找到结果。";
		}

		StringBuilder sb = new StringBuilder();

		// 格式化为可读表格。
		List<String> columns = results.get(0).keySet().stream().toList();

		// 表头。
		sb.append(String.join(" | ", columns)).append("\n");
		sb.append("-".repeat(columns.stream().mapToInt(String::length).sum() + (columns.size() - 1) * 3)).append("\n");

		// 数据行。
		for (Map<String, Object> row : results) {
			String rowStr = columns.stream()
				.map(col -> row.get(col) == null ? "NULL" : String.valueOf(row.get(col)))
				.collect(Collectors.joining(" | "));
			sb.append(rowStr).append("\n");
		}

		sb.append("\n（返回 ").append(results.size()).append(" 行结果）");

		return sb.toString();
	}

	public ToolCallback toolCallback() {
		return FunctionToolCallback.builder("execute_query", this)
			.description("执行 SQL SELECT 查询并返回结果。重要：为了安全，只允许执行 SELECT 查询，"
					+ "INSERT、UPDATE、DELETE、DROP 等语句会被拒绝。执行前应先使用 check_query 检查 SQL。"
					+ "默认最多返回 " + maxResults + " 行结果。")
			.inputType(Request.class)
			.build();
	}

	@JsonClassDescription("执行 SQL 查询的请求")
	public record Request(@JsonProperty(value = "query", required = true)
	@JsonPropertyDescription("要执行的 SQL SELECT 查询语句。只允许 SELECT 语句。") String query) {
	}

}
