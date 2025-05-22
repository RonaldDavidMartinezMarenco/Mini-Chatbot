package com.chatbot.ai_assistant.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.chatbot.ai_assistant.rag.ragService;

import jakarta.annotation.PostConstruct;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
public class DocumentController 
{
    private final Map<String, String> documentStore = new ConcurrentHashMap<>();
    @Autowired
    private ragService ragService;
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
            ragService.indexDocument(documentId,content);
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
        return content;
    }    
    @PostConstruct
    public void initUnisimonDoc() {
        try {
            // Cambia la ruta y extensión según tu archivo
            ClassPathResource resources = new ClassPathResource("static/documents/unisimon-doc.pdf");
            InputStream inputStream = resources.getInputStream();
            InputStreamResource tikaResource = new InputStreamResource(inputStream);
            TikaDocumentReader reader = new TikaDocumentReader(tikaResource);
            List<Document> docs = reader.read();
            String content = docs.stream().map(Document::getText).collect(Collectors.joining("\n"));


            // Guarda el contenido en el store y lo indexa
            documentStore.put("unisimon-doc", content);
            ragService.loadUnisimonEmbeddings();
            if(ragService.retrieveRelevantChunks("unisimon-doc", "test", 1).isEmpty()){
                ragService.indexDocument("unisimon-doc", content);
                ragService.saveUnisimonEmbeddings();
            }
            System.out.println("Documento Unisimon RAG cargado e indexado correctamente.");
        } catch (Exception e) {
            System.err.println("Error cargando el documento Unisimon RAG: " + e.getMessage());
        }
    }
}