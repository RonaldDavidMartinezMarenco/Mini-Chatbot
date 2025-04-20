package com.chatbot.ai_assistant.dto;

import java.util.List;

public class ChatRequest {
    private String message;
    private String carrera;
    private List<String> materias;
    
    // Getters y setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getCarrera() { return carrera; }
    public void setCarrera(String carrera) { this.carrera = carrera; }
    public List<String> getMaterias() { return materias; }
    public void setMaterias(List<String> materias) { this.materias = materias; }
    
}