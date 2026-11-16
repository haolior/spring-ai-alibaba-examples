package cn.haohaoli.cloud.ai.practice.agent;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * SQL Agent 练习。
 *
 * <p>本 Controller 用来练习自然语言到 SQL 的 Agent 工具链。建议拆成列出数据表、
 * 获取表结构、SQL 检查、SQL 执行几个小工具，再交给 Agent 自主编排。
 *
 * @author lwh
 */
@RestController
@RequestMapping("/agent/sql")
public class AgentController11 {

    /**
     * SQL Agent 查询占位接口。
     *
     * <p>访问示例：
     * http://localhost:8765/agent/sql/query?userQuery=查询订单数量
     *
     * <p>后续补充逻辑：
     * 让 Agent 先通过 Schema 工具理解表结构，再生成 SQL，经过检查后执行查询并解释结果。
     */
    @GetMapping("/query")
    public Map<String, String> sql(
            @RequestParam(value = "userQuery", defaultValue = "查询订单数量", required = false) String userQuery) {
        return Map.of(
                "controller", "AgentController11",
                "topic", "SQL Agent",
                "userQuery", userQuery
        );
    }

}
