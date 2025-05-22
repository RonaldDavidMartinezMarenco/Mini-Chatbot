package com.chatbot.ai_assistant.service;

import java.io.File;
import java.nio.file.Files;

import org.springframework.stereotype.Service;

import net.andrewcpu.elevenlabs.ElevenLabs;
import net.andrewcpu.elevenlabs.api.ElevenLabsAPI;
import net.andrewcpu.elevenlabs.builders.SpeechGenerationBuilder;
import net.andrewcpu.elevenlabs.enums.ElevenLabsVoiceModel;
import net.andrewcpu.elevenlabs.enums.GeneratedAudioOutputFormat;
import net.andrewcpu.elevenlabs.enums.StreamLatencyOptimization;
import net.andrewcpu.elevenlabs.model.voice.VoiceSettings;


@Service
public class TTSService {
    //Coqui tts o Google Cloud TTS 
    private static final String API_KEY = System.getenv("ELEVENLABS_API_KEY");
    private static final String VOICE_ID = "21m00Tcm4TlvDq8ikWAM"; // Cambia por tu voiceId preferido

    public TTSService() {
        ElevenLabs.setApiKey(API_KEY);
        ElevenLabs.setDefaultModel("eleven_multilingual_v2"); // Usa modelo multilenguaje para español
    }

    public byte[] synthesize(String text) {
        try {
            VoiceSettings settings = new VoiceSettings(0.5, 0.5);
            File audioFile = SpeechGenerationBuilder.textToSpeech()
                    .file()
                    .setText(text)
                    .setGeneratedAudioOutputFormat(GeneratedAudioOutputFormat.MP3_44100_128)
                    .setVoiceId(VOICE_ID)
                    .setVoiceSettings(settings)
                    .setModel(ElevenLabsVoiceModel.ELEVEN_MULTILINGUAL_V2)
                    .setLatencyOptimization(StreamLatencyOptimization.NONE)
                    .build();
            return Files.readAllBytes(audioFile.toPath());
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el audio con ElevenLabs", e);
        }
    }
}
