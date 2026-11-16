package cn.haohaoli.cloud.ai.practice.agent;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Agent 输出控制练习。
 *
 * <p>本 Controller 用来从 {@code call} 过渡到 {@code invokeAndGetOutput}，
 * 观察 {@code NodeOutput}、状态数据和特殊输出类型。后续 Human-in-the-Loop 会依赖
 * 这种更完整的输出形态。
 *
 * @author lwh
 */
@RestController
@RequestMapping("/agent/output-control")
public class AgentController5 {

    /**
     * 完整输出占位接口。
     *
     * <p>访问示例：
     * http://localhost:8765/agent/output-control/output?userQuery=帮我创建一个学习计划
     *
     * <p>后续补充逻辑：
     * 使用 {@code reactAgent.invokeAndGetOutput(...)} 获取完整输出，并尝试从
     * {@code NodeOutput.state()} 中提取最终文本或中间状态。
     */
    @GetMapping("/output")
    public Map<String, String> output(
            @RequestParam(value = "userQuery", defaultValue = "帮我创建一个学习计划", required = false) String userQuery) {
        return Map.of(
                "controller", "AgentController5",
                "topic", "invokeAndGetOutput + NodeOutput",
                "userQuery", userQuery
        );
    }

}
