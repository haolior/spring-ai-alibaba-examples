# AI 机票预订专家系统示例

这个应用展示如何使用 Spring AI Alibaba 构建一个 AI 专家系统，核心能力包括：

- 读取服务条款并回答问题（检索增强生成，RAG）
- 调用工具执行业务操作（Java 方法，Function Calling）
- 使用大语言模型与用户进行自然语言交互

![spring-ai-alibaba-flight-booking](diagram.png)

## 环境要求

- Java 17+
- 在环境变量 `AI_DASHSCOPE_API_KEY` 中配置 DashScope API Key

## 运行方式

可以在 IDE 中运行 `Application.java`，也可以在命令行执行 `mvn spring-boot:run`。

POM 中需要添加 Spring AI Alibaba Boot Starter：

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-alibaba-starter-dashscope</artifactId>
    <version>1.0.0.1</version>
</dependency>
```

在 `application.properties` 中添加 DashScope 配置：

```properties
spring.ai.dashscope.api-key=${AI_DASHSCOPE_API_KEY}
spring.ai.dashscope.chat.options.model=qwen-max
```

## 构建 Jar

```shell
./mvnw clean package
```

```shell
java -jar ./target/playground-flight-booking-0.0.1-SNAPSHOT.jar
```

## 构建前端（按需）

如果修改了前端代码，需要执行以下命令重新构建前端资源：

```shell
mvn clean compile -Pbuild-frontend
```

然后访问 [localhost:9000](http://localhost:9000) 查看效果。
