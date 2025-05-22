package com.chatbot.ai_assistant.rag;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chatbot.ai_assistant.rag.model.DocumentChunk;

@Service
public class ragService {
    private final Map<String, List<DocumentChunk>> chunkStore = new ConcurrentHashMap<>();

    @Autowired
    private EmbeddingService embeddingService;
     // 2. Indexar documento: divide en chunks, calcula embeddings y guarda
    public void indexDocument(String documentId, String content) {
        List<String> chunks = ChunkUtils.chunkText(content, 300); // 300 caracteres por chunk
        List<DocumentChunk> chunkList = new ArrayList<>();
        int i = 0;
        for (String chunk : chunks) {
            float[] embedding = embeddingService.embed(chunk);
            chunkList.add(new DocumentChunk(documentId + "_" + i, documentId, chunk, embedding));
            i++;
        }
        chunkStore.put(documentId, chunkList);
    }
    // 3. Recuperar los chunks más relevantes para una pregunta
    public List<String> retrieveRelevantChunks(String documentId, String question, int topK) {
        float[] questionEmbedding = embeddingService.embed(question);
        List<DocumentChunk> chunks = chunkStore.getOrDefault(documentId, Collections.emptyList());
        // Calcula similitud coseno y ordena de mayor a menor
        return chunks.stream()
            .map(chunk -> Map.entry(chunk, cosineSimilarity(chunk.getEmbedding(), questionEmbedding)))
            .sorted((a, b) -> -Float.compare(a.getValue(), b.getValue()))
            .limit(topK)
            .map(entry -> entry.getKey().getText())
            .collect(Collectors.toList());
    }

    // Utilidad para similitud coseno
    private float cosineSimilarity(float[] a, float[] b) {
        float dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return (float) (dot / (Math.sqrt(normA) * Math.sqrt(normB) + 1e-10));
    }
}
