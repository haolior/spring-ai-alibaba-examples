package cn.haohaoli.cloud.ai.practice.mcp.config;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
public class Tools {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );

    @Tool(name = "calculatePercentage", description = "计算一个数值占总数的百分比，返回保留两位小数的百分比字符串")
    public String calculatePercentage(BigDecimal value, BigDecimal total) {
        if (value == null || total == null) {
            throw new IllegalArgumentException("数值和总数不能为空");
        }
        if (BigDecimal.ZERO.compareTo(total) == 0) {
            throw new IllegalArgumentException("总数不能为0");
        }

        BigDecimal percentage = value
                .divide(total, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        return percentage + "%";
    }

    @Tool(name = "generateUuid", description = "生成一个随机UUID字符串")
    public String generateUuid() {
        System.out.println("generateUuid start");
        return UUID.randomUUID().toString();
    }

    @Tool(name = "validateEmail", description = "校验字符串是否是合法的邮箱格式")
    public boolean validateEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    @Tool(name = "encodeBase64", description = "将文本编码为Base64字符串")
    public String encodeBase64(String text) {
        if (text == null) {
            throw new IllegalArgumentException("文本不能为空");
        }
        return Base64.getEncoder().encodeToString(text.getBytes());
    }

    @Tool(name = "decodeBase64", description = "将Base64字符串解码为普通文本")
    public String decodeBase64(String encodedText) {
        if (encodedText == null || encodedText.isBlank()) {
            throw new IllegalArgumentException("Base64文本不能为空");
        }
        return new String(Base64.getDecoder().decode(encodedText));
    }
}