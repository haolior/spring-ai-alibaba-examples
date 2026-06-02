package cn.haohaoli.cloud.ai.practice.rag;


import com.alibaba.cloud.ai.memory.redis.RedissonRedisChatMemoryRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * @author lwh
 */
@RestController
@RequestMapping("/rag/advisor")
public class RagAdvisor1Controller {

    private static final Logger log = LoggerFactory.getLogger(RagAdvisor1Controller.class);

    private final RedisVectorStore vectorStore;

    private final ChatClient chatClient;

    public RagAdvisor1Controller(ChatClient.Builder builder,
                                 RedisVectorStore vectorStore,
                                 RedissonRedisChatMemoryRepository chatMemoryRepository) {
        this.vectorStore = vectorStore;
        PromptTemplate promptTemplate = PromptTemplate.builder()
                .template(
                        """
                                {query}
                                
                                上下文信息如下。
                                
                                ---------------------
                                {question_answer_context}
                                ---------------------
                                
                                根据上下文信息，在没有先验知识的情况下回答问题。
                                
                                请遵循以下规则：
                                
                                1. 如果答案不在上下文中，请直接说不知道。
                                2. 避免使用“根据上下文...”或“根据提供的信息...”之类的表述。
                                """)
                .build();

        this.chatClient =
                builder.defaultSystem("你是一个友好且知识渊博的AI助手。基于提供的上下文信息来回答问题，如果上下文中没有相关信息，请明确告知用户。")
                        .defaultAdvisors(
                                QuestionAnswerAdvisor
                                        .builder(vectorStore)
                                        .searchRequest(SearchRequest.builder().build())
                                        .promptTemplate(promptTemplate)
                                        .build()
                        )
                        .defaultAdvisors(
                                SimpleLoggerAdvisor
                                        .builder()
                                        .build()
                        )
                        .defaultAdvisors(
                                MessageChatMemoryAdvisor
                                        .builder(MessageWindowChatMemory.builder().chatMemoryRepository(chatMemoryRepository).build())
                                        .build()
                        )
                        .build();

    }


    @GetMapping("/init")
    public void initData() {
        List<Document> documents1 = List.of(
                new Document("1. 使用SpringAIAlibaba创建一个Spring Boot项目，并添加spring-ai-alibaba-starter-dashscope依赖。"),
                new Document("2. 在SpringAIAlibaba项目的pom.xml中添加Spring Milestone和Snapshot存储库。"),
                new Document("3. 通过SpringAIAlibaba申请阿里云通义API Key，在application.yml中进行配置。"),
                new Document("4. 使用SpringAIAlibaba的ChatClient和Prompt功能实现对话模型。"),
                new Document("5. 通过SpringAIAlibaba的Spring Boot与Spring Cloud Alibaba AI对接，实现基本聊天功能。"),
                new Document("6. SpringAIAlibaba支持文本生成、翻译、摘要等生成式AI功能。"),
                new Document("7. SpringAIAlibaba支持文本数据的语义搜索和AI绘画功能。"),
                new Document("8. 使用SpringAIAlibaba的TongYiChatModel和TongYiImagesModel实现聊天和图片服务。"),
                new Document("9. 在SpringAIAlibaba的REST控制器中提供对外的API接口。"),
                new Document("10. 通过SpringAIAlibaba的简单API调用实现AI模型的集成。")
        );

        List<Document> documents2 = List.of(
                new Document("11. 使用SpringAIAlibaba的Prompt模板管理控制AI模型的输出。"),
                new Document("12. 结合SpringAIAlibaba的检索和生成技术（RAG）提高生成内容的质量。"),
                new Document("13. 使用SpringAIAlibaba实现文本生成图像和图像识别功能。"),
                new Document("14. 准备SpringAIAlibaba需要的Java 17及以上的开发环境。"),
                new Document("15. 使用IDEA进行SpringAIAlibaba的Java开发和HBuilder X进行前端开发。"),
                new Document("16. 在SpringAIAlibaba的Spring Boot项目中集成多种AI模型和向量数据库。"),
                new Document("17. SpringAIAlibaba支持自然语言处理、计算机视觉、语音处理和数据分析与预测功能。"),
                new Document("18. 通过SpringAIAlibaba的配置中心和注册中心实现动态扩展。")
        );

        vectorStore.add(documents1);
        vectorStore.add(documents2);
        log.info("Vector data initialized");
    }

    @GetMapping("/chat")
    public Flux<String> search(HttpServletResponse response,
                               @RequestParam String query,
                               @RequestParam(value = "chatId", defaultValue = "default_session") String chatId) {
        response.setCharacterEncoding("UTF-8");

        return chatClient.prompt()
                .user(query)
                .advisors(a -> a.param(MessageWindowChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content();
    }

    /**
     * 向量数据查询测试
     */
    @GetMapping("/search")
    public List<Document> search(@RequestParam String query) {
        return vectorStore.similaritySearch(
                SearchRequest.builder().query(query).build());
    }

}
