package cn.haohaoli.cloud.ai.practice.rag;


import com.alibaba.cloud.ai.advisor.RetrievalRerankAdvisor;
import com.alibaba.cloud.ai.memory.redis.RedissonRedisChatMemoryRepository;
import com.alibaba.cloud.ai.model.RerankModel;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
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
public class RagAdvisor6Controller {

    private final ChatClient chatClient;

    private final MessageChatMemoryAdvisor messageChatMemoryAdvisor;
    private final RetrievalAugmentationAdvisor retrievalAugmentationAdvisor;
    private final RetrievalRerankAdvisor retrievalRerankAdvisor;

    public RagAdvisor6Controller(ChatClient.Builder builder,
                                 RedisVectorStore vectorStore,
                                 RedissonRedisChatMemoryRepository chatMemoryRepository,
                                 RerankModel rerankModel) {
        this.chatClient = builder.defaultAdvisors(SimpleLoggerAdvisor.builder().build()).build();

        this.messageChatMemoryAdvisor = MessageChatMemoryAdvisor
                .builder(
                        MessageWindowChatMemory.builder().chatMemoryRepository(chatMemoryRepository).build()
                )
                .build();
        this.retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(
                        VectorStoreDocumentRetriever.builder()
                                .topK(20)
                                .similarityThreshold(0.3)
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
                ).queryTransformers(
                        CompressionQueryTransformer.builder()
                                .chatClientBuilder(builder.build().mutate())
                                .build()
                ).build();

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
        this.retrievalRerankAdvisor = new RetrievalRerankAdvisor(vectorStore, rerankModel,
                SearchRequest.builder().topK(2).similarityThresholdAll().build(),
                promptTemplate,
                0.5);

    }

    @GetMapping("/rerank/chat")
    public Flux<String> search(HttpServletResponse response,
                               @RequestParam String query,
                               @RequestParam(value = "chatId", defaultValue = "default_session") String chatId) {
        response.setCharacterEncoding("UTF-8");

        return chatClient.prompt()
                .user(query)
                .advisors(messageChatMemoryAdvisor, retrievalAugmentationAdvisor, retrievalRerankAdvisor)
                .advisors(a -> a.param(MessageWindowChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content();
    }


}
