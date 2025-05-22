package com.chatbot.ai_assistant.rag.model;

public class DocumentChunk {
    private String chunkId;
    private String documentId;
    private String text;
    private float[] embedding;

    public DocumentChunk(String chunkId, String documentId, String text, float[] embedding) {
        this.chunkId = chunkId;
        this.documentId = documentId;
        this.text = text;
        this.embedding = embedding;
    }

    public String getChunkId() { return chunkId; }
    public String getDocumentId() { return documentId; }
    public String getText() { return text; }
    public float[] getEmbedding() { return embedding; }

    public DocumentChunk() {} // Constructor vacío para Jackson

    public void setChunkId(String chunkId) { this.chunkId = chunkId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public void setText(String text) { this.text = text; }
    public void setEmbedding(float[] embedding) { this.embedding = embedding; }
}
