package cn.haohaoli.cloud.ai.practice.mcp;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.resolution.ToolCallbackResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mcp")
public class McpController1 {

    private final ChatClient           client;
    private final ToolCallbackResolver toolCallbackResolver;
    private final ToolCallbackProvider toolCallbackProvider;

    public McpController1(ChatClient.Builder builder, ToolCallbackResolver toolCallbackResolver,
                          @Qualifier("localToolCallbacks") ToolCallbackProvider toolCallbackProvider) {
        this.client = builder.build();
        this.toolCallbackResolver = toolCallbackResolver;
        this.toolCallbackProvider = toolCallbackProvider;
    }

    @GetMapping("")
    public String simple(@RequestParam(value = "userQuery", defaultValue = "成都现在几点钟", required = false) String userQuery) {

        HttpClientStreamableHttpTransport streamableHttpTransport = HttpClientStreamableHttpTransport
                .builder("http://localhost:20000")
                .endpoint("/mcp")
                .build();
        McpSyncClient client1 = McpClient.sync(streamableHttpTransport).build();
        client1.initialize();

        HttpClientSseClientTransport httpClientSseTransport = HttpClientSseClientTransport
                .builder("http://localhost:48080")
                .sseEndpoint("/weather/sse")
                .build();


        McpSyncClient client2 = McpClient.sync(httpClientSseTransport).build();
        client2.initialize();

        SyncMcpToolCallbackProvider build = SyncMcpToolCallbackProvider.builder()
                .mcpClients(client1,client2)
                .build();
        ToolCallback encodeBase64 = toolCallbackResolver.resolve("encodeBase64");
        if (encodeBase64 != null) {
            System.out.println(encodeBase64.getToolDefinition());
        }
        return client.prompt(userQuery)
                .toolCallbacks(build.getToolCallbacks())
                .toolCallbacks(toolCallbackProvider.getToolCallbacks())
                .call().content()
                ;
    }


}
