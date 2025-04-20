package com.chatbot.ai_assistant.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.concurrent.ConcurrentHashMap;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class ChatService {

    private final ChatClient chatClient;
    private final InMemoryChatMemory memory;


    public ChatService (ChatClient.Builder builder) {
        this.memory = new InMemoryChatMemory();
        this.chatClient = builder.defaultAdvisors(new MessageChatMemoryAdvisor(memory)).build(); //Lo usamos sin memoria momentanemente hasta que tengamos varios chats.
    }
    

    public String processChatMessage(String message) {
        String prompt = "Eres un chatbot especializado en educación y solo respondes preguntas estrictamente educativas en español. No aceptas preguntas personales, de entretenimiento, políticas, de opinión o cualquier otro tema fuera del ámbito educativo. Antes de responder, analiza si la pregunta está relacionada con educación (matemáticas, ciencias, historia, literatura, tecnología, etc.). Si la pregunta no es educativa, responde con: 'Lo siento, soy un chatbot entrenado solo para responder preguntas educativas'";
        String fullPrompt = prompt + "\nUsuario: " + message;

        return chatClient.prompt()
                .user(fullPrompt)
                .call()
                .content();
    }

    public Flux<String> streamChatResponse(String message) {

        /* 
        SystemMessage systemMessage = new SystemMessage(
            "Eres un asistente educativo experto, especializado en proporcionar explicaciones claras y detalladas sobre temas académicos. Tu función es ayudar a estudiantes de [nivel educativo] a comprender conceptos en áreas como [materias específicas]. Responde de manera estructurada, utilizando ejemplos y analogías cuando sea apropiado. Si una pregunta no está relacionada con temas educativos, indica amablemente que tu función es exclusivamente educativa y redirige la conversación hacia temas académicos."
        );

        UserMessage userMessage = new UserMessage(message);
        Prompt prompt = new Prompt(
            List.of(systemMessage, userMessage),
            OllamaOptions.builder()
                    .model("phi3")
                    .temperature(0.4)
                    .build()
        );
        return chatClient.prompt(prompt).stream().content();
        */
        return chatClient.prompt()
                .system("Eres un asistente educativo experto, especializado en proporcionar explicaciones claras y detalladas sobre temas académicos. Tu función es ayudar a estudiantes de [nivel experto] a comprender conceptos en áreas como [Matematicas, Ciencias, Lenguaje, Historia, Quimica, Biologia, etc.]. Responde de manera estructurada, utilizando ejemplos y analogías cuando sea apropiado. Si una pregunta no está relacionada con temas educativos Por ejemplo los videojuegos, peliculas, series, compras, etc.indica amablemente que tu función es exclusivamente educativa y redirige la conversación hacia temas académicos.")
                .user(message)
                .options(OllamaOptions.builder()
                .model("phi3")
                .temperature(0.2)
                .build())
                .stream()
                .content();
        }

    public void clearMemory() {
        memory.clear(null);
    }

}