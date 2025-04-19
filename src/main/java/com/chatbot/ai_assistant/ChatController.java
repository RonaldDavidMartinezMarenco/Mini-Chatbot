package com.chatbot.ai_assistant;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
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

        String intent = classifyIntent(message);
        String prompt;

        if((!intent.equalsIgnoreCase("Educativo") 
            && !intent.equalsIgnoreCase("Opiniones")
            && !intent.equalsIgnoreCase("Universidad")
            && !intent.equalsIgnoreCase("Saludo")
            && !intent.equalsIgnoreCase("Investigacion")
            && !intent.equalsIgnoreCase("Curso en linea")))
            {
             prompt = "Actúa como un asistente educativo diseñado para estudiantes y docentes.\n" + //
                                  "Tu función principal es brindar información clara, precisa y respetuosa sobre temas académicos, científicos, tecnológicos, humanísticos o culturales.\n" + //
                                  "Si el usuario realiza una pregunta que no esté relacionada con el aprendizaje, la formación académica o el desarrollo de habilidades útiles, responde amablemente lo siguiente:\n" + //
                                  "\n" + //
                                  "Explica que eres un chatbot educativo y no estás programado para ese tipo de contenido.\n" + //
                                  "\n" + //
                                  "Invita al usuario a realizar preguntas dentro del ámbito académico.\n" + //
                                  "\n" + //
                                  "No des detalles, opiniones, ni juicios sobre temas fuera de tu alcance educativo.\n" + //
                                  "\n" + //
                                  "Mantén un tono cordial, profesional y claro. No inventes respuestas.\n" + //
                                  "No digas “soy una IA”, sino “soy un asistente educativo”.";

            //Podemos devolver una respuesta en base a un prompt. Haciendo uso de Prompt Engineer   
            //return Flux.just("Lo siento, soy un chatbot entrenado solo para responder preguntas educativas. Por favor, intenta otra pregunta o petición.");
        }else
        {
            //Guide to the chatbot
            prompt = "Eres un asistente académico especializado en educación superior, formación profesional y recursos de aprendizaje.  \r\n" + //
                                "**Tu rol es:**  \r\n" + //
                                "1. Proporcionar respuestas detalladas, precisas y basadas en fuentes confiables.  \r\n" + //
                                "2. Centrarte en temas como:  \r\n" + //
                                "   - Universidades, becas, programas de estudio, cursos en línea (MOOCs), investigación académica.  \r\n" + //
                                "   - Técnicas de estudio, preparación de exámenes, redacción de tesis, herramientas pedagógicas.  \r\n" + //
                                "   - Certificaciones, intercambios académicos, recursos educativos abiertos (REA).  \r\n" + //
                                "\r\n" + //
                                "### **Instrucciones clave**  \r\n" + //
                                "- **Profundidad**: Da pasos concretos, ejemplos prácticos o enlaces a plataformas relevantes (ej: Coursera, edX, Scholar).  \r\n" + //
                                "- **Precisión**: Si no tienes información 100% verificada, dilo.  \r\n" + //
                                "- **Tono**: Formal pero cercano. Evita humor, opiniones personales o desviaciones del tema.  \r\n" + //
                                "\r\n" + //
                                "### **Ejemplos de respuestas ideales**  \r\n" + //
                                "1. **Usuario**: \"¿Cómo elegir una maestría en ingeniería?\"  \r\n" + //
                                "   **Asistente**: \"Para elegir una maestría en ingeniería, sigue estos pasos: 1) Identifica tu especialización (ej: civil, software)... Plataformas como edX ofrecen cursos introductorios gratuitos.\"  \r\n" + //
                                "\r\n" + //
                                "2. **Usuario**: \"¿Qué becas hay para estudiar en España?\"  \r\n" + //
                                "   **Asistente**: \"Las becas más destacadas son: Becas MAEC-AECID para extranjeros, Becas Santander para posgrados... Requisitos comunes: promedio mínimo de 8.0 y carta de motivación.\"  \r\n" + //
                                "\r\n" + //
                                "3. **Usuario**: \"¿Qué es el método Feynman para estudiar?\"  \r\n" + //
                                "   **Asistente**: \"El método Feynman es una técnica de aprendizaje que consiste en: 1) Enseñar el tema como si fuera a un niño... Ejemplo: Aplicarlo en física explicando la gravedad con analogías simples.\"  \r\n" + //
                                "\r\n" + //
                                "### **Notas finales**  \r\n" + //
                                "- **No menciones** que clasificaste la pregunta como educativa.  \r\n" + //
                                "- **Evita por completo** temas de entretenimiento, deportes, videojuegos, o cultura pop.  \r\n" + //
                                "- **Si la pregunta es ambigua**, pide clarificación: \"¿Te refieres a cursos formales o recursos informales?\".  ";
        }

        String fullPrompt = prompt + "\nUsuario: " + message;
        return chatClient.prompt()
                .user(fullPrompt)
                .stream()
                .content();
        
        
    }
    
    //Solo de prueba
    @GetMapping("/test-classify")
    public String testClassifyIntent(@RequestParam String message) {
        System.out.println("Mensaje recibido para clasificación: " + message);
        String intent = classifyIntent(message);
        System.out.println("Intención clasificada: " + intent);
        return "La intención clasificada es: " + intent;
    }

    //We use a text classification zero-shot model
    public String classifyIntent(String message) {
        // Token de Hugging Face para autenticar la solicitud
        String huggingFaceToken = "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"; // Reemplaza con tu token válido
        String apiUrl = "https://api-inference.huggingface.co/models/facebook/bart-large-mnli";
    
        // Configurar los encabezados de la solicitud
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + huggingFaceToken);
        headers.set("Content-Type", "application/json");
    
        // Crear el cuerpo de la solicitud con el mensaje y las etiquetas
        String requestBody = "{ \"inputs\": \"" + message + "\", \"parameters\": { \"candidate_labels\": [\"Universidad\" ,\"Educativo\", \"Investigacion\", \"Cursos en linea\",\"No educativo\", \"Apuestas\",\"Peliculas\",\"Videojuegos\",\"Opiniones\",\"Saludo\" ] } }";
    
        // Crear la solicitud HTTP
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
    
        try {
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, String.class);
    
            // Verificar si la respuesta es exitosa
            if (response.getStatusCode().is2xxSuccessful()) {
                // Parsear la respuesta JSON
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode responseBody = objectMapper.readTree(response.getBody());
    
                // Extraer la etiqueta con mayor probabilidad
                JsonNode labelsNode = responseBody.get("labels");
                return labelsNode.get(0).asText(); // Retorna la etiqueta con mayor probabilidad (primera en el orden)
            } else {
                throw new RuntimeException("Error al clasificar la intención: " + response.getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al procesar la respuesta de Hugging Face.");
        }
    }
}
