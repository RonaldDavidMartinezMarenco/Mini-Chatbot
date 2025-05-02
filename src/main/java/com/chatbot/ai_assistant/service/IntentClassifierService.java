package com.chatbot.ai_assistant.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.stereotype.Service;

// Zero-Shot-Prompting

@Service
public class IntentClassifierService {

    private final ChatClient chatClient;

    
    public IntentClassifierService(ChatClient.Builder builder) 
    {
        this.chatClient = builder.build();
    }

    public enum Classifier
    {
        EDUCATIVO, NO_EDUCATIVO
    }
    
    public record ClasificacionRespuesta(String clasificacion) {}

    public Classifier classifyEducationalIntent(String userMessage) {
                
        String systemPrompt = """
        Responde únicamente con el siguiente JSON:
        {"clasificacion": "EDUCATIVO"} o {"clasificacion": "NO_EDUCATIVO"}
            Considera mensajes de saludo como "Hola", "¿Cómo estás?", "Hola, ¿cómo estás?", "Gracias", "Hasta luego", "Listo", "si", "no","claro","esta bien","hasta pronto" como EDUCATIVO sin importar si es mayuscula o minuscula.
        """;

        String userPrompt = """
        Clasifica el siguiente mensaje:
        "%s"
        Respuesta JSON:
        """.formatted(userMessage);

        ClasificacionRespuesta respuesta = chatClient.prompt()
            .system(systemPrompt)
            .user(userPrompt)
            .options(OllamaOptions.builder()
                .model("phi3")
                .temperature(0.1)
                .build())
            .call()
            .entity(ClasificacionRespuesta.class);


        System.out.println("Respuesta JSON del modelo: " + (respuesta != null ? respuesta.clasificacion() : "null"));    

        if (respuesta != null && "EDUCATIVO".equalsIgnoreCase(respuesta.clasificacion())) {
            return Classifier.EDUCATIVO;
        } else {
            return Classifier.NO_EDUCATIVO;
        }
            }
}