package cn.haohaoli.cloud.ai.practice.agent;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Agentic RAG 练习。
 *
 * <p>本 Controller 用来练习把检索能力作为工具交给 Agent，而不是在每次请求中固定执行
 * RAG 流程。目标是观察模型何时决定检索、检索后如何继续推理。
 *
 * @author lwh
 */
@RestController
@RequestMapping("/agent/rag")
public class AgentController10 {

    /**
     * Agentic RAG 占位接口。
     *
     * <p>访问示例：
     * http://localhost:8765/agent/rag/search?userQuery=查询项目里的Agent记忆说明
     *
     * <p>后续补充逻辑：
     * 封装一个知识检索 Tool，让 Agent 自主判断是否调用检索工具，并把检索结果作为后续
     * 推理依据。
     */
    @GetMapping("/search")
    public Map<String, String> rag(
            @RequestParam(value = "userQuery", defaultValue = "查询项目里的Agent记忆说明", required = false) String userQuery) {
        return Map.of(
                "controller", "AgentController10",
                "topic", "Agentic RAG",
                "userQuery", userQuery
        );
    }

}
