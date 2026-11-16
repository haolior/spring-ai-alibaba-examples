package cn.haohaoli.cloud.ai.practice.agent;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Tool Interceptor 练习。
 *
 * <p>本 Controller 用来练习工具调用链路上的拦截器，例如记录工具名称、入参、返回值、
 * 执行耗时和异常信息。Interceptor 更适合做审计、日志、兜底处理。
 *
 * @author lwh
 */
@RestController
@RequestMapping("/agent/interceptor")
public class AgentController7 {

    /**
     * 工具拦截器占位接口。
     *
     * <p>访问示例：
     * http://localhost:8765/agent/interceptor/tool?userQuery=调用工具并打印日志
     *
     * <p>后续补充逻辑：
     * 注册自定义 {@code ToolInterceptor}，在工具调用前后打印关键日志，并观察工具
     * 是否被模型实际调用。
     */
    @GetMapping("/tool")
    public Map<String, String> interceptor(
            @RequestParam(value = "userQuery", defaultValue = "调用工具并打印日志", required = false) String userQuery) {
        return Map.of(
                "controller", "AgentController7",
                "topic", "Tool Interceptor",
                "userQuery", userQuery
        );
    }

}
