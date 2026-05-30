/*
 * Copyright 2024-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.ai.example.vector.simple.controller;

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
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Alibaba Cloud AI Team
 * @description 基于内存/单机本地文件的向量数据库(SimpleVectorStore)基础演示
 * 本质是一个维护在JVM内存中的 ConcurrentHashMap，适合本地开发、轻量级小数据量测试。
 */
@RestController
@RequestMapping("/simple")
public class SimpleController {

    private static final Logger logger = LoggerFactory.getLogger(SimpleController.class);

    // Spring AI 提供的简易内存向量库组件
    private final SimpleVectorStore simpleVectorStore;

    // 本地持久化 JSON 快照文件的存储绝对路径
    private final String SAVE_PATH = System.getProperty("user.dir")
            + "/spring-ai-alibaba-rag-example"
            + "/spring-ai-alibaba-vector-databases-example/vector-simple-example/src/main/resources/save.json";

    /**
     * 构造函数注入 EmbeddingModel (向量化模型)
     * 内存库本身不具备计算文本语义坐标的能力，必须绑定一个“翻译官”模型，在数据进出时打配合。
     */
    public SimpleController(EmbeddingModel embeddingModel) {
        this.simpleVectorStore = SimpleVectorStore
                .builder(embeddingModel).build();
    }

    /**
     * 🛠️ 接口一：添加/修改数据 (Create / Update)
     * 底层逻辑是 Upsert (覆盖更新)：如果 ID 存在则覆盖老数据并重算向量，不存在则作为新记录插入。
     */
    @GetMapping("/add")
    public void importData() {
        logger.info("开始执行：向内存向量库添加/更新文档数据");

        // 1. 初始化业务元数据 (Metadata Map)，用于传统 SQL 式的精确条件检索与隔离
        HashMap<String, Object> map = new HashMap<>();
        map.put("year", 2025);
        map.put("name", "yingzi");

        // 2. 组装 Document 集合（等同于关系型数据库中的 Rows 数据行）
        List<Document> documents = List.of(
                // 仅传正文：系统会自动生成一个随机 UUID 作为该条记录的主键 ID
                new Document("The World is Big and Salvation Lurks Around the Corner"),

                // 传正文 + 快捷元数据：通过 Map.of 绑定业务标签
                new Document("You walk forward facing the past and you turn back toward the future.", Map.of("year", 2024)),

                // 传正文 + 复杂元数据：绑定上面定义的 map 对象
                new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", map),

                // 🌟 强行指定主键 ID："1"。后续如果再次 add 相同 ID 的 Document，则会触发旧数据覆盖更新
                new Document("1", "test content", map)
        );

        // 3. 提交给向量库：底层会自动调用远程 Embedding 接口，将每条记录转化为 1536 维浮点数坐标并存入内存 Map
        simpleVectorStore.add(documents);
        logger.info("数据添加成功，已自动完成远程 Embedding 语义向量化处理。");
    }

    /**
     * ❌ 接口二：删除数据 (Delete)
     * 根据文档的唯一主键 ID，从内存 Map 中物理抹除该条向量记录
     */
    @GetMapping("/delete")
    public void delete() {
        logger.info("开始执行：根据 ID 物理删除向量数据");
        // 传入主键 ID 集合，精准删除主键为 "1" 的记录
        simpleVectorStore.delete(List.of("1"));
        logger.info("ID 为 1 的数据已成功从内存抹除");
    }

    /**
     * 💾 接口三：数据存档快照 (Save to Disk)
     * 将当前 JVM 内存中所有序列化好的向量、文本和 Metadata 整体打包，持久化写入本地 JSON 文件中。
     * 目的：防止服务重启后内存数据丢失，避免重复调用百炼接口、耗费网络 I/O 与 Token。
     */
    @GetMapping("/save")
    public void save() {
        logger.info("开始执行：将内存向量数据序列化并导出到本地硬盘: {}", SAVE_PATH);
        File file = new File(SAVE_PATH);
        // 如果之前存在旧快照，直接删除，确保存档数据最新且无冲突
        if (file.exists()) {
            file.delete();
        }
        // 执行盘落盘序列化操作
        simpleVectorStore.save(file);
        logger.info("本地快照持久化文件保存成功！");
    }

    /**
     * 🔄 接口四：数据读档复活 (Load from Disk)
     * 在服务重启、内存为空时，从本地 JSON 快照文件中反序列化数据，一键将数据灌回 JVM 内存。
     * 优势：全本地操作，完全不消耗任何远程大模型 Token，瞬间复活业务知识库。
     */
    @GetMapping("/load")
    public void load() {
        logger.info("开始执行：从本地硬盘加载 JSON 快照数据还原至内存: {}", SAVE_PATH);
        File file = new File(SAVE_PATH);
        // 执行反序列化，重新填满 JVM 的 ConcurrentHashMap
        simpleVectorStore.load(file);
        logger.info("向量数据读档成功，内存状态已恢复。");
    }

    /**
     * 🔍 接口五：纯语义相似度搜索 (Vector Similarity Search)
     * 经典的 RAG 小抄捞取动作。纯靠数学距离（如余弦相似度）来计算语义最贴合的文本。
     */
    @GetMapping("/search")
    public List<Document> search() {
        logger.info("开始执行：纯语义相似度匹配检索");

        return simpleVectorStore.similaritySearch(SearchRequest
                .builder()
                .query("Spring") // 用户的提问文本。底层会将此词向量化后去比对
                .topK(2)         // 类似 SQL 的 Limit 2，只捞取相关度最高的前 2 条数据
                .build());
    }

    /**
     * 🎯 接口六：混合检索与过滤 (Hybrid Search with Metadata Filter)
     * 企业级 RAG 绝对不翻车的核心技术。
     * 将“大模型模糊语义理解（向量）”与“传统后端确定性规则（SQL/Metadata）”进行双轨并轨查询。
     */
    @GetMapping("/search-filter")
    public List<Document> searchFilter() {
        logger.info("开始执行：带元数据标签精准过滤的混合相似度检索");

        // 1. 采用类似 MyBatis-Plus 的条件构造器（QueryWrapper）构建传统的精确过滤规则
        FilterExpressionBuilder b = new FilterExpressionBuilder();
        Filter.Expression expression = b.and(
                b.in("year", 2025, 2024), // 强行要求元数据中 year 必须是 2024 或 2025
                b.eq("name", "yingzi")     // 强行要求元数据中 name 必须等于 "yingzi"
        ).build();

        // 2. 执行检索：只有满足 query("Spring") 语义最接近，且符合上面元数据硬性卡死条件的数据，才会被返回
        return simpleVectorStore.similaritySearch(SearchRequest
                .builder()
                .query("Spring")
                .topK(2)
                .filterExpression(expression) // 将元数据过滤条件拍在脸上，作为第一道闸机
                .build());
    }

}