package cn.haohaoli.cloud.ai.practice.agent;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Skills 练习。
 *
 * <p>本 Controller 用来练习 Skill 机制。Skill 更像能力组织方式，适合在已经会写 Tool
 * 和 Agent 后再学习，用来把一组说明、脚本、参考资料组织成可复用能力。
 *
 * @author lwh
 */
@RestController
@RequestMapping("/agent/skills")
public class AgentController12 {

    /**
     * Skill 调用占位接口。
     *
     * <p>访问示例：
     * http://localhost:8765/agent/skills/chat?userQuery=使用技能搜索论文
     *
     * <p>后续补充逻辑：
     * 加载一个最小 Skill，例如论文搜索或网页研究，让 Agent 根据用户问题选择是否使用它。
     */
    @GetMapping("/chat")
    public Map<String, String> skills(
            @RequestParam(value = "userQuery", defaultValue = "使用技能搜索论文", required = false) String userQuery) {
        return Map.of(
                "controller", "AgentController12",
                "topic", "Skills",
                "userQuery", userQuery
        );
    }

}
