package cn.haohaoli.cloud.ai.practice.agent;

import cn.haohaoli.cloud.ai.practice.agent.config.WeatherFunction;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.ai.chat.model.ChatModel;

/**
 * Agent Memory 入门练习。
 *
 * <p>本 Controller 用来承接 {@code AgentController1} 中移出的配置调用能力：
 * {@code RunnableConfig.threadId} 和 {@code MemorySaver}。建议先不加 Saver 跑一遍，
 * 再加上 Saver 跑同一个 {@code threadId}，观察 Agent 状态是否能被保存和恢复。
 *
 * @author lwh
 */
@RestController
@RequestMapping("/agent/memory")
public class AgentController2 {

    private final ReactAgent reactAgent;

    public AgentController2(ChatModel chatModel) {
        this.reactAgent = ReactAgent.builder()
                .name("weather_agent")
                .model(chatModel)
                .tools(new WeatherFunction().getCallback())
                .saver(new MemorySaver())
                .systemPrompt("你是一个天气查询小助手")
                .build();
    }

    /**
     * Memory 基础调用占位接口。
     *
     * <p>访问示例：
     * http://localhost:8765/agent/memory/call?userQuery=杭州天气怎么样&threadId=memory-demo
     */
    @GetMapping("/call")
    public String call(
            @RequestParam(value = "userQuery", defaultValue = "杭州天气怎么样", required = false) String userQuery,
            @RequestParam(value = "threadId", defaultValue = "default_thread_id", required = false) String threadId) throws GraphRunnerException {
        RunnableConfig runnableConfig = RunnableConfig.builder().threadId(threadId).build();
        return reactAgent.call(userQuery, runnableConfig)
                .getText();

    }

}
