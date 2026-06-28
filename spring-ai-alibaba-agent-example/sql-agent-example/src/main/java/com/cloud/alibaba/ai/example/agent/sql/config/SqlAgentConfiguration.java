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
package com.cloud.alibaba.ai.example.agent.sql.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.cloud.alibaba.ai.example.agent.sql.tool.ExecuteQueryTool;
import com.cloud.alibaba.ai.example.agent.sql.tool.GetSchemaTool;
import com.cloud.alibaba.ai.example.agent.sql.tool.ListTablesTool;
import com.cloud.alibaba.ai.example.agent.sql.tool.QueryCheckerTool;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SQL Agent 配置。
 * <p>
 * 该配置创建一个具备数据库交互能力的 ReactAgent。Agent 可以自主完成：
 * <ul>
 *   <li>列出数据库中可用的表</li>
 *   <li>获取表结构信息</li>
 *   <li>执行前检查 SQL 查询</li>
 *   <li>执行查询并解释结果</li>
 * </ul>
 * </p>
 *
 * @author zth9
 * @since 2026-01-22
 */
@Configuration
public class SqlAgentConfiguration {

	private static final String SYSTEM_PROMPT = """
			你是一个专门和 SQL 数据库交互的智能体。
			收到用户问题后，请先理解问题，再生成语法正确的 SQLite 查询，
			然后查看查询结果，并用自然语言回答用户。

			除非用户明确要求返回指定数量的数据，否则查询结果最多限制为 10 条。

			你可以根据相关字段排序，优先返回最有价值的数据。不要查询某张表的所有字段，
			只查询回答问题所需的相关字段。

			执行 SQL 前必须先检查查询。如果执行查询时报错，请改写 SQL 后重试。

			禁止对数据库执行任何 DML 或 DDL 操作，例如 INSERT、UPDATE、DELETE、DROP 等。
			为了安全，只允许执行 SELECT 查询。

			开始回答前，必须先查看数据库中有哪些表，确认可以查询的数据范围。不要跳过这一步。

			然后查询最相关表的结构，理解表字段和示例数据。

			拿到表结构后，必须先调用 check_query 检查 SQL，再执行查询。

			最后执行查询，并根据查询结果给出清晰的中文回答。

			请按下面顺序工作：
			1. 先调用 list_tables 查看可用表
			2. 再调用 get_schema 获取相关表结构
			3. 然后调用 check_query 检查 SQL
			4. 最后调用 execute_query 获取结果
			5. 汇总查询结果，给出对用户有帮助的中文回答
			""";

	private final ChatModel chatModel;

	private final ListTablesTool listTablesTool;

	private final GetSchemaTool getSchemaTool;

	private final QueryCheckerTool queryCheckerTool;

	private final ExecuteQueryTool executeQueryTool;

	public SqlAgentConfiguration(ChatModel chatModel, ListTablesTool listTablesTool, GetSchemaTool getSchemaTool,
			QueryCheckerTool queryCheckerTool, ExecuteQueryTool executeQueryTool) {
		this.chatModel = chatModel;
		this.listTablesTool = listTablesTool;
		this.getSchemaTool = getSchemaTool;
		this.queryCheckerTool = queryCheckerTool;
		this.executeQueryTool = executeQueryTool;
	}

	@Bean
	public ReactAgent sqlAgent() throws GraphStateException {
		return ReactAgent.builder()
			.name("sql-agent")
			.description(SYSTEM_PROMPT)
			.model(chatModel)
			.saver(new MemorySaver())
			.tools(listTablesTool.toolCallback(), getSchemaTool.toolCallback(), queryCheckerTool.toolCallback(),
					executeQueryTool.toolCallback())
			.build();
	}

}
