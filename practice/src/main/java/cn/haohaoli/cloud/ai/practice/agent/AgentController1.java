package cn.haohaoli.cloud.ai.practice.agent;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Agent 基础调用练习。
 *
 * <p>本 Controller 只练三个最小概念：
 * <ul>
 *     <li>如何手动构建一个 {@link ReactAgent}</li>
 *     <li>如何把一个最小 Tool 挂到 Agent 上</li>
 *     <li>如何使用 {@code call} 和 {@code invoke} 调用 Agent</li>
 * </ul>
 *
 * @author lwh
 */
@RestController
@RequestMapping("/agent")
public class AgentController1 {

    private final ReactAgent reactAgent;

    public AgentController1(ChatModel chatModel) {
        // 1. 定义一个最小天气工具：模型提取 city 后，会把参数封装成 WeatherRequest 调用这里。
        BiFunction<WeatherRequest, ToolContext, String> weatherFunction =
                (request, toolContext) -> request.city() + "现在是晴天!";

        // 2. 将普通 Java 函数包装成 Agent 可识别的 ToolCallback。
        // 工具名和描述会影响模型是否选择调用该工具。
        ToolCallback weatherTool = FunctionToolCallback.builder("get_weather", weatherFunction)
                .description("根据给定的城市获取天气")
                .inputType(WeatherRequest.class)
                .build();

        // 3. 构建最小 ReactAgent：模型 + 工具 + 系统提示词。
        this.reactAgent = ReactAgent.builder()
                .name("weather_agent")
                .model(chatModel)
                .tools(weatherTool)
                .systemPrompt("你是一个天气查询小助手")
                .build();
    }

    /**
     * 基础同步调用，只返回 Agent 最终输出文本。
     *
     * <p>访问示例：
     * http://localhost:8765/agent/call?userQuery=杭州天气怎么样
     */
    @GetMapping("/call")
    public String call(@RequestParam(value = "userQuery", defaultValue = "杭州天气怎么样？", required = false) String userQuery) throws GraphRunnerException {
        return reactAgent.call(userQuery)
                .getText();
    }

    /**
     * 基础状态调用，返回 Agent 执行后的完整状态字符串。
     *
     * <p>访问示例：
     * http://localhost:8765/agent/invoke?userQuery=杭州天气怎么样
     *
     * <p>和 {@link #call(String)} 的区别：
     * {@code call} 更适合只关心最终文本；{@code invoke} 更适合观察 Agent 执行状态。
     */
    @GetMapping(value = "/invoke", produces = MediaType.APPLICATION_JSON_VALUE)
    public String invoke(@RequestParam(value = "userQuery", defaultValue = "杭州天气怎么样？", required = false) String userQuery) throws GraphRunnerException {
        Optional<OverAllState> invoke = reactAgent.invoke(userQuery);
        return invoke.map(OverAllState::toString).orElse(null);
    }

    /**
     * 天气工具入参。
     *
     * @param city 模型从用户问题中提取出的城市名
     */
    public record WeatherRequest(String city) {
    }
}
