package cn.haohaoli.cloud.ai.practice.tool.config;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DateTimeTools {

    /**
     * 获取用户所在时区的当前日期和时间
     *
     * <p>【returnDirect = true 说明】</p>
     * 强行截断大模型的二轮思考网络循环。当此方法执行完毕后，返回的时间字符串将绕过大模型，
     * 直接作为最终的响应结果甩给前端调用者，极大地提升响应速度并节省大模型的 Token。
     *
     * @param toolContext 🛠️ <b>【工具上下文参数含义与底层原理】</b>
     * <ol>
     * <li><b>隐式暗道参数</b>：此参数在 Spring AI 底层属于“特权保留类型”。在系统启动、将该方法翻译成发送给大模型的 JSON Schema 报文时，底层反射引擎会<b>主动将该参数过滤掉</b>。大模型在网络报文中完全看不见这个参数，也管不着它。</li>
     * <li><b>安全隔离载体</b>：它专门用来承载敏感的、绝对不能让用户伪造或由大模型胡乱生成的参数（如当前登录人的租户ID、用户角色、风控信息）。</li>
     * <li><b>运行期动态交汇</b>：当大模型做出决策下达调用指令时，Spring AI 底层物理零件（MethodToolCallback）在触发反射前，会主动将本次 HTTP 请求中通过 `.toolContext(Map.of(...))` 暂存到内存里的 Map 捞出来，包装成 ToolContext 对象并精准注入到本参数中。</li>
     * <li><b>线程绝对安全</b>：本工具类虽为全局单例 Bean，但 toolContext 是作为方法局部变量传入的，在 JVM 各自请求的线程栈帧中相互隔离，完美契合“类是全局单例，数据是请求隔离”的工业级高并发要求。</li>
     * * @return String 格式化后的当前时区日期和时间字符串（例如: 2026-05-29T00:11:02.123+08:00）
     */
    @Tool(description = "获取用户所在时区的当前日期和时间", returnDirect = false)
    String getCurrentDateTime(ToolContext toolContext) {
        System.out.println(toolContext.getContext().get("username"));
        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
    }

}