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

package com.alibaba.cloud.ai.example.llm.auditor.tool;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Tavily 联网搜索工具。
 *
 * <p>该工具用于给审稿 Agent 提供外部事实依据，返回摘要、来源标题、内容片段和链接。
 *
 * @author : zhengyuchao
 * @date : 2026/1/22
 */
public class WebSearchTool implements BiFunction<WebSearchTool.Request, ToolContext, String> {

    private static final Logger log = LoggerFactory.getLogger(WebSearchTool.class);


    private final String tavilyApiKey;

    private static final String TAVILY_URL = "https://api.tavily.com/search";

    private final RestTemplate restTemplate;

    public WebSearchTool(String tavilyApiKey) {
        this.restTemplate = new RestTemplate();
        this.tavilyApiKey = tavilyApiKey;
    }

    @Override
    public String apply(Request request, ToolContext toolContext) {
        log.info("Tavily 开始联网搜索：{}", request.query);

        try {
            // 1. 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 2. 构建请求体
            // include_answer: 让 Tavily 生成一段简短的回答
            // search_depth: "basic" (快) 或 "advanced" (深，但耗额度)
            Map<String, Object> body = Map.of(
                    "api_key", tavilyApiKey,
                    "query", request.query,
                    "search_depth", "basic",
                    "include_answer", true,
                    "max_results", request.maxResults != null ? request.maxResults : 5
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            // 3. 发送 POST 请求
            TavilyResponse response = restTemplate.postForObject(TAVILY_URL, entity, TavilyResponse.class);

            // 4. 处理并格式化结果给 AI
            if (response == null || response.results == null || response.results.isEmpty()) {
                return "未找到关于 '" + request.query + "' 的相关信息。";
            }

            StringBuilder output = new StringBuilder();

            // 如果 Tavily 生成了直接回答，优先放入
            if (response.answer != null && !response.answer.isEmpty()) {
                output.append("【AI 摘要】: ").append(response.answer).append("\n\n");
            }

            output.append("【详细来源】:\n");
            for (int i = 0; i < response.results.size(); i++) {
                TavilyResult result = response.results.get(i);
                output.append(i + 1).append(". ").append(result.title).append("\n");
                output.append("   内容: ").append(result.content).append("\n");
                output.append("   链接: ").append(result.url).append("\n\n");
            }

            String finalResult = output.toString();
            // log.info("搜索结果：{}", finalResult); // 调试时可以打开
            return finalResult;

        } catch (Exception e) {
            log.error("Tavily 联网搜索失败", e);
            return "搜索服务异常：" + e.getMessage();
        }
    }

    public static FunctionToolCallback getFunctionToolCallback(String tavilyApiKey) {
        return FunctionToolCallback.builder("web_search", new WebSearchTool(tavilyApiKey))
                .description("联网搜索工具。用于查询实时新闻、具体事实、游戏攻略或现有知识库中没有的信息。")
                .inputType(Request.class)
                .build();
    }

    // DTO 类定义。

    @JsonClassDescription("搜索请求参数")
    public record Request(
            @JsonProperty(value = "query", required = true)
            @JsonPropertyDescription("搜索关键词")
            String query,

            @JsonProperty(value = "max_results")
            @JsonPropertyDescription("结果数量，默认5")
            Integer maxResults
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class TavilyResponse {
        @JsonProperty("answer")
        public String answer; // Tavily 自动总结的答案。

        @JsonProperty("results")
        public List<TavilyResult> results;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class TavilyResult {
        @JsonProperty("title")
        public String title;

        @JsonProperty("url")
        public String url;

        @JsonProperty("content")
        public String content; // 经过清洗的纯文本片段，适合交给模型引用。
    }
}
