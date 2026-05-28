package cn.haohaoli.cloud.ai.practice.chat;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 *
 * @author lwh
 */
@RestController
@RequestMapping("/chat")
public class Chat2Controller {

    private final ChatClient chatClient;

    public Chat2Controller(ChatClient.Builder chatClientBuilder) {
        // 1. 出厂设置：全局默认人设（风趣助手）
        this.chatClient = chatClientBuilder
                .defaultSystem("你是一个智能聊天助手，每次回答都要富有风趣！")
                .build();
    }

    /**
     * 体验：全局默认人设（风趣助手）
     * 访问路径：http://localhost:8080/chat/role/default
     */
    @GetMapping("/role/default")
    public Flux<String> defaultRole(
            HttpServletResponse response,
            @RequestParam(value = "userQuery", defaultValue = "你好呀，请介绍下你自己", required = false) String userQuery) {

        response.setCharacterEncoding("UTF-8");

        return chatClient.prompt()
                .user(userQuery)
                .stream()
                .content();
    }

    /**
     * 体验：局部覆盖人设（临时变身暴躁程序员）
     * 访问路径：http://localhost:8080/chat/role/override
     */
    @GetMapping("/role/override")
    public Flux<String> overrideRole(
            HttpServletResponse response,
            @RequestParam(value = "userQuery", defaultValue = "你好呀，请介绍下你自己", required = false) String userQuery) {

        response.setCharacterEncoding("UTF-8");

        return chatClient.prompt()
                .system("你是一个暴躁的程序员！")
                .user(userQuery)
                .stream()
                .content();
    }
}