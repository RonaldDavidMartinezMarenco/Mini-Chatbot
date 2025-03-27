package com.chatbot.ai_assistant;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 *
 * @author Ronald
 */

/*
 * Esta clase se encarga de enviar las solicitudes y recibir la respuesta del LLMs
 */
@RestController 
@CrossOrigin(origins = "http://localhost:3000") //Permite que el servidor de react se comunique con el servidor de spring
public class ChatController {

    private final ChatClient chatClient; //Tgis universal to all llms

    public ChatController(ChatClient.Builder builder) {
        this.chatClient = builder.defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory())).build();  //Se le puede agregar mas funciones., en este caso se le agrega la memoria
    }
    
    @PostMapping("/chat") //Envia datos al servidor, en este caso 8080.
    public String chat(@RequestParam String message){
        String prompt = "Eres un chatbot especializado en educación y solo respondes preguntas estrictamente educativas en español. No aceptas preguntas personales, de entretenimiento, políticas, de opinión o cualquier otro tema fuera del ámbito educativo. Antes de responder, analiza si la pregunta está relacionada con educación (matemáticas, ciencias, historia, literatura, tecnología, etc.). Si la pregunta no es educativa, responde con: 'Lo siento, soy un chatbot entrenado solo para responder preguntas educativas'";
        String fullPrompt = prompt +"\nUsuario"+ message;
        return chatClient.prompt()
                .user(fullPrompt)
                .call()
                .content();
    }      

    @GetMapping("/stream")
    public Flux<String> chatWithStream(@RequestParam String message) {
        String prompt = "Eres un chatbot educativo y amigable que responde en español. Puedes recibir mensajes de saludo y contestar de forma cordial, manteniendo una conversación agradable. Sin embargo, solo respondes preguntas relacionadas con educación (por ejemplo, matemáticas, ciencias, historia, literatura, tecnología, etc.). Si recibes una consulta que no sea educativa, responde con: 'Lo siento, soy un chatbot entrenado solo para responder preguntas educativas'. Además, si no entiendes la intención o los datos específicos de un mensaje, informa al usuario: 'Lo siento, no puedo responder a eso en este momento";
        String fullPrompt = prompt + "\nUsuario: " + message;
        return chatClient.prompt()
                .user(fullPrompt)
                .stream()
                .content();
    }
    
}
