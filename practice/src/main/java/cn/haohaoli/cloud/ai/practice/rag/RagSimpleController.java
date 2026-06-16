package cn.haohaoli.cloud.ai.practice.rag;


import com.alibaba.cloud.ai.memory.redis.RedissonRedisChatMemoryRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * @author lwh
 */
@RestController
@RequestMapping("/rag")
public class RagSimpleController {

    private static final Logger log = LoggerFactory.getLogger(RagSimpleController.class);

    private static final String SOURCE_FILE = "sample-large-cn.md";

    private static final String SOURCE_ID = "simple";

    private static final int EMBEDDING_BATCH_SIZE = 10;

    private final ChatClient chatClient;

    private final MessageChatMemoryAdvisor messageChatMemoryAdvisor;

    private final RedisVectorStore vectorStore;

    private final List<Document> documents;

    public RagSimpleController(ChatClient.Builder builder,
                               RedissonRedisChatMemoryRepository chatMemoryRepository,
                               RedisVectorStore vectorStore) {
        this.chatClient = builder
                .defaultAdvisors(SimpleLoggerAdvisor.builder().build())
                .build();
        // 会话记忆
        this.messageChatMemoryAdvisor = MessageChatMemoryAdvisor
                .builder(
                        MessageWindowChatMemory.builder().chatMemoryRepository(chatMemoryRepository).build()
                )
                .build();
        // 向量数据库
        this.vectorStore = vectorStore;

        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(800)
                .withMinChunkSizeChars(350)
                .withMinChunkLengthToEmbed(5)
                .withMaxNumChunks(10000)
                .withKeepSeparator(true)
                .build();

        DocumentReader reader = new MarkdownDocumentReader(
                new ClassPathResource("data/sample-large-cn.md"),
                MarkdownDocumentReaderConfig.defaultConfig()
        );
        this.documents = splitter.apply(reader.read());
        for (Document document : documents) {
            Map<String, Object> metadata = document.getMetadata();
            metadata.put("source_id", SOURCE_ID);
            metadata.put("source_file", SOURCE_FILE);
        }

    }

    /**
     * 只执行入库。
     */
    @GetMapping("/import")
    public ImportResult importData() {
        for (List<Document> documentList : ListUtils.partition(documents, EMBEDDING_BATCH_SIZE)) {
            vectorStore.add(documentList);
        }
        log.info("简单 RAG 总结示例导入完成，sourceId={}, count={}", SOURCE_ID, documents.size());
        return new ImportResult(SOURCE_ID, documents.size());
    }

    /**
     * 预览即将入库的 Document。
     * <p>
     * 这个接口不写入向量库，只用于观察 Reader -> Splitter 后，最终准备交给 Writer 的数据长什么样。
     */
    @GetMapping("/preview")
    public List<Document> preview() {
        return documents.stream()
                .limit(3)
                .toList();
    }

    /**
     * 只执行检索，观察用户问题召回了哪些 chunk。
     */
    @GetMapping("/search")
    public List<Document> search(@RequestParam String query,
                                 @RequestParam(defaultValue = "2") int topK) {
        FilterExpressionBuilder builder = new FilterExpressionBuilder();
        Filter.Expression expression = builder.eq("source_id", SOURCE_ID).build();

        return vectorStore.similaritySearch(SearchRequest.builder()
                .query(query)
                .topK(Math.max(1, topK))
                .filterExpression(expression)
                .build());
    }

    /**
     * 基于已入库的知识库回答问题。
     */
    @GetMapping("/chat")
    public Flux<String> chat(HttpServletResponse response, @RequestParam String query,
                             @RequestParam(value = "chatId", defaultValue = "default_session") String chatId) {
        response.setCharacterEncoding("UTF-8");
        PromptTemplate contextualPromptTemplate = PromptTemplate.builder()
                .template(
                        """
                                下面是与你的问题相关的知识库上下文。

                                ---------------------
                                {context}
                                ---------------------

                                请在不使用任何外部知识的前提下，仅依据上述上下文回答问题。

                                请遵循以下规则：
                                1. 如果答案不在上下文中，请直接说不知道。
                                2. 不要补充、猜测或改写上下文中没有明确说明的规则、条件、原因或结论。
                                3. 回答要直接、简洁、自然，不要出现“根据上下文...”或“根据提供的信息...”之类的表述。

                                问题：{query}

                                回答：
                                """
                )
                .build();
        PromptTemplate emptyContextPromptTemplate = PromptTemplate.builder()
                .template(
                        """
                                当前没有检索到与你问题相关的知识库内容。

                                请直接回答：不知道。
                                """
                )
                .build();
        return chatClient.prompt()
                .user(query)
                .advisors(messageChatMemoryAdvisor)
                .advisors(a -> a.param(MessageWindowChatMemory.CONVERSATION_ID, chatId))
                .advisors(RetrievalAugmentationAdvisor.builder()
                        .documentRetriever(
                                VectorStoreDocumentRetriever.builder()
                                        .vectorStore(vectorStore)
                                        .topK(2)
                                        .filterExpression(new FilterExpressionBuilder().eq("source_id", SOURCE_ID).build())
                                        .build()
                        )
                        .queryAugmenter(
                                ContextualQueryAugmenter.builder()
                                        .promptTemplate(contextualPromptTemplate)
                                        .emptyContextPromptTemplate(emptyContextPromptTemplate)
                                        .allowEmptyContext(false)
                                        .build()
                        )
                        .build()
                )
                .system(
                        """
                                你是一个严谨的售后客服知识库助手。
                                请只基于 RAG 召回的上下文回答问题，不要使用外部知识补充规则。
                                如果答案不在上下文中，请直接说不知道。
                                避免使用“根据上下文...”或“根据提供的信息...”之类的表述。
                                """
                )
                .stream()
                .content();
    }

    public record ImportResult(String sourceId, int documentCount) {
    }

}
