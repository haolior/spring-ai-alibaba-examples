package cn.haohaoli.cloud.ai.practice.chat;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * 第二阶段：融入后端业务
 * 模块：企业级提示词工程（Prompt Engineering - 混合编排形态）
 * 核心看点：
 * 1. 资源解耦：利用 @Value 将核心“系统人设”剥离至外部文本文件，实现人设配置化。
 * 2. 角色隔离：严格区分 SYSTEM（系统圣旨）与 USER（用户业务）角色，发挥大模型最佳指令遵从度。
 * 3. 动态渲染：同时演练了基于 Builder 的系统模板渲染与基于内存的局部用户模板渲染。
 *
 * @author lwh
 */
@RestController
@RequestMapping("/chat")
public class Chat3Controller {

    private final ChatClient chatClient;

    // 自动注入类路径下的提示词模板文件，将文本与 Java 代码彻底解耦
    @Value("classpath:/prompts/system.txt")
    private Resource resource;

    public Chat3Controller(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * 混合编排提示词接口（流式返回）
     * <p>
     * 正确的测试 URL（注意参数间用 & 连接）：
     * http://localhost:8080/chat/prompt?actor=周杰伦&year=2007&name=杰西卡&voice=暴躁
     *
     * @param name  AI 的虚拟名字（注入系统人设）
     * @param voice AI 的说话语气（注入系统人设）
     * @param actor 明星名字（注入用户提问）
     * @param year  年份（注入用户提问）
     */
    @GetMapping("/prompt")
    public Flux<String> concertQuery(
            HttpServletResponse response,
            @RequestParam(value = "name", required = false, defaultValue = "鲍勃") String name,
            @RequestParam(value = "voice", required = false, defaultValue = "幽默") String voice,
            @RequestParam(value = "actor", defaultValue = "周杰伦", required = false) String actor,
            @RequestParam(value = "year", defaultValue = "2007", required = false) String year) {

        // 确保流式输出到浏览器或前端时中文不乱码
        response.setCharacterEncoding("UTF-8");

        // 1. 编排 SYSTEM 角色消息（系统圣旨层）：负责框定 AI 的灵魂、人设和底层行为准则
        // 使用最新的 Builder 模式加载外部 Resource 资源，并动态填充人设变量
        SystemPromptTemplate systemPromptTemplate = SystemPromptTemplate.builder().resource(resource).build();
        Message systemMessage = systemPromptTemplate.createMessage(Map.of("name", name, "voice", voice));

        // 2. 编排 USER 角色消息（业务数据层）：负责存放用户当前具体的任务请求
        // 采用方法内局部模板，保持局部业务代码的敏捷与清爽
        String templateStr = "请问在 {year} 年，{actor} 举办过哪些演唱会？请列出主要的几场，并保持回答简短。";
        PromptTemplate promptTemplate = new PromptTemplate(templateStr);

        // 调用 .createMessage() 仅生成消息零件（UserMessage），而不直接包装成 Prompt 箱子
        Message userMessage = promptTemplate.createMessage(Map.of("year", year, "actor", actor));

        // 3. 核心总装：将分属不同角色的 Message 零件打包装进完全体的 Prompt 容器中发给大模型
        // 大模型底层接收到的是一个有层次的消息队列：[SYSTEM 消息, USER 消息]
        return chatClient.prompt(new Prompt(List.of(systemMessage, userMessage)))
                .stream()
                .content();
    }
}