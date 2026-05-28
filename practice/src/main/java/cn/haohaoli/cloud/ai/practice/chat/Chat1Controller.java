package cn.haohaoli.cloud.ai.practice.chat;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * @author lwh
 */
@RestController
@RequestMapping("/chat")
public class Chat1Controller {

    private final ChatClient chatClient;

    // 通过 ChatClient.Builder 来创建 ChatClient
    public Chat1Controller(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    // 同步的调用方法（等大模型完成后一次性返回）
    // http://localhost:8080/chat/call?userQuery=你好呀
    @GetMapping("/call")
    public String simple(@RequestParam(value = "userQuery", defaultValue = "你好呀，请介绍下你自己", required = false) String userQuery) {
        return chatClient.prompt()
                .user(userQuery)
                .call()
                .content();
    }

    // 流式的调用方法SSE（大模型输出一点就返回一点给前端，类似打字机）
    // http://localhost:8080/chat/stream?userQuery=你好呀
    @GetMapping("/stream")
    public Flux<String> stream(HttpServletResponse response, @RequestParam(value = "userQuery", defaultValue = "你好呀，请介绍下你自己", required = false) String userQuery) {
        response.setCharacterEncoding("UTF-8");

        return chatClient.prompt()
                .user(userQuery)
                .stream()
                .content();
    }
}