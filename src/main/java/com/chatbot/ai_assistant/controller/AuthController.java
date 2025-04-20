package com.chatbot.ai_assistant.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.chatbot.ai_assistant.model.User;
import com.chatbot.ai_assistant.service.UserService;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")

public class AuthController { 
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Object register(@RequestBody User user) {
        System.out.println("Recibido: " + user.getUsername() + ", " + user.getCarrera() + ", " + user.getMaterias());
        User created = userService.register(user);
        if (created == null) {
            return new org.springframework.http.ResponseEntity<>("Usuario o email ya existen", org.springframework.http.HttpStatus.BAD_REQUEST);
        }
        return created;
    }

    @PostMapping("/login")
    public Object login(@RequestBody java.util.Map<String, String> body) {
        String username = body.get("username");
        String email = body.get("email");
        String password = body.get("password");
        String userOrEmail = (username != null && !username.isEmpty()) ? username : email;
        User user = userService.login(userOrEmail, password);
        if (user == null) {
            return new org.springframework.http.ResponseEntity<>("Credenciales incorrectas", org.springframework.http.HttpStatus.UNAUTHORIZED);
        }
        return user;
    }
}
