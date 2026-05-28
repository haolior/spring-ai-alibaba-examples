package cn.haohaoli.cloud.ai.practice.chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author lwh
 */
@RestController
@RequestMapping("/chat")
public class Chat5Controller {

    private final ChatClient chatClient;

    public Chat5Controller(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    /**
     * 场景一：非结构化大白话文本 -> 单个 Java Record 对象映射
     * <p>
     * 测试 URL：
     * http://localhost:8080/chat/struct/bean?userQuery
     */
    @GetMapping("/struct/bean")
    public ActorRecord extractActor(@RequestParam(value = "userQuery",defaultValue = "姓名，性别，年龄，学历; 按照这个格式帮我生成演员信息") String userQuery) {
        return chatClient.prompt()
                .user(userQuery)
                .call()
                .entity(ActorRecord.class);
    }

    /**
     * 场景二：批量非结构化数据生成 -> 复杂泛型 List 集合映射
     * <p>
     * 测试 URL：
     * http://localhost:8080/chat/struct/bean-list
     */
    @GetMapping("/struct/bean-list")
    public List<ActorRecord> extractActorList(@RequestParam(value = "userQuery",defaultValue = "姓名，性别，年龄，学历; 按照这个格式帮我生成5条演员信息") String userQuery) {
        return chatClient.prompt()
                .user(userQuery)
                .call()
                .entity(new ParameterizedTypeReference<List<ActorRecord>>() {});
    }

    public record ActorRecord(String name, String gender, Integer age, List<String> degrees) {
    }
}