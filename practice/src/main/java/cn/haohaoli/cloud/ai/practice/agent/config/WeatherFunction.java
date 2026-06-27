package cn.haohaoli.cloud.ai.practice.agent.config;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.stereotype.Service;

import java.util.function.BiFunction;

/**
 * https://java2ai.com/docs/frameworks/agent-framework/tutorials/tools
 *
 * @author liwenhao
 */
@Service
public class WeatherFunction implements BiFunction<WeatherFunction.Request, ToolContext, String> {

    private final static Logger log = LoggerFactory.getLogger(WeatherFunction.class);

    @Override
    public String apply(Request request, ToolContext toolContext) {
        log.info("request:{}, context: {}", request, toolContext);
        return request.city() + "现在是晴天!";
    }


    public ToolCallback getCallback() {
        return FunctionToolCallback.builder("get_weather", this)
                .description("""
                        根据城市名称查询天气信息。
                        适用于用户询问天气、气温、是否下雨、出行建议、穿衣建议等天气相关问题
                        """)
                .inputType(Request.class)
                .build();
    }

    /**
     * 天气工具入参。
     *
     * @param city 模型从用户问题中提取出的城市名
     */
    @JsonClassDescription("天气查询请求")
    public record Request(
            @JsonProperty(value = "city", required = true)
            @JsonPropertyDescription("城市名") String city
    ) {
    }
}
