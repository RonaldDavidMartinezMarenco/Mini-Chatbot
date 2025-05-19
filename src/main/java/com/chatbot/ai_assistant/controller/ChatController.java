package com.chatbot.ai_assistant.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import com.chatbot.ai_assistant.service.ChatService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Flux;
import com.chatbot.ai_assistant.dto.ChatRequest;

/**
 *
 * @author Ronald
 */


/*
    * Esta clase se encarga de enviar las solicitudes y recibir la respuesta del LLM
 */
//endpoints managment
@RestController 
@CrossOrigin(origins = "*") 
public class ChatController {

    private final ChatService chatService; 

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }
   
    @PostMapping("/stream")
    public Flux<String> chatWithStream(@org.springframework.web.bind.annotation.RequestBody ChatRequest req) {
       return chatService.streamChatResponse(req.getMessage(), req.getCarrera(), req.getMaterias(), req.getUsername(),req.getDocumentId());
    }

}
