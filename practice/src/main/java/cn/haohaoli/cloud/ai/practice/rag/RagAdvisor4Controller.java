package cn.haohaoli.cloud.ai.practice.rag;


import com.alibaba.cloud.ai.memory.redis.RedissonRedisChatMemoryRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * @author lwh
 */
@RestController
@RequestMapping("/rag/advisor")
public class RagAdvisor4Controller {

    private final ChatClient chatClient;

    private final MessageChatMemoryAdvisor messageChatMemoryAdvisor;
    private final RetrievalAugmentationAdvisor retrievalAugmentationAdvisor;

    public RagAdvisor4Controller(ChatClient.Builder builder,
                                 RedisVectorStore vectorStore,
                                 RedissonRedisChatMemoryRepository chatMemoryRepository) {
        this.chatClient = builder.defaultAdvisors(SimpleLoggerAdvisor.builder().build()).build();

        this.messageChatMemoryAdvisor = MessageChatMemoryAdvisor
                .builder(
                        MessageWindowChatMemory.builder().chatMemoryRepository(chatMemoryRepository).build()
                )
                .build();
        this.retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(
                        VectorStoreDocumentRetriever.builder()
                                .vectorStore(vectorStore)
                                .build()
                )
                .queryTransformers(TranslationQueryTransformer.builder()
                        .chatClientBuilder(builder.build().mutate())
                        .targetLanguage("chinese")
                        .build())
                .queryTransformers(
                        RewriteQueryTransformer.builder()
                                .chatClientBuilder(builder.build().mutate())
                                .targetSearchSystem("vector store")
                                .build()
                )
                .build();


    }

    @GetMapping("/trans/chat")
    public Flux<String> search(HttpServletResponse response,
                               @RequestParam String query,
                               @RequestParam(value = "chatId", defaultValue = "default_session") String chatId) {
        response.setCharacterEncoding("UTF-8");

        return chatClient.prompt()
                .user(query)
                .advisors(messageChatMemoryAdvisor, retrievalAugmentationAdvisor)
                .advisors(a -> a.param(MessageWindowChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content();
    }


}
