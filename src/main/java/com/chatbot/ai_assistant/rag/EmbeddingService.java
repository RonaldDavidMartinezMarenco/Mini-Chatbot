package com.chatbot.ai_assistant.rag;
import java.util.List;
import java.util.Map;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;



@Service
public class EmbeddingService {
    //Llamaremos al embedding de Vertex AI

    /* 
    private final EmbeddingModel embeddingModel;

    @Autowired
    public EmbeddingService (EmbeddingModel embeddingModel)
    {
        this.embeddingModel = embeddingModel;
    }

    public float[] embed(String text) {
        EmbeddingResponse response = embeddingModel.embedForResponse(List.of(text));
        return response.getResults().get(0).getOutput();
    }
        */

    //Lo llamaremos con JINA el cual es mas flexible en cuanato caraceteres que vertex ai mediante un rest template
    private final RestTemplate restTemplate;
    private static final String API_URL = "https://api.jina.ai/v1/embeddings"; // Cambia esto a la URL de tu servicio de embeddings
    private static final String API_KEY = System.getenv("JINA_API_KEY"); // Cambia esto a tu API Key de Jina

    public EmbeddingService() {
        this.restTemplate = new RestTemplate();
    }

    public float[] embed(String text) {
        // 1. Construir headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(API_KEY);

        // 2. Construir el cuerpo del request
        Map<String, Object> body = Map.of(
            "model", "jina-clip-v2",
            "input", List.of(Map.of("text", text))
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // 3. Enviar request
        ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, request, Map.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Error al llamar a Jina: " + response.getStatusCode());
        }

        // 4. Parsear la respuesta
        List<?> data = (List<?>) response.getBody().get("data");
        if (data == null || data.isEmpty()) {
            throw new RuntimeException("No se recibió ningún embedding.");
        }

        Map<?, ?> embeddingObj = (Map<?, ?>) data.get(0);
        List<?> embedding = (List<?>) embeddingObj.get("embedding");

        float[] result = new float[embedding.size()];
        for (int i = 0; i < embedding.size(); i++) {
            result[i] = ((Number) embedding.get(i)).floatValue();
        }
        return result;
    }
}
