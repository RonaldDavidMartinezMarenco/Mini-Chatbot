package com.chatbot.ai_assistant;

import java.time.Duration;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import reactor.core.publisher.Flux;


public class JavaFxApp extends Application {
    private ConfigurableApplicationContext context;
    private ChatController chatController;

     @Override
    public void init() {
        // Inicializa el contexto de Spring
        context = new SpringApplicationBuilder(AiAssistantApplication.class).run();
        chatController = context.getBean(ChatController.class);
    }

    @Override
    public void start(Stage primaryStage) {
         // Construir la interfaz JavaFX (por ejemplo, con un TextArea para la conversación y un TextField para escribir)
        TextArea chatArea = new TextArea();
        TextField inputField = new TextField();
        Button sendButton = new Button("Enviar");

        sendButton.setOnAction(event -> {
            String userMessage = inputField.getText();
            if (!userMessage.isEmpty()) {
                chatArea.appendText("Usuario: " + userMessage + "\n");
                inputField.clear();

                // Llamar al método chatWithStream y procesar el flujo de respuestas
                streamChatResponse(chatArea, userMessage);
            }
        });

        VBox root = new VBox(10, chatArea, inputField, sendButton);
        Scene scene = new Scene(root, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Chatbot Educativo");
        primaryStage.show();
    }
    private void streamChatResponse(TextArea chatArea, String userMessage) {
        // Llamar al método chatWithStream del ChatController
        Flux<String> responseStream = chatController.chatWithStream(userMessage);
    
        StringBuilder accumulatedResponse = new StringBuilder();
    
        responseStream
            .delayElements(Duration.ofMillis(100)) // Agregar un retraso de 500 ms entre fragmentos
            .subscribe(
                response -> Platform.runLater(() -> {
                    // Acumular fragmentos en el StringBuilder
                    accumulatedResponse.append(response);
    
                    // Mostrar el texto acumulado en el TextArea
                    chatArea.setText("Chatbot: " + accumulatedResponse.toString());
                }),
                error -> Platform.runLater(() -> chatArea.appendText("Error: " + error.getMessage() + "\n")),
                () -> Platform.runLater(() -> {
                    // Mostrar el mensaje completo al final (opcional)
                    chatArea.appendText("Chatbot: [Respuesta completa]\n");
                })
            );
    }
    @Override

    public void stop() {
        context.close();
        Platform.exit();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}