package cn.haohaoli.cloud.ai.practice.chat;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
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
public class Chat4Controller {

    private final ChatClient chatClient;

    public Chat4Controller(ChatClient.Builder chatClientBuilder) {
        // 1. 实例化“滑动窗口”记忆组件
        // MessageWindowChatMemory 会在内存中开辟空间，并默认限制/滑动保留最近的 N 条消息，防止上下文撑爆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(100)
                .build();

        // 2. 将“记忆顾问 (MessageChatMemoryAdvisor)”作为默认切面注入 ChatClient
        // Advisor 核心哲学：类似于 Spring AOP 的 Around 环绕通知。
        // - 请求发出前：它会自动拦截请求，去 chatMemory 捞出该用户的历史对话拼入 Prompt
        // - 大模型回复后：它会自动拦截响应，将 AI 的回复（Assistant 消息）存入 chatMemory 归档
        this.chatClient = chatClientBuilder
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
    }

    /**
     * 有记忆的连续对话接口
     * <p>
     * 对照实验步骤：
     * 1. GET http://localhost:8080/chat/memory?chatId=111&userQuery=我是马斯克 -> AI 会礼貌问好
     * 2. GET http://localhost:8080/chat/memory?chatId=111&userQuery=我叫什么名字？ -> AI 能正确回答（有记忆）
     * 3. GET http://localhost:8080/chat/memory?chatId=222&userQuery=我叫什么名字？ -> AI 提示不知道（会话隔离）
     *
     * @param chatId    会话隔离ID（用于区分不同的用户或不同的聊天窗口）
     * @param userQuery 用户当前输入的提示词
     */
    @GetMapping("/memory")
    public Flux<String> memoryChat(
            HttpServletResponse response,
            @RequestParam(value = "chatId", defaultValue = "default_session") String chatId,
            @RequestParam(value = "userQuery") String userQuery) {


        response.setCharacterEncoding("UTF-8");

        // 3. ⭐️ 核心暗号对接：利用 Lambda 向底层的 Advisor（拦截器）传递当前请求的上下文参数
        // a -> a.param(...) 的本质是向参数传送带塞入一个键值对：
        // Key  : MessageWindowChatMemory.CONVERSATION_ID（底层的固定暗号，字符串为 chat_memory_conversation_id）
        // Value: 外部传入的动态 chatId（如 111 或 222）
        // 作用 : 告诉底层的记忆顾问：“当前这次请求是专属 '${chatId}' 的，帮我捞取和存储历史记录时千万别认错人！”
        return chatClient.prompt()
                .user(userQuery)
                .advisors(a -> a.param(MessageWindowChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content();
    }
}