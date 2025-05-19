
# USB AI Assistant

## Descripción

Este proyecto es un asistente educativo inteligente que responde preguntas académicas, analiza documentos y convierte respuestas en audio. Utiliza modelos locales y en la nube para ofrecer respuestas contextuales y de alta calidad.

---

## Tecnologías utilizadas

- **Spring Boot** (backend)
- **Tailwind CSS** (frontend)
- **Gemini** a través de **Vertex AI** (Google Cloud) para generación de respuestas
- **ElevenLabs** para Text to Speech (TTS)
- **Tika Reader** para extracción de texto de documentos
- **Zero-Shot Classification** para clasificación de intenciones
- **MySQL** como base de datos (database:ai_asistant)

---

## Requisitos previos

1. **Java 21+** y **Maven** instalados y configurados en el PATH.
2. **MySQL** instalado y corriendo.
3. **Cuenta de Google Cloud** con Vertex AI habilitado y el proyecto especificado en `application.properties`. Para mas informacion, leer documentacion de Spring AI en Vertex AI. 
4. **API Key de ElevenLabs** agregada como variable de entorno (`ELEVENLABS_API_KEY`).