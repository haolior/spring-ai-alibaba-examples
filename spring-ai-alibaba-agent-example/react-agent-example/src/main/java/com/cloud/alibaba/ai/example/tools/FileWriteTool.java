package com.cloud.alibaba.ai.example.tools;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

public class FileWriteTool implements Tool<FileWriteTool.Request, String> {

    @Override
    public ToolCallback toolCallback() {
        return FunctionToolCallback.builder("file_write", this)
                .description("用于写入本地文件的工具")
                .inputType(Request.class)
                .build();
    }

    @Override
    public String apply(FileWriteTool.Request s, ToolContext toolContext) {
        try {
            // 将模型传入的相对路径限制在当前运行目录下，避免直接使用未规范化的路径。
            String safePath = Paths.get(System.getProperty("user.dir"))
                    .resolve(s.filePath)
                    .normalize()
                    .toString();

            FileWriter writer = new FileWriter(safePath);
            writer.write(s.content);
            writer.close();
            
            return "文件写入成功：" + s.filePath;
        } catch (IOException e) {
            return "文件写入失败：" + e.getMessage();
        }
    }

    @JsonClassDescription("写入文件请求")
    public record Request(
            @JsonProperty(value = "file_path", required = true)
            @JsonPropertyDescription("要写入的文件路径")
            String filePath,
            @JsonProperty(value = "content", required = true)
            @JsonPropertyDescription("要写入文件的内容")
            String content
    ) {

    }
}
