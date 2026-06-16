/*
 * Copyright 2024-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.haohaoli.cloud.ai.practice.rag.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.JedisPooled;

import java.util.ArrayList;
import java.util.List;

/**
 * Redis 向量库配置。
 *
 * <p>
 * RedisVectorStore 的自动配置不会声明 metadata 字段。需要过滤的 metadata 必须进入
 * RedisSearch schema，否则 JSON 中有字段也不能作为 filterExpression 查询条件。
 */
@Configuration
@EnableConfigurationProperties(RedisVectorStoreConfig.RedisVectorStoreProperties.class)
public class RedisVectorStoreConfig {

    private static final Logger log = LoggerFactory.getLogger(RedisVectorStoreConfig.class);

    @Bean
    public RedisVectorStore redisVectorStore(EmbeddingModel embeddingModel,
                                             RedisVectorStoreProperties properties, JedisConnectionFactory jedisConnectionFactory) {

        RedisVectorStore.Builder builder = RedisVectorStore.builder(this.jedisPooled(jedisConnectionFactory),
                        embeddingModel)
                .initializeSchema(properties.isInitializeSchema())
                .batchingStrategy(new TokenCountBatchingStrategy())
                .indexName(properties.getIndexName())
                .prefix(properties.getPrefix());

        if (properties.getAlgorithm() != null) {
            builder.vectorAlgorithm(properties.getAlgorithm());
        }

        if (!CollectionUtils.isEmpty(properties.getMetadataFields())) {
            builder.metadataFields(properties.getMetadataFields().stream().map(this::toMetadataField).toList());
        }
        return builder.build();
    }

    private RedisVectorStore.MetadataField toMetadataField(MetadataFieldProperties field) {
        return switch (field.getType()) {
            case NUMERIC -> RedisVectorStore.MetadataField.numeric(field.getName());
            case TEXT -> RedisVectorStore.MetadataField.text(field.getName());
            case TAG -> RedisVectorStore.MetadataField.tag(field.getName());
        };
    }

    private JedisPooled jedisPooled(JedisConnectionFactory jedisConnectionFactory) {
        JedisClientConfig clientConfig = DefaultJedisClientConfig.builder()
                .ssl(jedisConnectionFactory.isUseSsl())
                .clientName(jedisConnectionFactory.getClientName())
                .timeoutMillis(jedisConnectionFactory.getTimeout())
                .password(jedisConnectionFactory.getPassword())
                .build();

        return new JedisPooled(
                new HostAndPort(jedisConnectionFactory.getHostName(), jedisConnectionFactory.getPort()), clientConfig);
    }

    @ConfigurationProperties(prefix = "spring.ai.vectorstore.redis")
    public static class RedisVectorStoreProperties {

        private String indexName = "spring-ai-index";

        private String prefix = "embedding:";

        private String contentFieldName = "content";

        private String embeddingFieldName = "embedding";

        private RedisVectorStore.Algorithm algorithm = RedisVectorStore.Algorithm.HSNW;

        private List<MetadataFieldProperties> metadataFields = new ArrayList<>();

        private boolean initializeSchema;

        public String getIndexName() {
            return this.indexName;
        }

        public void setIndexName(String indexName) {
            this.indexName = indexName;
        }

        public String getPrefix() {
            return this.prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public String getContentFieldName() {
            return this.contentFieldName;
        }

        public void setContentFieldName(String contentFieldName) {
            this.contentFieldName = contentFieldName;
        }

        public String getEmbeddingFieldName() {
            return this.embeddingFieldName;
        }

        public void setEmbeddingFieldName(String embeddingFieldName) {
            this.embeddingFieldName = embeddingFieldName;
        }

        public RedisVectorStore.Algorithm getAlgorithm() {
            return this.algorithm;
        }

        public void setAlgorithm(RedisVectorStore.Algorithm algorithm) {
            this.algorithm = algorithm;
        }

        public List<MetadataFieldProperties> getMetadataFields() {
            return this.metadataFields;
        }

        public void setMetadataFields(List<MetadataFieldProperties> metadataFields) {
            this.metadataFields = metadataFields;
        }

        public boolean isInitializeSchema() {
            return this.initializeSchema;
        }

        public void setInitializeSchema(boolean initializeSchema) {
            this.initializeSchema = initializeSchema;
        }

    }

    public static class MetadataFieldProperties {

        private String name;

        private FieldType type = FieldType.TAG;

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public FieldType getType() {
            return this.type;
        }

        public void setType(FieldType type) {
            this.type = type;
        }

    }

    public enum FieldType {

        TAG, TEXT, NUMERIC
    }

}
