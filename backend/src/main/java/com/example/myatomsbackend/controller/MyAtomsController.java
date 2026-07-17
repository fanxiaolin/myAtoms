package com.example.myatomsbackend.controller;

import com.example.myatomsbackend.service.MyAtomsService;
import com.example.myatomsbackend.utils.LLMutils.LLMResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
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
