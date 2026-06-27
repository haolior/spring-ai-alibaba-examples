package cn.haohaoli.cloud.ai.practice.agent;

import cn.haohaoli.cloud.ai.practice.agent.config.WeatherFunction;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Agent 结构化输出练习。
 *
 * <p>本 Controller 用来练习让 Agent 返回稳定结构，例如 Java Bean、JSON 或 Schema
 * 约束后的结果，避免只依赖自然语言文本解析。
 *
 * <p>官网提供了两种结构化输出配置方式：
 * <ul>
 * <li>{@code outputType(Class<?>)}：最简洁的推荐写法，直接传入 Java 类型，框架内部会使用
 * {@link BeanOutputConverter} 将 Java 类型转换为 JSON schema。</li>
 * <li>{@code outputSchema(String)}：更灵活的写法，直接传入 schema/format 字符串；这个字符串既可以来自
 * {@link BeanOutputConverter#getFormat()}，也可以是手写的自定义 schema。</li>
 * </ul>
 *
 * <p>从 ReactAgent 构建逻辑看，二者最终都会落到 outputSchema 上；如果同时配置
 * {@code outputSchema} 和 {@code outputType}，会优先使用 {@code outputSchema}。
 *
 * @author lwh
 */
@RestController
@RequestMapping("/agent/structured-output")
public class AgentController3 {

    private static final Logger log = LoggerFactory.getLogger(AgentController3.class);

    private static final String WEATHER_AGENT_PROMPT = """
            你是一个天气查询小助手。
            用户询问天气时，必须先调用 get_weather 工具获取天气结果。
            最终回答必须基于工具返回值，并按照结构化输出要求返回。
            """;

    @Autowired
    private ChatModel chatModel;

    /**
     * 方式一：使用 outputSchema(String) 配置结构化输出。
     *
     * <p>访问示例：
     * http://localhost:8765/agent/structured-output/schema?userQuery=杭州天气怎么样？
     *
     * <p>适合场景：
     * 已经有外部 JSON Schema，或希望手动控制 Schema 内容时使用。这里为了方便对比，仍然使用
     * {@link BeanOutputConverter} 从 {@link WeatherAnswer} 生成 schema，再手动传给
     * {@code outputSchema}。
     */
    @GetMapping("/schema")
    public AssistantMessage schema(
            @RequestParam(value = "userQuery", defaultValue = "杭州天气怎么样？", required = false) String userQuery,
            @RequestParam(value = "threadId", defaultValue = "default_thread_id", required = false) String threadId) throws GraphRunnerException {
        RunnableConfig runnableConfig = RunnableConfig.builder().threadId(threadId).build();
        BeanOutputConverter<WeatherAnswer> converter = new BeanOutputConverter<>(WeatherAnswer.class);
        log.info("schema: {}", converter.getFormat());

        return ReactAgent.builder()
                .name("weather_agent")
                .model(chatModel)
                .tools(new WeatherFunction().getCallback())
                .saver(new MemorySaver())
                .systemPrompt(WEATHER_AGENT_PROMPT)
                .outputSchema(converter.getFormat())
                .build()
                .call(userQuery, runnableConfig);

    }

    /**
     * 方式二：使用 outputType(Class<?>) 配置结构化输出。
     *
     * <p>访问示例：
     * http://localhost:8765/agent/structured-output/type?userQuery=杭州天气怎么样
     *
     * <p>适合场景：
     * 已经有 Java 类型，希望框架根据类型自动生成 schema。官网推荐优先使用这种方式，因为调用方只需要维护
     * Java 类型，schema 生成细节交给框架处理。
     */
    @GetMapping("/type")
    public String type(
            @RequestParam(value = "userQuery", defaultValue = "杭州天气怎么样", required = false) String userQuery,
            @RequestParam(value = "threadId", defaultValue = "default_thread_id", required = false) String threadId) throws GraphRunnerException {
        RunnableConfig runnableConfig = RunnableConfig.builder().threadId(threadId).build();
        return ReactAgent.builder()
                .name("weather_agent")
                .model(chatModel)
                .tools(new WeatherFunction().getCallback())
                .saver(new MemorySaver())
                .systemPrompt(WEATHER_AGENT_PROMPT)
                .outputType(WeatherAnswer.class)
                .build()
                .call(userQuery, runnableConfig)
                .getText();

    }

    /**
     * 天气结构化输出结果。
     *
     * @param city 城市名
     * @param weather 天气情况
     * @param suggestion 出行建议
     */
    public record WeatherAnswer(String city, String weather, String suggestion) {
    }

}
