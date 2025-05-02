package com.chatbot.ai_assistant.repository;

import com.chatbot.ai_assistant.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
//database managment
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    User findByEmail(String email);
}