package cn.haohaoli.cloud.ai.practice.rag;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.DefaultContentFormatter;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.ai.model.transformer.SummaryMetadataEnricher;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.transformer.ContentFormatTransformer;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author lwh
 */
@RestController
@RequestMapping("/rag/etl/transformer")
public class RagETL2Controller {

    private static final Logger log = LoggerFactory.getLogger(RagETL2Controller.class);

    private final List<Document> documents;
    private final ChatModel chatModel;

    public RagETL2Controller(ChatModel chatModel) {
        DocumentReader reader = new MarkdownDocumentReader(
                new ClassPathResource("data/sample-large-cn.md"),
                MarkdownDocumentReaderConfig.defaultConfig()
        );
        this.documents = reader.read();
        this.chatModel = chatModel;
    }

    @GetMapping("/token-text-splitter")
    public List<Document> tokenTextSplitter() {
        return splitDocuments();
    }

    /**
     * 格式化 Document 正文。
     * <p>
     * 这个转换器不调用大模型，只负责按 ContentFormatter 的规则整理文本内容。它适合在入库前统一正文格式，
     * 尤其是 Markdown、表格、列表等来源混杂时，可以先观察格式化前后的 text 差异。
     */
    @GetMapping("/content-format-transformer")
    public List<Document> contentFormatTransformer() {
        DefaultContentFormatter formatter = DefaultContentFormatter.defaultConfig();
        ContentFormatTransformer transformer = new ContentFormatTransformer(formatter);
        return transformer.apply(splitDocuments());
    }

    /**
     * 提取关键词并写入 metadata。
     * <p>
     * 该转换器会调用大模型。第二个参数 3 表示希望为每个 chunk 提取 3 个关键词。
     * 关键词可用于调试召回、结果展示，也可以作为后续过滤或重排的辅助信息。
     */
    @GetMapping("/keyword-metadata-enricher")
    public List<Document> keywordMetadataEnricher() {
        KeywordMetadataEnricher enricher = new KeywordMetadataEnricher(chatModel,
                new PromptTemplate("""
                        请从以下文档内容中提取 3 个最能代表文档核心主题的关键词。
                        
                        要求：
                        1. 关键词应为名词或名词短语，长度控制在2-6个汉字
                        2. 按重要性从高到低排序
                        3. 排除停用词（如"这个"、"那个"、"然后"、"但是"等）
                        4. 优先提取专业术语、专有名词、核心概念
                        5. 输出格式：仅输出关键词，用英文逗号分隔，不要有其他解释或标点
                        
                        文档内容：
                        {context_str}
                        
                        关键词：
                        """)
        );
        return enricher.apply(splitDocuments());
    }

    /**
     * 生成摘要并写入 metadata。
     * <p>
     * 该转换器会调用大模型。CURRENT 表示当前 chunk 摘要，PREVIOUS/NEXT 表示相邻 chunk 摘要。
     * 摘要可以补充上下文，但成本比关键词更高，实际项目里通常只对重点知识库或离线任务使用。
     */
    @GetMapping("/summary-metadata-enricher")
    public List<Document> summaryMetadataEnricher() {
        List<SummaryMetadataEnricher.SummaryType> summaryTypes = List.of(
                SummaryMetadataEnricher.SummaryType.CURRENT,
                SummaryMetadataEnricher.SummaryType.PREVIOUS,
                SummaryMetadataEnricher.SummaryType.NEXT
        );
        String template = """
                请对以下文档内容生成一段简洁的摘要。
                
                要求：
                1. 摘要长度控制在 100-200 字之间
                2. 覆盖文档的核心主题、关键实体和主要结论
                3. 使用通顺的中文，不要使用列表或序号
                4. 只输出摘要内容，不要添加任何额外说明
                
                文档内容：
                {context_str}
                
                摘要：
                """;
        SummaryMetadataEnricher enricher = new SummaryMetadataEnricher(chatModel, summaryTypes, template, MetadataMode.ALL);
        return enricher.apply(splitDocuments());
    }

    private List<Document> splitDocuments() {
        TokenTextSplitter splitter = TokenTextSplitter.builder()
                // 每个 chunk 的目标 token 数。不是按字符硬切，而是尽量让每块接近 800 token。
                // 值越小，召回越精准但上下文越少；值越大，上下文更多但语义更容易变杂。
                .withChunkSize(800)
                // 切分过程中尽量避免生成过短的 chunk。它更偏向“切分/合并策略”，不是简单的小于 350 字符就直接丢弃。
                .withMinChunkSizeChars(350)
                // 最终可进入 embedding 的最小片段长度。太短的碎片没有检索价值，例如“结束”“---”这类内容。
                .withMinChunkLengthToEmbed(5)
                // 单篇文档最多生成多少个 chunk，用来防止超大文件切出过多片段导致内存或向量库压力过大。
                .withMaxNumChunks(10000)
                // 保留分隔符（如换行、段落边界）。对 Markdown、列表、代码块等结构化内容更友好。
                .withKeepSeparator(true)
                .build();
        return splitter.apply(documents);
    }

}
