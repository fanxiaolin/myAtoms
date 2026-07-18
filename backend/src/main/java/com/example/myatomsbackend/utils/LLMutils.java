package com.example.myatomsbackend.utils;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

public class LLMutils {
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final String apiKey;
    private final String chatCompletionsUrl;
    private final OkHttpClient client;

    public LLMutils(String apiKey, String baseUrl) {
        this.apiKey = apiKey;
        this.chatCompletionsUrl = stripTrailingSlash(baseUrl) + "/v1/chat/completions";
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public LLMResponse chatCompletions(String requestJson) throws IOException {
        Request request = new Request.Builder()
                .url(chatCompletionsUrl)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(requestJson, JSON))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() == null ? "" : response.body().string();
            return new LLMResponse(response.code(), responseBody);
        }
    }

    public LLMStreamResponse chatCompletionsStream(String requestJson) throws IOException {
        Request request = new Request.Builder()
                .url(chatCompletionsUrl)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .header("Accept", "text/event-stream")
                .post(RequestBody.create(requestJson, JSON))
                .build();

        return new LLMStreamResponse(client.newCall(request).execute());
    }

    private static String stripTrailingSlash(String value) {
        return value != null && value.endsWith("/")
                ? value.substring(0, value.length() - 1)
                : value;
    }

    public record LLMResponse(int statusCode, String body) {
    }

    public static final class LLMStreamResponse implements AutoCloseable {
        private static final int BUFFER_SIZE = 1024;

        private final Response response;

        private LLMStreamResponse(Response response) {
            this.response = response;
        }

        public int statusCode() {
            return response.code();
        }

        public String contentType() {
            return response.header("Content-Type");
        }

        public void writeTo(OutputStream outputStream) throws IOException {
            if (response.body() == null) {
                close();
                throw new IOException("SiliconFlow 返回空响应");
            }

            try (this; InputStream inputStream = response.body().byteStream()) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int length;
                while ((length = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, length);
                    outputStream.flush();
                }
            }
        }

        @Override
        public void close() {
            response.close();
        }
    }
}
