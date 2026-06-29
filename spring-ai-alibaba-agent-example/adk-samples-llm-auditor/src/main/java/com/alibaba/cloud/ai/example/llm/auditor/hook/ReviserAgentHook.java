/*
 * Copyright 2026-2027 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.ai.example.llm.auditor.hook;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.ModelHook;
import org.springframework.ai.chat.messages.AssistantMessage;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * 修订 Agent Hook。
 *
 * <p>该 Hook 会在修订 Agent 输出后移除 {@code ---END-OF-EDIT---} 标记，让最终响应更适合作为接口结果返回。
 *
 * @author : zhengyuchao
 * @date : 2026/1/22
 */
public class ReviserAgentHook  extends ModelHook {


    private static final String _END_OF_EDIT_MARK = "---END-OF-EDIT---";


    @Override
    public String getName() {
        return "";
    }

    @Override
    public CompletableFuture<Map<String, Object>> beforeModel(OverAllState state, RunnableConfig config) {
        return CompletableFuture.completedFuture(Map.of());
    }

    @Override
    public CompletableFuture<Map<String, Object>> afterModel(OverAllState state, RunnableConfig config) {
        Optional<Object> messagesOpt = state.value("reviser_agent_output");
        if (!messagesOpt.isPresent()) {
            return CompletableFuture.completedFuture(Map.of());
        }
        if(messagesOpt.get() instanceof AssistantMessage){
            AssistantMessage message = (AssistantMessage) messagesOpt.get();
            // 移除修订 Agent 按提示词输出的结束标记。
            String text = message.getText();
            String newMessage = text;
            if(text.contains(_END_OF_EDIT_MARK)){
                newMessage = text.replace(_END_OF_EDIT_MARK,"");
            }
            AssistantMessage newAssistantMessage = AssistantMessage.builder()
                    .content(newMessage)
                    .media(message.getMedia())
                    .properties(message.getMetadata())
                    .toolCalls(message.getToolCalls())
                    .build();

            return CompletableFuture.completedFuture(Map.of("reviser_agent_output", newAssistantMessage));
        }
        return CompletableFuture.completedFuture(Map.of());
    }
}
