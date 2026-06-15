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

import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.JedisPooled;

/**
 * Redis 向量库配置。
 *
 * <p>
 * RedisVectorStore 的自动配置不会声明 metadata 字段。需要过滤的 metadata 必须进入
 * RediSearch schema，否则 JSON 中有字段也不能作为 filterExpression 查询条件。
 */
@Configuration
public class RedisVectorStoreConfig {

	@Bean
	public RedisVectorStore redisVectorStore(EmbeddingModel embeddingModel, BatchingStrategy batchingStrategy,
			JedisConnectionFactory jedisConnectionFactory,
			@Value("${spring.ai.vectorstore.redis.index-name}") String indexName,
			@Value("${spring.ai.vectorstore.redis.prefix}") String prefix,
			@Value("${spring.ai.vectorstore.redis.initialize-schema:false}") boolean initializeSchema) {

		return RedisVectorStore.builder(jedisPooled(jedisConnectionFactory), embeddingModel)
			.indexName(indexName)
			.prefix(prefix)
			.metadataFields(RedisVectorStore.MetadataField.tag("source_id"),
					RedisVectorStore.MetadataField.tag("source_file"), RedisVectorStore.MetadataField.tag("category"),
					RedisVectorStore.MetadataField.tag("title"))
			.initializeSchema(initializeSchema)
			.batchingStrategy(batchingStrategy)
			.build();
	}

	private JedisPooled jedisPooled(JedisConnectionFactory jedisConnectionFactory) {
		JedisClientConfig clientConfig = DefaultJedisClientConfig.builder()
			.ssl(jedisConnectionFactory.isUseSsl())
			.clientName(jedisConnectionFactory.getClientName())
			.timeoutMillis(jedisConnectionFactory.getTimeout())
			.password(jedisConnectionFactory.getPassword())
			.build();

		return new JedisPooled(new HostAndPort(jedisConnectionFactory.getHostName(), jedisConnectionFactory.getPort()),
				clientConfig);
	}

}
