package com.rag.controller;

import com.rag.model.ChatMessage;
import com.rag.model.QueryRequest;
import com.rag.service.RagPipelineService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final RagPipelineService ragPipelineService;
    private final List<ChatMessage> chatHistory = new CopyOnWriteArrayList<>();

    public ChatController(RagPipelineService ragPipelineService) {
        this.ragPipelineService = ragPipelineService;
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestBody QueryRequest request) {
        // Add user message to history
        chatHistory.add(new ChatMessage("user", request.getQuery()));

        StringBuilder fullResponse = new StringBuilder();

        return ragPipelineService.query(request.getQuery())
                .doOnNext(fullResponse::append)
                .doOnComplete(() -> {
                    // Add assistant response to history
                    chatHistory.add(new ChatMessage("assistant", fullResponse.toString()));
                });
    }

    @GetMapping("/history")
    public List<ChatMessage> getChatHistory() {
        return new ArrayList<>(chatHistory);
    }

    @DeleteMapping("/history")
    public void clearHistory() {
        chatHistory.clear();
    }
}
