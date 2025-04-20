package com.chatbot.ai_assistant.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chatbot.ai_assistant.model.User;
import com.chatbot.ai_assistant.repository.UserRepository;


@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public User register(User user) {
        // Validación simple: no permitir usuarios/emails repetidos
        if (userRepository.findByUsername(user.getUsername()) != null ||
            userRepository.findByEmail(user.getEmail()) != null) {
            return null;
        }
        return userRepository.save(user);
    }

    public User login(String usernameOrEmail, String password) {
        User user = userRepository.findByUsername(usernameOrEmail);
        if (user == null) {
            user = userRepository.findByEmail(usernameOrEmail);
        }
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }
}
