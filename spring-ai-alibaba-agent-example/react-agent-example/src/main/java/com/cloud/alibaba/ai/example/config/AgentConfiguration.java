package com.cloud.alibaba.ai.example.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.hip.HumanInTheLoopHook;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.cloud.alibaba.ai.example.interceptor.LogToolInterceptor;
import com.cloud.alibaba.ai.example.tools.FileReadTool;
import com.cloud.alibaba.ai.example.tools.FileWriteTool;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentConfiguration {

    private final ChatModel chatModel;

    public AgentConfiguration(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Bean
    public ReactAgent reactAgent() throws GraphStateException {
        return ReactAgent.builder()
                .name("agent")
                .description("用于演示工具调用和人工审批的 ReAct 智能体")
                .model(chatModel)
                // MemorySaver 会按 threadId 保存中断现场，/feedback 恢复执行时依赖这份内存状态。
                .saver(new MemorySaver())
                .tools(
                        new FileReadTool().toolCallback(),
                        new FileWriteTool().toolCallback()
                )
                .hooks(HumanInTheLoopHook.builder()
                        // 写文件属于有副作用操作，执行前先返回 InterruptionMetadata 等待人工审批。
                        .approvalOn("file_write", "写入文件前需要人工审批")
                        .build())
                .interceptors(new LogToolInterceptor())
                .build();
    }
}
