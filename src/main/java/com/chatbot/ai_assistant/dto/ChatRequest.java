package com.chatbot.ai_assistant.dto;

import java.util.List;
//Only transfer data
public class ChatRequest {
    private String message;
    private String carrera;
    private List<String> materias;
    private String username;
    private String documentId;
    private String imageId;
    
    // Getters y setters
    public String getImageId() { return imageId; }
    public void setImageId(String imageId) { this.imageId = imageId;}
    public String getDocumentId(){return documentId;}
    public void setDocumentId (String documentId){this.documentId = documentId;}
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getCarrera() { return carrera; }
    public void setCarrera(String carrera) { this.carrera = carrera; }
    public List<String> getMaterias() { return materias; }
    public void setMaterias(List<String> materias) { this.materias = materias; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
}