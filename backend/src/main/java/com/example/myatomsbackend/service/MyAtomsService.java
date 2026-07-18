package com.example.myatomsbackend.service;

import com.example.myatomsbackend.utils.LLMutils;
import com.example.myatomsbackend.utils.LLMutils.LLMResponse;
import com.example.myatomsbackend.utils.LLMutils.LLMStreamResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;

@Service
public class MyAtomsService {
    private final String apiKey;
    private final String model;
    private final ObjectMapper objectMapper;
    private final LLMutils llmutils;

    public MyAtomsService(
            @Value("${llm.api-key}") String apiKey,
            @Value("${llm.base-url}") String baseUrl,
            @Value("${llm.model}") String model,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.model = model;
        this.objectMapper = objectMapper;
        this.llmutils = new LLMutils(apiKey, baseUrl);
    }

    public LLMResponse chatCompletions(JsonNode requestBody) {
        requireApiKey();

        // 模型由服务端 application.yml 统一配置，不允许外部请求指定。
        ((ObjectNode) requestBody).put("model", model);

        try {
            return llmutils.chatCompletions(objectMapper.writeValueAsString(requestBody));
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "调用 SiliconFlow 接口失败",
                    exception
            );
        }
    }

    public LLMStreamResponse chatCompletionsStream(JsonNode requestBody) {
        requireApiKey();

        // 流式接口由服务端统一开启思考模式，前端只需解析 SSE 增量。
        ObjectNode body = (ObjectNode) requestBody;
        body.put("model", model);
        body.put("stream", true);
        body.put("enable_thinking", true);

        try {
            return llmutils.chatCompletionsStream(objectMapper.writeValueAsString(body));
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "调用 SiliconFlow 流式接口失败",
                    exception
            );
        }
    }

    private void requireApiKey() {
        if (apiKey.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "未配置 SiliconFlow API Key，请设置环境变量 SILICONFLOW_API_KEY"
            );
        }
    }

}
