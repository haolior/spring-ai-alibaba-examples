package cn.haohaoli.cloud.ai.practice.agent;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Human-in-the-Loop 练习。
 *
 * <p>本 Controller 用来练习有副作用工具执行前的人工审批流程。推荐先实现一个简单的
 * {@code create_todo} 或 {@code file_write} 工具，再用 Hook 在工具执行前返回审批元数据。
 *
 * @author lwh
 */
@RestController
@RequestMapping("/agent/human-in-loop")
public class AgentController6 {

    /**
     * 发起需要人工审批的 Agent 调用。
     *
     * <p>访问示例：
     * http://localhost:8765/agent/human-in-loop/invoke?userQuery=帮我创建一个待办&threadId=approval-1
     *
     * <p>后续补充逻辑：
     * 使用 {@code HumanInTheLoopHook} 拦截指定工具，返回待审批的
     * {@code InterruptionMetadata.ToolFeedback}。
     */
    @GetMapping("/invoke")
    public Map<String, String> invoke(
            @RequestParam(value = "userQuery", defaultValue = "帮我创建一个待办", required = false) String userQuery,
            @RequestParam(value = "threadId", defaultValue = "approval-1", required = false) String threadId) {
        return Map.of(
                "controller", "AgentController6",
                "topic", "Human-in-the-Loop invoke",
                "userQuery", userQuery,
                "threadId", threadId
        );
    }

    /**
     * 提交人工审批结果并恢复 Agent 执行。
     *
     * <p>访问示例：
     * http://localhost:8765/agent/human-in-loop/feedback?threadId=approval-1&approved=true
     *
     * <p>后续补充逻辑：
     * 将审批结果写入 {@code RunnableConfig.HUMAN_FEEDBACK_METADATA_KEY}，再用同一个
     * {@code threadId} 恢复中断前的 Agent 执行现场。
     */
    @PostMapping("/feedback")
    public Map<String, String> feedback(
            @RequestParam(value = "threadId", defaultValue = "approval-1", required = false) String threadId,
            @RequestParam(value = "approved", defaultValue = "true", required = false) boolean approved) {
        return Map.of(
                "controller", "AgentController6",
                "topic", "Human-in-the-Loop feedback",
                "threadId", threadId,
                "approved", String.valueOf(approved)
        );
    }

}
