package com.chatbot.ai_assistant.controller;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
public class DocumentController 
{
    private final Map<String, String> documentStore = new ConcurrentHashMap<>();
    @PostMapping("/upload-document")
    public ResponseEntity<Map<String,String>>uploadDocument(@RequestParam("file") MultipartFile file)
    {
        try{
            //Usamos tikaReader para extraer el texto del archivo
            InputStreamResource resource = new InputStreamResource(file.getInputStream());
            TikaDocumentReader reader = new TikaDocumentReader(resource);
            List<Document> docs = reader.read();

            //Une el contenido de todos los documentos (muchas paginas por ejemplo)
            String content = docs.stream().map(Document::getText).collect(Collectors.joining("\n"));

            String documentId = UUID.randomUUID().toString();
            documentStore.put(documentId, content);
            return ResponseEntity.ok(Map.of("documentId",documentId));
        }catch(Exception e)
        {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error","No se pudo procesar el documento."));
        }
    }

    public String getDocumentContent(String documentId){
        String content = documentStore.get(documentId);
        System.out.println("Buscando documento: " + documentId + " => " + (content == null ? "NO ENCONTRADO" : "ENCONTRADO"));
        return documentStore.get(documentId);
    }

        
}