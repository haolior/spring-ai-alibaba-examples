package cn.haohaoli.cloud.ai.practice.mcp.config;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolConfig {

    @Bean
    public ToolCallbackProvider localToolCallbacks (Tools tools) {
        return MethodToolCallbackProvider.builder().toolObjects(tools).build();
    }



}
