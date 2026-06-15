package cn.haohaoli.cloud.ai.practice.rag;


import org.apache.commons.collections4.ListUtils;
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

    private static final String SOURCE_ID = "sample_large_cn_md";

    private static final int EMBEDDING_BATCH_SIZE = 10;

    private final List<Document> documents;

    private final RedisVectorStore vectorStore;

    public RagETL3Controller(RedisVectorStore vectorStore) {
        DocumentReader reader = new MarkdownDocumentReader(
                new ClassPathResource("data/sample-large-cn.md"),
                MarkdownDocumentReaderConfig.defaultConfig()
        );

        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(800)
                .withMinChunkSizeChars(350)
                .withMinChunkLengthToEmbed(5)
                .withMaxNumChunks(10000)
                .withKeepSeparator(true)
                .build();
        this.documents = splitter.apply(reader.read());
        for (Document document : documents) {
            Map<String, Object> metadata = document.getMetadata();
            metadata.put("source_id", SOURCE_ID);
            metadata.put("source_file", SOURCE_FILE);
        }
        this.vectorStore = vectorStore;
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
     * Writer 阶段：把切分后的 Document 写入 RedisVectorStore。
     * <p>
     * vectorStore.add() 会自动调用 EmbeddingModel，把每个 Document.text 转成向量后写入 Redis。
     */
    @GetMapping("/import")
    public int importData() {
        for (List<Document> documentList : ListUtils.partition(documents, EMBEDDING_BATCH_SIZE)) {
            vectorStore.add(documentList);
        }
        log.info("RAG ETL Writer 导入完成，sourceFile={}, count={}", SOURCE_FILE, documents.size());
        return documents.size();
    }

    /**
     * 验证刚才写入的知识库是否能被召回。
     */
    @GetMapping("/search")
    public List<Document> search(@RequestParam String query,
                                 @RequestParam(defaultValue = "3") int topK) {
        FilterExpressionBuilder builder = new FilterExpressionBuilder();
        Filter.Expression expression = builder.eq("source_id", SOURCE_ID).build();

        return vectorStore.similaritySearch(SearchRequest.builder()
                .query(query)
                .topK(Math.max(1, topK))
                .filterExpression(expression)
                .build());
    }


}
