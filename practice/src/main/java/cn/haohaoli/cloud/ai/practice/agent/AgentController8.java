package cn.haohaoli.cloud.ai.practice.agent;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Model / Agent Hook 练习。
 *
 * <p>本 Controller 用来练习 Hook 在 Agent 或模型调用前后的控制能力，例如修改消息、
 * 读取状态、清洗输出或提取引用信息。Hook 比 Interceptor 更靠近 Agent 执行状态。
 *
 * @author lwh
 */
@RestController
@RequestMapping("/agent/hook")
public class AgentController8 {

    /**
     * Hook 占位接口。
     *
     * <p>访问示例：
     * http://localhost:8765/agent/hook/model?userQuery=生成一段需要清洗的回答
     *
     * <p>后续补充逻辑：
     * 实现一个简单 Hook，在模型输出后清理标记文本，或在调用前向状态中补充上下文。
     */
    @GetMapping("/model")
    public Map<String, String> hook(
            @RequestParam(value = "userQuery", defaultValue = "生成一段需要清洗的回答", required = false) String userQuery) {
        return Map.of(
                "controller", "AgentController8",
                "topic", "Model / Agent Hook",
                "userQuery", userQuery
        );
    }

}
