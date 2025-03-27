package com.chatbot.ai_assistant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import javafx.application.Application;


@SpringBootApplication
public class AiAssistantApplication {

	public static void main(String[] args) {
		//SpringApplication.run(AiAssistantApplication.class, args);
		JavaFxApp.launch(JavaFxApp.class, args);
	}

}
