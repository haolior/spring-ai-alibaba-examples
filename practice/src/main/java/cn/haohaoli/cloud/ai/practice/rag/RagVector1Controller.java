package cn.haohaoli.cloud.ai.practice.rag;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lwh
 */
@RestController
@RequestMapping("/rag/vector/simple")
public class RagVector1Controller {

    private static final Logger log = LoggerFactory.getLogger(RagVector1Controller.class);

    private final SimpleVectorStore simpleVectorStore;

    // 本地持久化 JSON 快照文件的存储绝对路径
    private final String DATA_PATH = System.getProperty("user.dir") + "/practice/data/save.json";

    public RagVector1Controller(EmbeddingModel embeddingModel) {
        this.simpleVectorStore = SimpleVectorStore
                .builder(embeddingModel)
                .build();
    }

    @GetMapping("/simple/add")
    public void importData() {
        log.info("开始执行：向内存向量库添加/更新文档数据");

        // 1. 初始化业务元数据 (Metadata Map)，用于传统 SQL 式的精确条件检索与隔离
        HashMap<String, Object> map = new HashMap<>();
        map.put("year", 2025);
        map.put("name", "liwenhao");

        // 2. 组装 Document 集合（等同于关系型数据库中的 Rows 数据行）
        List<Document> documents = List.of(
                // 仅传正文：系统会自动生成一个随机 UUID 作为该条记录的主键 ID
                new Document("世界很大，救赎就在不远处"),

                // 传正文 + 快捷元数据：通过 Map.of 绑定业务标签
                new Document("你向前走时面向过去，转身面向未来。", Map.of("year", 2024)),

                // 传正文 + 复杂元数据：绑定上面定义的 map 对象
                new Document("Spring AI太棒了！！Spring AI太棒了！！Spring AI太棒了！！Spring AI太棒了！！Spring AI太棒了！！", map),

                // 🌟 强行指定主键 ID："1"。后续如果再次 add 相同 ID 的 Document，则会触发旧数据覆盖更新
                new Document("1", "测试内容", map)
        );

        // 3. 提交给向量库：底层会自动调用远程 Embedding 接口，将每条记录转化为 1536 维浮点数坐标并存入内存 Map
        simpleVectorStore.add(documents);
        log.info("数据添加成功，已自动完成远程 Embedding 语义向量化处理。");
    }

    @GetMapping("/delete")
    public void delete() {
        log.info("开始执行：根据 ID 物理删除向量数据");
        // 传入主键 ID 集合，精准删除主键为 "1" 的记录
        simpleVectorStore.delete(List.of("1"));
        log.info("ID 为 1 的数据已成功从内存抹除");
    }

    @GetMapping("/search")
    public List<Document> search(@RequestParam String query) {
        log.info("开始执行：纯语义相似度匹配检索");

        return simpleVectorStore.similaritySearch(SearchRequest
                .builder()
                .query(query) // 用户的提问文本。底层会将此词向量化后去比对
                .topK(2)         // 类似 SQL 的 Limit 2，只捞取相关度最高的前 2 条数据
                .build());
    }


    @GetMapping("/search-filter")
    public List<Document> searchFilter() {
        log.info("开始执行：带元数据标签精准过滤的混合相似度检索");

        // 1. 采用类似 MyBatis-Plus 的条件构造器（QueryWrapper）构建传统的精确过滤规则
        FilterExpressionBuilder b = new FilterExpressionBuilder();
        Filter.Expression expression = b.and(
                b.in("year", 2025, 2024), // 强行要求元数据中 year 必须是 2024 或 2025
                b.eq("name", "liwenhao")     // 强行要求元数据中 name 必须等于 "liwenhao"
        ).build();

        // 2. 执行检索：只有满足 query("Spring") 语义最接近，且符合上面元数据硬性卡死条件的数据，才会被返回
        return simpleVectorStore.similaritySearch(SearchRequest
                .builder()
                .query("Spring")
                .topK(2)
                .filterExpression(expression) // 将元数据过滤条件拍在脸上，作为第一道闸机
                .build());
    }

    @GetMapping("/save")
    public void save() {
        log.info("开始执行：将内存向量数据序列化并导出到本地硬盘: {}", DATA_PATH);
        File file = new File(DATA_PATH);
        // 如果之前存在旧快照，直接删除，确保存档数据最新且无冲突
        if (file.exists()) {
            file.delete();
        }
        // 执行盘落盘序列化操作
        simpleVectorStore.save(file);
        log.info("本地快照持久化文件保存成功！");
    }


    @GetMapping("/load")
    public void load() {
        log.info("开始执行：从本地硬盘加载 JSON 快照数据还原至内存: {}", DATA_PATH);
        File file = new File(DATA_PATH);
        // 执行反序列化，重新填满 JVM 的 ConcurrentHashMap
        simpleVectorStore.load(file);
        log.info("向量数据读档成功，内存状态已恢复。");
    }


}
