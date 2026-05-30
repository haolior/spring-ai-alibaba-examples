/*
 * Copyright 2025-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.spring.ai.example.stock.client;

import java.util.Map;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ListToolsResult;

/**
 * 💡 这是一个使用 stdio 传输协议的 MCP 客户端示例，它在启动时会自动拉起 MCP 服务端进程。
 * 🚀 运行前，你需要先编译好服务端的 jar 包：
 *
 * <pre>
 * ./mvnw clean install -DskipTests
 * </pre>
 */
public class ClientStdio {

    public static void main(String[] args) {
        // 配置启动底层 MCP 服务端进程的参数（这里是在执行纯 Java 命令）
        var stdioParams = ServerParameters.builder("java")
                .args("-Dspring.ai.mcp.server.stdio=true",          // 开启服务端的 Stdio 支持
                        "-Dspring.main.web-application-type=none",  // 让服务端不要启动 Tomcat 网页服务器
                        "-Dlogging.pattern.console=",               // 关闭控制台日志格式，防止干扰 Stdio 管道的 JSON 传输
                        "-jar",
                        "spring-ai-alibaba-mcp-example/spring-ai-alibaba-mcp-build-example/starter-stock-server/target/starter-stock-server-1.0.0.jar") // 服务端 Jar 包路径
                .build();

        // 创建 Stdio 传输层通道
        var transport = new StdioClientTransport(stdioParams, McpJsonMapper.getDefault());
        // 组装并构建纯同步的原生 MCP 客户端
        var client = McpClient.sync(transport).build();

        try {
            // 🌟 核心：手动发起底层 4 步握手初始化
            client.initialize();

            // 1. 获取并展示服务端上报的所有可用工具列表
            ListToolsResult toolsList = client.listTools();
            System.out.println("📬 当前可用工具列表 = " + toolsList);

            // 2. 测试调用：查询上海股票 (贵州茅台: 600519)
            System.out.println("\n📈 正在测试上海股票 (600519):");
            CallToolResult shStockResult = client.callTool(new CallToolRequest("getStockInfo",
                    Map.of("stockCode", "600519")));
            System.out.println("📊 返回的股票数据: " + shStockResult);

            // 3. 测试调用：查询深圳股票 (平安银行: 000001)
            System.out.println("\n📉 正在测试深圳股票 (000001):");
            CallToolResult szStockResult = client.callTool(new CallToolRequest("getStockInfo",
                    Map.of("stockCode", "000001")));
            System.out.println("📊 返回的股票数据: " + szStockResult);

            // 4. 容错测试：查询一个压根不存在的股票代码 (999999)
            System.out.println("\n🔍 正在测试不存在的股票代码 (999999):");
            try {
                CallToolResult invalidStockResult = client.callTool(new CallToolRequest("getStockInfo",
                        Map.of("stockCode", "999999")));
                System.out.println("📊 返回的股票数据: " + invalidStockResult);
            } catch (Exception e) {
                // 捕获预期的服务端业务异常
                System.out.println("🎯 捕获到预期的业务异常: " + e.getMessage());
            }

            // 5. 容错测试：传入非法的股票代码格式 (abc)
            System.out.println("\n⚠️ 正在测试非法的股票代码格式 (abc):");
            try {
                CallToolResult invalidCodeResult = client.callTool(new CallToolRequest("getStockInfo",
                        Map.of("stockCode", "abc")));
                System.out.println("📊 返回的股票数据: " + invalidCodeResult);
            } catch (Exception e) {
                // 捕获预期的校验异常
                System.out.println("🎯 捕获到预期的校验异常: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("🚨 测试运行期间发生严重错误: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 优雅地关闭客户端，并安全地杀死弹出来的服务端子进程
            client.closeGracefully();
        }
    }
}