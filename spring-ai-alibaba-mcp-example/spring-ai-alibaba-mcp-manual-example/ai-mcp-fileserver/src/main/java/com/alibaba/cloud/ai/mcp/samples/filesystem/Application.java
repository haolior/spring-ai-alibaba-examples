/*
 * Copyright 2024-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.ai.mcp.samples.filesystem;

import java.nio.file.Paths;
import java.time.Duration;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;

import io.modelcontextprotocol.json.McpJsonMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

// https://docs.springjava.cn/spring-ai/reference/api/mcp/mcp-client-boot-starter-docs.html#_windows_stdio_configuration
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public CommandLineRunner predefinedQuestions(ChatClient.Builder chatClientBuilder,
			McpSyncClient mcpClient, ConfigurableApplicationContext context) {

		return args -> {

			// 将 MCP 客户端注入到 Spring AI 的工具提供者中
			var chatClient = chatClientBuilder
					.defaultToolCallbacks(SyncMcpToolCallbackProvider.builder().mcpClients(mcpClient).build())
					.build();

			System.out.println("🚀 开始运行预设问题，正在等待 AI 模型响应：\n");

			// 🌟 测试问题 1：让大模型去读你的硬盘文件
			String question1 = "你能帮我解释一下 spring-ai-mcp-overview.txt 这个文件里面具体写了什么内容吗？";
			System.out.println("❓ 客户端提问: " + question1);
			System.out.println("🤖 AI 助手回答: " + chatClient.prompt(question1).call().content());

			// 🌟 测试问题 2：让大模型在你的硬盘里写一个新文件
			String question2 = "请帮我把 spring-ai-mcp-overview.txt 的内容提炼一个摘要，并把它以 Markdown 格式保存到一个新文件 summary.md 中。";
			System.out.println("\n❓ 客户端提问: " + question2);
			System.out.println("🤖 AI 助手回答: " + chatClient.prompt(question2).call().content());

			// 运行完自动关闭容器
			context.close();

		};
	}


	@Bean(destroyMethod = "close")
	public McpSyncClient mcpClient() {

		// based on
		// https://github.com/modelcontextprotocol/servers/tree/main/src/filesystem
		// Windows 系统需要改为 npx.cmd
		var stdioParams = ServerParameters.builder("npx.cmd")
				.args("-y", "@modelcontextprotocol/server-filesystem", getDbPath())
				.build();

		var mcpClient = McpClient.sync(new StdioClientTransport(stdioParams, McpJsonMapper.getDefault()))
				.requestTimeout(Duration.ofSeconds(10)).build();

		var init = mcpClient.initialize();

		// 🌟 翻译了这句初始化成功的日志
		System.out.println("🎉 MCP 客户端初始化成功，服务器握手信息: " + init);

		return mcpClient;

	}

	private static String getDbPath() {

		// spring-ai-alibaba-examples/spring-ai-alibaba-mcp-example/spring-ai-alibaba-manual-mcp-example/ai-mcp-fileserver/target
		// windows use: spring-ai-alibaba-mcp-example/spring-ai-alibaba-manual-mcp-example/ai-mcp-fileserver/target
		String path = Paths.get(System.getProperty("user.dir"), "spring-ai-alibaba-mcp-example/spring-ai-alibaba-mcp-manual-example/ai-mcp-fileserver/data").toString();
		System.out.println(path);

		return path;
	}

}
