package com.chatbot.ai_assistant.model;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;



@Entity
@Table(name = "users")

public class User {

    public User()
    {

    }
    
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String carrera; // Carrera o rol

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_materias", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "materia")
    private List<String> materias; // Materias específicas

    // Getters y setters
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getCarrera() { return carrera; }
    public void setCarrera(String carrera) { this.carrera = carrera; }
    public List<String> getMaterias() { return materias; }
    public void setMaterias(List<String> materias) {  this.materias = (materias == null) ? new ArrayList<>() : materias; }
    
}
