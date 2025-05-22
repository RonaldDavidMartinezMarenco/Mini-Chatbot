package com.chatbot.ai_assistant.rag;
import java.util.List;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmbeddingService {
    //Llamaremos al embedding de Vertex AI
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
}
