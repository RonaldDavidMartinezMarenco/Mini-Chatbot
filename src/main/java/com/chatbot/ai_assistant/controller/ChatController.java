package com.chatbot.ai_assistant.controller;

import java.util.List;

import javax.print.attribute.standard.Media;

import org.apache.tomcat.util.http.parser.MediaType;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
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
import com.chatbot.ai_assistant.service.*;
/**
 *
 * @author Ronald
 */


/*
    * Esta clase se encarga de enviar las solicitudes y recibir la respuesta del LLM
 */
@RestController 
@CrossOrigin(origins = "*") //Permite que el servidor de react se comunique con el servidor de spring
public class ChatController {

    private final ChatService chatService; //Objeto de Spring AI

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }
   
    @PostMapping("/stream")
    public Flux<String> chatWithStream(@org.springframework.web.bind.annotation.RequestBody ChatRequest req) {
       return chatService.streamChatResponse(req.getMessage(), req.getCarrera(), req.getMaterias(), req.getUsername());
    }
    /*
     @PostMapping("/clear-memory")
    public void clearMemory() {
        chatService.clearMemory();
    }
    @PostMapping("/logout")
    public void logout() {
        chatService.clearMemory();
    }
    
     */
  
    
    //We use a text classification zero-shot model
    public String classifyIntent(String message) {
        // Token de Hugging Face para autenticar la solicitud
        String huggingFaceToken = "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"; // Reemplaza con tu token válido
        String apiUrl = "https://api-inference.huggingface.co/models/facebook/bart-large-mnli";
    
        // Configurar los encabezados de la solicitud
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + huggingFaceToken);
        headers.set("Content-Type", "application/json");
    
        // Crear el cuerpo de la solicitud con el mensaje y las etiquetas
        String requestBody = "{ \"inputs\": \"" + message + "\", \"parameters\": { \"candidate_labels\": [\"Universidad\" ,\"Educativo\", \"Investigacion\", \"Cursos en linea\",\"No educativo\", \"Apuestas\",\"Peliculas\",\"Videojuegos\",\"Cortesia\",\"Saludo\" ] } }";
    
        // Crear la solicitud HTTP
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, String.class);
    
            // Verificar si la respuesta es exitosa
            if (response.getStatusCode().is2xxSuccessful()) {
                // Parsear la respuesta JSON
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode responseBody = objectMapper.readTree(response.getBody());
    
                // Extraer la etiqueta con mayor probabilidad
                JsonNode labelsNode = responseBody.get("labels");
                return labelsNode.get(0).asText(); // Retorna la etiqueta con mayor probabilidad (primera en el orden)
            } else {
                throw new RuntimeException("Error al clasificar la intención: " + response.getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al procesar la respuesta de Hugging Face.");
        }
    }
     /*
     
    @PostMapping("/chat") //Envia datos al servidor, en este caso 8080.
    public String chat(@RequestParam String message){
        String prompt = "Eres un chatbot especializado en educación y solo respondes preguntas estrictamente educativas en español. No aceptas preguntas personales, de entretenimiento, políticas, de opinión o cualquier otro tema fuera del ámbito educativo. Antes de responder, analiza si la pregunta está relacionada con educación (matemáticas, ciencias, historia, literatura, tecnología, etc.). Si la pregunta no es educativa, responde con: 'Lo siento, soy un chatbot entrenado solo para responder preguntas educativas'";
        String fullPrompt = prompt +"\nUsuario"+ message;
        return chatClient.prompt()
                .user(fullPrompt)
                .call()
                .content();
    }      
    */

}
