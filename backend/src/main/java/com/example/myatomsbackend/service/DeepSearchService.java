package com.example.myatomsbackend.service;

import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class DeepSearchService {
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final String apiKey; private final ObjectMapper mapper;
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.SECONDS).readTimeout(6, TimeUnit.SECONDS)
            .writeTimeout(3, TimeUnit.SECONDS).callTimeout(8, TimeUnit.SECONDS).build();
    public DeepSearchService(@Value("${search.tavily-api-key:}") String apiKey, ObjectMapper mapper) { this.apiKey=apiKey; this.mapper=mapper; }

    public String researchContext(String topic) {
        if (apiKey.isBlank()) return "\n\n[深度搜索未配置：请设置 TAVILY_API_KEY。不得编造联网信源。]";
        List<String> queries = List.of(topic, topic + " 官方 数据", topic + " 最新 趋势", topic + " 争议 风险 反例");
        Map<String, JsonNode> unique = new ConcurrentHashMap<>();
        queries.parallelStream().forEach(query -> {
            try {
                ObjectNode body = mapper.createObjectNode(); body.put("api_key", apiKey); body.put("query", query);
                body.put("search_depth", "advanced"); body.put("max_results", 5); body.put("include_answer", false);
                Request request = new Request.Builder().url("https://api.tavily.com/search").post(RequestBody.create(mapper.writeValueAsString(body), JSON)).build();
                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful() || response.body() == null) return;
                    JsonNode results = mapper.readTree(response.body().string()).path("results");
                    for (JsonNode result : results) {
                        String url = result.path("url").asText(); if (!url.isBlank()) unique.putIfAbsent(url.split("\\?")[0], result.deepCopy());
                    }
                }
            } catch (IOException ignored) { }
        });
        StringBuilder out = new StringBuilder("\n\n# 深度搜索证据（仅引用下列真实 URL）\n");
        unique.values().stream().sorted(Comparator.comparing(item -> item.path("title").asText())).limit(25)
                .forEach(item -> out.append("- ").append(item.path("title").asText())
                .append("\n  URL: ").append(item.path("url").asText()).append("\n  摘要: ")
                .append(item.path("content").asText()).append("\n"));
        if (unique.isEmpty()) out.append("未获取到可用搜索结果。请基于已有知识回答，并明确说明未完成联网验证。\n");
        return out.toString();
    }
}
