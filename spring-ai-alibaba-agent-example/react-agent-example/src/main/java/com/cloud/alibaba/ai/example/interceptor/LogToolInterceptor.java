package com.cloud.alibaba.ai.example.interceptor;

import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallHandler;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallRequest;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallResponse;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LogToolInterceptor extends ToolInterceptor {

    private static final Logger log = LoggerFactory.getLogger(LogToolInterceptor.class);

    @Override
    public ToolCallResponse interceptToolCall(ToolCallRequest request, ToolCallHandler handler) {
        // 仅记录工具调用入口，实际执行仍交给后续 handler。
        log.info("工具拦截器：即将调用工具 {}", request.getToolName());
        return handler.call(request);
    }

    @Override
    public String getName() {
        return "工具调用日志拦截器";
    }
}
