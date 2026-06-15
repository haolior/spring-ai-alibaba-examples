package cn.haohaoli.cloud.ai.practice.rag;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.JsonReader;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.jsoup.JsoupDocumentReader;
import org.springframework.ai.reader.jsoup.config.JsoupDocumentReaderConfig;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.ParagraphPdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * @author lwh
 */
@RestController
@RequestMapping("/rag/etl/reader")
public class RagETL1Controller {

    private static final Logger log = LoggerFactory.getLogger(RagETL1Controller.class);

    @GetMapping("/json")
    public List<Document> json() {
        JsonReader reader = new JsonReader(
                new ClassPathResource("data/sample.json"),
                jsonMap -> Map.of("filename", "sample.json"),  //为每个 Document 创建元数据
                "description" // JSON 中的键数组，应将其用作生成 Document 对象中的文本内容
        );
        // return reader.get("/0"); // 获取第一个
        return reader.get();
    }

    @GetMapping("/text")
    public List<Document> text() {
        TextReader reader = new TextReader(new ClassPathResource("data/sample.txt"));
        reader.setCharset(StandardCharsets.UTF_8);
        reader.getCustomMetadata().put("filename", "sample.txt"); // 返回一个可变映射，为文档添加自定义元数据
        return reader.get();
    }


    @GetMapping("/html")
    public List<Document> html() {
        JsoupDocumentReaderConfig config = JsoupDocumentReaderConfig.builder()
                .charset("UTF-8")                                   // 指定 HTML 文档的字符编码（默认为 "UTF-8"）
                .selector("body")                                   // 一个 JSoup CSS 选择器，用于指定从哪些元素中提取文本（默认为 "body"）
                .separator("\n")                                    // 用于连接来自多个选定元素的文本的字符串（默认为 "\n"）
                .allElements(false)                                 // 如果为 true，则从 <body> 元素中提取所有文本，忽略 selector（默认为 false）
                .groupByElement(false)                              // 如果为 true，则为 selector 匹配的每个元素创建一个单独的 Document（默认为 false）
                .includeLinkUrls(true)                              // 如果为 true，则提取绝对链接 URL 并将其添加到元数据中（默认为 false）
                .metadataTags(List.of("generator"))             // 要提取内容的 <meta> 标签名称列表（默认为 ["description", "keywords"]）
                .additionalMetadata("filename", "sample.html")      // 允许您向所有创建的 Document 对象添加自定义元数据
                .build();

        JsoupDocumentReader reader = new JsoupDocumentReader(new ClassPathResource("data/sample.html"), config);
        return reader.read();
    }

    @GetMapping("/markdown")
    public List<Document> markdown() {
        MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                .withHorizontalRuleCreateDocument(false)            // 设置为 true 时，Markdown 中的水平线将创建新的 Document 对象（默认为 false）
                .withIncludeCodeBlock(false)                        // 设置为 true 时，代码块将包含在与周围文本相同的 Document 中。设置为 false 时，代码块会创建单独的 Document 对象（默认为 false）
                .withIncludeBlockquote(false)                       // 设置为 true 时，引用块将包含在与周围文本相同的 Document 中。设置为 false 时，引用块会创建单独的 Document 对象（默认为 false）
                .withAdditionalMetadata("filename", "sample.md")    // 添加自定义元数据
                .build();
        MarkdownDocumentReader reader = new MarkdownDocumentReader(new ClassPathResource("data/sample.md"), config);
        return reader.read();
    }

    @GetMapping("/pdf-page")
    public List<Document> pdfPage() {
        PdfDocumentReaderConfig config = PdfDocumentReaderConfig.builder()
                .withPagesPerDocument(1)                                            // 每个 Document 对象包含的页数（默认为 1）
                .withPageTopMargin(0)                                               // 页面顶部忽略的行数。用于跳过每页顶部重复出现的页眉等冗余内容（默认为 0）
                .withPageBottomMargin(0)                                            // 页面底部忽略的行数。用于跳过每页底部重复出现的页码、版权信息等冗余内容;（默认为 0）
                .withReversedParagraphPosition(false)                               // 是否反转段落位置顺序。true 时段落从下到上排列，极少使用；（默认为 false）
                .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()    // 设置文本格式化器
                        .withLeftAlignment(false)                           // 是否将文本左对齐。true 时文本左对齐（会移除前导空格），false 时保持原始布局；（默认为 false）
                        .withNumberOfTopPagesToSkipBeforeDelete(0)          // 在执行删除顶部/底部行操作时，跳过前 N 页。即前 N 页不应用删除行的规则，从第 N+1 页开始才删除
                        .withNumberOfTopTextLinesToDelete(0)                // 从每页文本顶部删除的行数。用于精确移除每页开头的固定行数
                        .withNumberOfBottomTextLinesToDelete(0)             // 从每页文本底部删除的行数。用于精确移除每页末尾的固定行数
                        .overrideLineSeparator(System.lineSeparator())      // 指定文本中的换行符。可以覆盖系统默认换行符，统一使用指定的分隔符（如 \n 或 \r\n）
                        .build())
                .build();
        PagePdfDocumentReader reader = new PagePdfDocumentReader(new ClassPathResource("data/sample-pdf-page.pdf"), config);
        return reader.read();
    }

    @GetMapping("/pdf-paragraph")
    public List<Document> pdfParagraph() {
        PdfDocumentReaderConfig config = PdfDocumentReaderConfig.defaultConfig();
        ParagraphPdfDocumentReader pdfReader = new ParagraphPdfDocumentReader("data/sample-pdf-paragraph.pdf", config);
        return pdfReader.read();
    }


    @GetMapping("/tika")
    public List<Document> tika() {
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(new ClassPathResource("data/sample-tika.html")); // 可以传多种文档格式
        return tikaDocumentReader.read();
    }
}
