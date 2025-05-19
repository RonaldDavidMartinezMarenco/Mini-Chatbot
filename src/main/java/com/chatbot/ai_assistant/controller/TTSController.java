package com.chatbot.ai_assistant.controller;

import com.chatbot.ai_assistant.service.TTSService;

import com.chatbot.ai_assistant.service.TTSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class TTSController {
    
    @Autowired
    private TTSService ttsService;

    @GetMapping("/speak")
    public ResponseEntity<byte[]> speak(@RequestParam String text) {
        if (text == null || text.length() > 300) {
            return ResponseEntity.badRequest().body(null);
        }
        byte[] audio = ttsService.synthesize(text);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("audio/mpeg")); // ElevenLabs devuelve MP3
        headers.setContentLength(audio.length);
        return new ResponseEntity<>(audio, headers, HttpStatus.OK);
    }


}
