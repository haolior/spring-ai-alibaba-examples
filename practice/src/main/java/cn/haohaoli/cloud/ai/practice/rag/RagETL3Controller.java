package cn.haohaoli.cloud.ai.practice.rag;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lwh
 */
@RestController
@RequestMapping("/rag/etl/writer")
public class RagETL3Controller {

    private static final Logger log = LoggerFactory.getLogger(RagETL3Controller.class);

    private static final String SOURCE_FILE = "sample-large-cn.md";

    private final RedisVectorStore vectorStore;

    public RagETL3Controller(RedisVectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * 预览即将入库的 Document。
     * <p>
     * 这个接口不写入向量库，只用于观察 Reader -> Splitter 后，最终准备交给 Writer 的数据长什么样。
     */
    @GetMapping("/preview")
    public List<Document> preview() {
        return buildDocuments().stream()
                .limit(3)
                .toList();
    }

    /**
     * Writer 阶段：把切分后的 Document 写入 RedisVectorStore。
     * <p>
     * vectorStore.add() 会自动调用 EmbeddingModel，把每个 Document.text 转成向量后写入 Redis。
     */
    @GetMapping("/import")
    public ImportResult importData() {
        List<Document> documents = buildDocuments();
        vectorStore.add(documents);
        log.info("RAG ETL Writer 导入完成，sourceFile={}, count={}", SOURCE_FILE, documents.size());
        return new ImportResult(SOURCE_FILE, documents.size());
    }

    /**
     * 验证刚才写入的知识库是否能被召回。
     */
    @GetMapping("/search")
    public List<Document> search(@RequestParam String query,
                                 @RequestParam(defaultValue = "3") int topK) {
        FilterExpressionBuilder builder = new FilterExpressionBuilder();
        Filter.Expression expression = builder.eq("source_file", SOURCE_FILE).build();

        return vectorStore.similaritySearch(SearchRequest.builder()
                .query(query)
                .topK(Math.max(1, topK))
                .filterExpression(expression)
                .build());
    }

    private List<Document> buildDocuments() {
        List<Document> splitDocuments = splitDocuments(readDocuments());
        List<Document> result = new ArrayList<>(splitDocuments.size());

        for (int i = 0; i < splitDocuments.size(); i++) {
            Document document = splitDocuments.get(i);
            Map<String, Object> metadata = new HashMap<>(document.getMetadata());
            metadata.put("source_file", SOURCE_FILE);
            metadata.put("etl_stage", "writer");
            metadata.put("chunk_no", i);

            // 使用稳定 ID，重复调用 import 时会覆盖同一批数据，避免 Redis 中不断堆重复 chunk。
            result.add(new Document("etl-" + SOURCE_FILE + "-" + i, document.getText(), metadata));
        }

        return result;
    }

    private List<Document> readDocuments() {
        DocumentReader reader = new MarkdownDocumentReader(
                new ClassPathResource("data/" + SOURCE_FILE),
                MarkdownDocumentReaderConfig.defaultConfig()
        );
        return reader.read();
    }

    private List<Document> splitDocuments(List<Document> documents) {
        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(800)
                .withMinChunkSizeChars(350)
                .withMinChunkLengthToEmbed(5)
                .withMaxNumChunks(10000)
                .withKeepSeparator(true)
                .build();
        return splitter.apply(documents);
    }

    public record ImportResult(String sourceFile, int documentCount) {
    }

}
