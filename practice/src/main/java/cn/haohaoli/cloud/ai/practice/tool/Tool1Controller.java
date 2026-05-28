package cn.haohaoli.cloud.ai.practice.tool;

import cn.haohaoli.cloud.ai.practice.tool.config.DateTimeTools;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * @author lwh
 */
@RestController
@RequestMapping("/time")
public class Tool1Controller {

    private final ChatClient chatClient;
    private final DateTimeTools dateTimeTools;

    public Tool1Controller(ChatClient.Builder chatClientBuilder, DateTimeTools dateTimeTools) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
        this.dateTimeTools = dateTimeTools;
    }

    /**
     * 1. 基础流式对话接口
     * * @return Flux<String> 逐字返回大模型的文本响应流
     * @see MediaType#TEXT_EVENT_STREAM_VALUE 启用标准 SSE 协议，前端可实现“打字机”蹦字效果，自动对齐 UTF-8 编码
     */
    @GetMapping("/chat")
    public Flux<String> chat(HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        return chatClient.prompt()
                .user("现在几点钟了")
                .stream()
                .content();
    }

    /**
     * 2. 挂载时间自定义工具的流式对话接口（包含上下文数据传递）
     * * @return Flux<String> 因为工具设置了 returnDirect=true，此处流返回的实际上是工具执行完的原始时间字符串，大模型不参与二次加工润色
     */
    @GetMapping("/chat/tool")
    public Flux<String> chatTool(HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        return chatClient.prompt()
                .user("现在几点钟了")
                .tools(dateTimeTools)
                .toolContext(Map.of("username", "马斯克"))
                .stream()
                .content();
    }

}