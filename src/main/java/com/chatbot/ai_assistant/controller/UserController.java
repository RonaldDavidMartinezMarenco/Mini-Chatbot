package com.chatbot.ai_assistant.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chatbot.ai_assistant.service.ChatService;

@RestController
public class UserController {
    
     @Autowired
    private ChatService chatService;

    @PostMapping("/logout")
    public void logout(@RequestParam(required = false) String username) {
        chatService.clearUserMemory(username);
    }
}
