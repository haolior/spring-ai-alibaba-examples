package com.cloud.alibaba.ai.example.tools;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileReadTool implements Tool<FileReadTool.Request, String> {
    @Override
    public ToolCallback toolCallback() {
        return FunctionToolCallback.builder("file_read", this)
                .description("用于读取本地文件的工具")
                .inputType(Request.class)
                .build();
    }

    @Override
    public String apply(FileReadTool.Request request, ToolContext toolContext) {
        try {
            // 读取模型指定的文件路径，并把内容返回给 Agent 作为后续推理依据。
            return Files.readString(Path.of(request.filePath));
        } catch (IOException e) {
            return "文件读取失败：" + e.getMessage();
        }
    }

    @JsonClassDescription("读取文件请求")
    public record Request(
            @JsonProperty(value = "file_path", required = true)
            @JsonPropertyDescription("要读取的文件路径")
            String filePath
    ) {}
}
