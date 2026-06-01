package cn.haohaoli.cloud.ai.practice.chat;

import com.alibaba.cloud.ai.memory.jdbc.JdbcChatMemoryRepository;
import com.alibaba.cloud.ai.memory.jdbc.PostgresChatMemoryRepository;
import com.alibaba.cloud.ai.memory.redis.RedissonRedisChatMemoryRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
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

    private final static int MAX_MESSAGE = 100;

    private final ChatClient chatClient;
    private final ChatMemoryRepository redisChatMemoryRepository;
    private final ChatMemoryRepository inMemoryChatMemoryRepository;
    private final ChatMemoryRepository jdbcChatMemoryRepository;

    public Chat4Controller(ChatClient.Builder chatClientBuilder,
                           RedissonRedisChatMemoryRepository redisChatMemoryRepository,
                           PostgresChatMemoryRepository postgresChatMemoryRepository) {

        this.chatClient = chatClientBuilder
                .defaultAdvisors(
                        SimpleLoggerAdvisor.builder().build()
                )
                .build();
        this.redisChatMemoryRepository = redisChatMemoryRepository;
        this.jdbcChatMemoryRepository = postgresChatMemoryRepository;
        this.inMemoryChatMemoryRepository = new InMemoryChatMemoryRepository();

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
                .advisors(
                        MessageChatMemoryAdvisor.builder(
                                MessageWindowChatMemory.builder()
                                        .maxMessages(MAX_MESSAGE)
                                        .chatMemoryRepository(inMemoryChatMemoryRepository)
                                        .build()
                        ).build()
                )
                .advisors(a -> a.param(MessageWindowChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content();
    }

    @GetMapping("/memory/redis")
    public Flux<String> redisMemoryChat(
            HttpServletResponse response,
            @RequestParam(value = "chatId", defaultValue = "default_session") String chatId,
            @RequestParam(value = "userQuery") String userQuery) {

        response.setCharacterEncoding("UTF-8");

        return chatClient.prompt()
                .user(userQuery)
                .advisors(
                        MessageChatMemoryAdvisor.builder(
                                MessageWindowChatMemory.builder()
                                        .maxMessages(MAX_MESSAGE)
                                        .chatMemoryRepository(redisChatMemoryRepository)
                                        .build()
                        ).build()
                )
                .advisors(a -> a.param(MessageWindowChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content();
    }

    @GetMapping("/memory/jdbc")
    public Flux<String> jdbcMemoryChat(
            HttpServletResponse response,
            @RequestParam(value = "chatId", defaultValue = "default_session") String chatId,
            @RequestParam(value = "userQuery") String userQuery) {

        response.setCharacterEncoding("UTF-8");

        return chatClient.prompt()
                .user(userQuery)
                .advisors(
                        MessageChatMemoryAdvisor.builder(
                                MessageWindowChatMemory.builder()
                                        .maxMessages(MAX_MESSAGE)
                                        .chatMemoryRepository(jdbcChatMemoryRepository)
                                        .build()
                        ).build()
                )
                .advisors(a -> a.param(MessageWindowChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content();
    }
}