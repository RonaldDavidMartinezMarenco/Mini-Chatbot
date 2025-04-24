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
    private final IntentClassifierService intent;

    public ChatService (ChatClient.Builder builder, IntentClassifierService intent) {
        this.memory = new InMemoryChatMemory();
        this.chatClient = builder.defaultAdvisors(new MessageChatMemoryAdvisor(memory)).build(); //Lo usamos sin memoria momentanemente hasta que tengamos varios chats.
        //this.chatClient = builder.build();
        this.intent = intent;
    }
    

    public String processChatMessage(String message) {
        String prompt = "Eres un chatbot especializado en educación y solo respondes preguntas estrictamente educativas en español. No aceptas preguntas personales, de entretenimiento, políticas, de opinión o cualquier otro tema fuera del ámbito educativo. Antes de responder, analiza si la pregunta está relacionada con educación (matemáticas, ciencias, historia, literatura, tecnología, etc.). Si la pregunta no es educativa, responde con: 'Lo siento, soy un chatbot entrenado solo para responder preguntas educativas'";
        String fullPrompt = prompt + "\nUsuario: " + message;

        return chatClient.prompt()
                .user(fullPrompt)
                .call()
                .content();
    }

    public Flux<String> streamChatResponse(String message, String carrera,List<String> materias, String username ) {

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
          // Si el usuario selecciona "Otra" como carrera o materia, usar prompt base
        
        IntentClassifierService.Classifier tipo = intent.classifyEducationalIntent(message);
        
        if(tipo == IntentClassifierService.Classifier.NO_EDUCATIVO)
        {
            return Flux.just("Lo siento, solo puedo responder preguntas educativas.");
        }

        boolean isCarreraBase = carrera != null && carrera.equalsIgnoreCase("Educacion(BASE)");
        boolean isMateriasBase = materias != null && materias.stream().anyMatch(
        m -> m != null && m.equalsIgnoreCase("Otra")
        );

        String systemPrompt;
        if (isCarreraBase && isMateriasBase) {
            systemPrompt = "Eres un asistente educativo experto, especializado en proporcionar explicaciones claras y detalladas sobre temas académicos. Tu función es ayudar a estudiantes de cualquier área a comprender conceptos en materias educativas. Responde de manera estructurada, utilizando ejemplos y analogías cuando sea apropiado. Si una pregunta no está relacionada con temas educativos por ejemplo, videojuegos, pelicuas,series, preguntas sobre compras, preguntas sobre videojuegos,etc. indica amablemente que tu función es exclusivamente educativa y redirige la conversación hacia temas académicos.";
        } else {
            StringBuilder promptBuilder = new StringBuilder("Eres un asistente educativo experto");
            if (!isCarreraBase) {
                promptBuilder.append(" en ").append(carrera);

            }
            if (!isMateriasBase) {
                promptBuilder.append(", especializado en: ").append(String.join(", ", materias));
            }
            promptBuilder.append(". El usuario se llama exactamente: ").append(username).append(". ");
            promptBuilder.append("En cada respuesta, saluda o menciona al usuario por su nombre al inicio o al final de tu mensaje. ");
            promptBuilder.append(". Responde de manera clara y estructurada. Si la pregunta no es educativa, por ejemplo, vieojuegos, peliculas, series, juegos, compras, futbol, apuestas o alguna otra responde que no tienes permitido dar respuestas a ese tipo de preguntas. indícalo amablemente.");
            systemPrompt = promptBuilder.toString();
        }

        return chatClient.prompt()
                .advisors(advisor -> advisor.param("chat_memory_conversation_id", username))
                .system(systemPrompt)
                .user(message)
                .options(OllamaOptions.builder()
                .model("phi3")
                .temperature(0.2)
                .build())
                .stream()
                .content();
        }
     /*    
    public void clearMemory() {
        memory.clear(null);
    }
        */

}