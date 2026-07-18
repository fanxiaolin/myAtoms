package com.example.myatomsbackend.controller;

import com.example.myatomsbackend.service.MyAtomsService;
import com.example.myatomsbackend.utils.LLMutils.LLMResponse;
import com.example.myatomsbackend.utils.LLMutils.LLMStreamResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import tools.jackson.databind.JsonNode;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/api/chat")
public class MyAtomsController {
    private final MyAtomsService myAtomsService;

    public MyAtomsController(MyAtomsService myAtomsService) {
        this.myAtomsService = myAtomsService;
    }

    @PostMapping(
            value = "/completions",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> chatCompletions(@RequestBody JsonNode requestBody) {
        validateRequest(requestBody);
        LLMResponse response = myAtomsService.chatCompletions(requestBody.deepCopy());

        return ResponseEntity.status(response.statusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(response.body());
    }

    @PostMapping(
            value = "/completions/stream",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public ResponseEntity<StreamingResponseBody> chatCompletionsStream(
            @RequestBody JsonNode requestBody) {
        validateRequest(requestBody);
        LLMStreamResponse response = myAtomsService.chatCompletionsStream(requestBody.deepCopy());
        StreamingResponseBody responseBody = response::writeTo;

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(response.statusCode())
                .header("Cache-Control", "no-cache")
                .header("X-Accel-Buffering", "no");

        String upstreamContentType = response.contentType();
        if (upstreamContentType != null && !upstreamContentType.isBlank()) {
            responseBuilder.header("Content-Type", upstreamContentType);
        } else {
            responseBuilder.contentType(MediaType.TEXT_EVENT_STREAM);
        }

        return responseBuilder.body(responseBody);
    }

    private static void validateRequest(JsonNode requestBody) {
        if (!requestBody.isObject()) {
            throw new ResponseStatusException(BAD_REQUEST, "请求体必须是 JSON 对象");
        }
        if (!requestBody.has("messages") || !requestBody.get("messages").isArray()
                || requestBody.get("messages").isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "messages 必须是非空数组");
        }
    }

}
