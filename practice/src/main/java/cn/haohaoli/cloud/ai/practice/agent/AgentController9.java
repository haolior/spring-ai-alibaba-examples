package cn.haohaoli.cloud.ai.practice.agent;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * Agent 控制和流式输出练习。
 *
 * <p>本 Controller 用来练习 Agent 的流式返回能力。当前先使用 {@link Flux#just(Object[])}
 * 返回占位流，后续再替换成真正的 {@code agent.stream(...)} 调用。
 *
 * @author lwh
 */
@RestController
@RequestMapping("/agent/streaming")
public class AgentController9 {

    /**
     * 流式输出占位接口。
     *
     * <p>访问示例：
     * http://localhost:8765/agent/streaming/stream?userQuery=流式介绍Agent控制流程
     *
     * <p>后续补充逻辑：
     * 使用 Agent 的 stream API 返回 SSE，并过滤 START / END 等控制事件，只把需要展示的
     * chunk 返回给前端。
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(
            @RequestParam(value = "userQuery", defaultValue = "流式介绍Agent控制流程", required = false) String userQuery) {
        return Flux.just(
                "controller=AgentController9\n",
                "topic=控制和流式输出\n",
                "userQuery=" + userQuery
        );
    }

}
