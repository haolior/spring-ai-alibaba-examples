package com.cloud.alibaba.ai.example.controller;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class AgentController {

    private final ReactAgent reactAgent;

    // 保存每个 threadId 对应的中断元数据，/feedback 会用它恢复同一次 Agent 执行。
    private final Map<String, InterruptionMetadata> map = new ConcurrentHashMap<>();

    public AgentController(ReactAgent reactAgent) {
        this.reactAgent = reactAgent;
    }

    @GetMapping("/invoke")
    @ResponseBody
    public List<InterruptionMetadata.ToolFeedback> invoke(@RequestParam("query") String query,
                       @RequestParam("threadId") String threadId
    ) throws Exception {
        RunnableConfig runnableConfig = RunnableConfig.builder().threadId(threadId).build();

        // 本示例专门演示 file_write 的人工审批流程，因此期望 invoke 返回 InterruptionMetadata。
        // 如果 query 只是普通聊天，没有触发 file_write，这里会因为返回 NodeOutput 而类型转换失败。
        InterruptionMetadata metadata = (InterruptionMetadata) reactAgent.invokeAndGetOutput(query, runnableConfig).orElseThrow();
        map.put(threadId, metadata);
        return metadata.toolFeedbacks();
    }

    @PostMapping("/feedback")
    @ResponseBody
    public String feedback(@RequestBody List<Feedback> feedbacks,
                         @RequestParam("threadId") String threadId
    ) throws Exception {
        InterruptionMetadata metadata = map.get(threadId);
        if(metadata == null) {
            return "未找到待恢复的审批上下文，请确认 threadId 是否和 /invoke 请求一致";
        }
        if(metadata.toolFeedbacks().size() != feedbacks.size()) {
            return "审批反馈数量和待审批工具调用数量不一致";
        }

        InterruptionMetadata.Builder newBuilder = InterruptionMetadata.builder()
                .nodeId(metadata.node())
                .state(metadata.state());
        for (int i = 0; i < feedbacks.size(); i++) {
            var toolFeedback = metadata.toolFeedbacks().get(i);
            InterruptionMetadata.ToolFeedback.Builder editedFeedbackBuilder = InterruptionMetadata.ToolFeedback
                    .builder(toolFeedback);
            // 将前端提交的批准/拒绝结果写回 ToolFeedback，供 HumanInTheLoopHook 恢复执行时读取。
            if(feedbacks.get(i).isApproved()) {
                editedFeedbackBuilder.result(InterruptionMetadata.ToolFeedback.FeedbackResult.APPROVED);
            } else {
                editedFeedbackBuilder.result(InterruptionMetadata.ToolFeedback.FeedbackResult.REJECTED)
                        .description(feedbacks.get(i).feedback());
            }
            newBuilder.addToolFeedback(editedFeedbackBuilder.build());
        }
        RunnableConfig resumeRunnableConfig = RunnableConfig.builder().threadId(threadId)
                .addMetadata(RunnableConfig.HUMAN_FEEDBACK_METADATA_KEY, newBuilder.build())
                .build();
        reactAgent.invokeAndGetOutput("", resumeRunnableConfig);
        return "执行成功";
    }

    @GetMapping
    public String index() {
        return "index";
    }
}
