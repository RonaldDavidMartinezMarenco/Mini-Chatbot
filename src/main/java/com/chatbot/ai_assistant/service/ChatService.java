package com.chatbot.ai_assistant.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatOptions;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.stereotype.Service;
import com.chatbot.ai_assistant.controller.DocumentController;
import com.chatbot.ai_assistant.rag.ragService; 
import reactor.core.publisher.Flux;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.time.Duration;

//Logic
@Service
public class ChatService {

    private final ChatClient chatClient;
    private final IntentClassifierService intent;
    private final Map<String, ChatMemory> userMemories = new ConcurrentHashMap<>();
    private final DocumentController documentController;
    private final ragService ragService; // <--- INYECTA RagService

    public ChatService(ChatClient.Builder builder, IntentClassifierService intent, DocumentController documentController, ragService ragService) {
        this.documentController = documentController;
        this.chatClient = builder.build();
        this.intent = intent;
        this.ragService = ragService;
    }
    private ChatMemory getMemoryForUser(String username) {
        String key = (username != null && !username.isBlank()) ? username : "anonymous";
        return userMemories.computeIfAbsent(key, k -> new InMemoryChatMemory());
    }
     public void clearUserMemory(String username) {
        if (username != null && !username.isBlank()) {
            userMemories.remove(username);
        }
        userMemories.remove("anonymous");
    }

    public Flux<String> streamChatResponse(String message, String carrera, List<String> materias, String username, String documentId) {

        if (message == null || message.trim().isEmpty()) {
        return Flux.just("Por favor, escribe una pregunta o mensaje.");
        }

        String documentContent = getValidDocumentContent(documentId);

        if (documentId != null && (documentContent == null || documentContent.isBlank())) {
            return Flux.just("El documento subido está vacío o no se pudo extraer texto.");
        }

        if ("__NO_EDUCATIVO__".equals(documentContent)) {
            return Flux.just("El documento subido no contiene contenido educativo.");
        }
        if (!isEducational(message)) {
            return Flux.just("Lo siento, solo puedo responder preguntas educativas.");
        }
        //System Prompts en base a las carreras
        boolean isCarreraBase = (carrera == null || carrera.isBlank() || carrera.equalsIgnoreCase("Educacion(BASE)"));
        boolean isMateriasBase = (materias == null || materias.isEmpty() || materias.stream().anyMatch(
                m -> m == null || m.equalsIgnoreCase("Otra")));

        StringBuilder promptBuilder;
        if (isCarreraBase && isMateriasBase) {
            promptBuilder = new StringBuilder("Eres un asistente educativo experto, especializado en proporcionar explicaciones claras y detalladas sobre temas académicos. Tu función es ayudar a estudiantes de cualquier área a comprender conceptos en materias educativas. Responde de manera estructurada, utilizando ejemplos y analogías cuando sea apropiado. Si una pregunta no está relacionada con temas educativos por ejemplo, videojuegos, pelicuas,series, preguntas sobre compras, preguntas sobre videojuegos,etc. indica amablemente que tu función es exclusivamente educativa y redirige la conversación hacia temas académicos.(Si no tiene nombre, no lo menciones simplemente.)");
        } else {
            promptBuilder = new StringBuilder(
                    "Eres un asistente educativo experto, das explicaciones claras y detalladas sobre cuestiones academicas en general.");
            if (!isCarreraBase) {
                promptBuilder.append(" en ").append(carrera);
            }
            if (!isMateriasBase) {
                promptBuilder.append(", especializado en: ").append(String.join(", ", materias)).append(". De igual manera puedes responder a preguntas educativas que no sean relacionadas con la materia. Pero recuerdale al usuario cuales son tus puntos fuertes si la pregunta no esta relacionada con la materia inicialmente.");
            }
            String safeUsername = (username == null || username.isBlank()) ? "estudiante" : username;
            if (username == null || username.isBlank()) {
                promptBuilder.append(". No menciones ningún nombre en tus respuestas. ");
            } else {
                promptBuilder.append(". El usuario se llama exactamente: ").append(safeUsername).append(". ");
                promptBuilder.append(" Menciona el nombre del usuario. ");
                promptBuilder.append(
                        ". Responde de manera clara y estructurada. Si la pregunta no es educativa, por ejemplo, videojuegos, peliculas, series, juegos, compras, futbol, apuestas o alguna otra responde que no tienes permitido dar respuestas a ese tipo de preguntas. indícalo amablemente.");
            }          
        }
        /* 
        if (documentContent != null && !documentContent.isBlank()) {
                promptBuilder.append("\n\nEl siguiente documento ha sido proporcionado por el usuario. Úsalo como contexto para responder preguntas:\n");
                promptBuilder.append(documentContent.substring(0, Math.min(50, documentContent.length())));
            }
        */
        promptBuilder.append(getRelevantDocumentContext(documentId, message, 2)); // top 3 chunks relevantes

        
        String systemPrompt = promptBuilder.toString();


        ChatMemory chatMemory = getMemoryForUser(username);
        PromptChatMemoryAdvisor chatMemoryAdvisor = new PromptChatMemoryAdvisor(chatMemory);
        //Retornar Respuesta
        return Flux.create(sink -> {
            String response = chatClient.prompt()
                    .system(systemPrompt)
                    .user(message)
                    .advisors(chatMemoryAdvisor)
                    .options(VertexAiGeminiChatOptions.builder()
                            .model("gemini-2.0-flash-001")
                            .temperature(0.5)
                            .maxOutputTokens(300)
                            .build())
                    .call()
                    .chatResponse()
                    .getResult()
                    .getOutput()
                    .getText();

            //Procesar Streaming
            // Split response into smaller chunks 
            String[] chunks = response.split("(?<=[.!?]\\s)");
            
            // Use Flux.interval to emit chunks with delay
            Flux.interval(Duration.ofMillis(200))
                .take(chunks.length)
                .subscribe(
                    index -> {
                        if (index < chunks.length) {
                            sink.next(chunks[(int)index.longValue()]);
                        }
                    },
                    sink::error,
                    sink::complete
                );
        });       
    }
    private String getValidDocumentContent(String documentId) {
        if (documentId == null || documentId.isBlank()) return null;
        String content = documentController.getDocumentContent(documentId);
        if (content != null && !content.isBlank()) {
            if (!"unisimon-doc".equals(documentId) && !isEducational(content)) return "__NO_EDUCATIVO__";
        }
        return content;
    }
    private boolean isEducational(String text) {
        return intent.classifyEducationalIntent(text) != IntentClassifierService.Classifier.NO_EDUCATIVO;
    }
    private String getRelevantDocumentContext(String documentId, String question, int topK) {
        if (documentId == null || documentId.isBlank()) return "";
        List<String> relevantChunks = ragService.retrieveRelevantChunks(documentId, question, topK);
        if (relevantChunks.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        sb.append("\n\nFragmentos relevantes del documento proporcionado por el usuario:\n");
        relevantChunks.forEach(chunk -> sb.append(chunk).append("\n"));

        int totalChars = sb.length();
        System.out.println("Fragmentos relevantes para el documento " + documentId + ":");
        relevantChunks.forEach(System.out::println);
        System.out.println("Total caracteres enviados al prompt: "+totalChars);

        return sb.toString();
    }
}