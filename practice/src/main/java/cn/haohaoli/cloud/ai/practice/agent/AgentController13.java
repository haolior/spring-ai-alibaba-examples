package cn.haohaoli.cloud.ai.practice.agent;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 多智能体 / 工作流 / A2A 综合练习。
 *
 * <p>本 Controller 放在最后，用来练习多个 Agent 的协作、顺序工作流，或通过 A2A
 * 调用远端 Agent。它是综合阶段，不建议在基础能力没熟之前先实现。
 *
 * @author lwh
 */
@RestController
@RequestMapping("/agent/multi-agent")
public class AgentController13 {

    /**
     * 多 Agent 审核占位接口。
     *
     * <p>访问示例：
     * http://localhost:8765/agent/multi-agent/audit?userQuery=审核并修订这段回答
     *
     * <p>后续补充逻辑：
     * 可以先实现 Critic -> Reviser 两个 Agent 的顺序协作，再扩展到 A2A 服务端和客户端。
     */
    @GetMapping("/audit")
    public Map<String, String> multiAgent(
            @RequestParam(value = "userQuery", defaultValue = "审核并修订这段回答", required = false) String userQuery) {
        return Map.of(
                "controller", "AgentController13",
                "topic", "Multi-agent / Workflow / A2A",
                "userQuery", userQuery
        );
    }

}
