package cn.haohaoli.cloud.ai.practice.agent;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Tool Calling 进阶练习。
 *
 * <p>AgentController2 已经负责 {@code RunnableConfig.threadId + MemorySaver}，
 * 所以本 Controller 不再继续练 Memory，改为练 Tool Calling 进阶能力。
 *
 * <p>建议从两个方向补逻辑：
 * <ul>
 *     <li>同时注册多个 Tool，观察模型如何根据工具名和 description 选择工具</li>
 *     <li>使用 ToolContext 传递当前用户、租户、权限等业务上下文</li>
 * </ul>
 *
 * @author lwh
 */
@RestController
@RequestMapping("/agent/tool-calling")
public class AgentController4 {

    /**
     * Tool Calling 进阶占位接口。
     *
     * <p>访问示例：
     * http://localhost:8765/agent/tool-calling/chat?userQuery=帮我查询杭州天气并记录查询人
     *
     * <p>后续补充逻辑：
     * 给 Agent 同时注册多个工具，例如天气查询、时间查询、待办创建，观察模型是否能选择
     * 正确工具；再通过 ToolContext 传递当前用户名或业务参数。
     */
    @GetMapping("/chat")
    public Map<String, String> toolCalling(
            @RequestParam(value = "userQuery", defaultValue = "帮我查询杭州天气并记录查询人", required = false) String userQuery) {
        return Map.of(
                "controller", "AgentController4",
                "topic", "Tool Calling 进阶",
                "userQuery", userQuery
        );
    }

}
