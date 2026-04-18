package hu.ppke.itk.beadando.server.ollama;

import io.github.ollama4j.models.chat.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class Ollama {

    private static final String MODEL = "gemma3:1b";

    private final io.github.ollama4j.Ollama api;

    private final Map<String, List<OllamaChatMessage>> histories =
            new ConcurrentHashMap<>();

    public Ollama() throws Exception {
        api = new io.github.ollama4j.Ollama("http://127.0.0.1:11434");
        api.pullModel(MODEL);
        System.out.println("[Ollama] Kész: " + MODEL);
    }
    public void chat(String sessionId, String userMessage, Consumer<String> onChunk)
            throws Exception {

        List<OllamaChatMessage> history =
                histories.computeIfAbsent(sessionId, id -> new ArrayList<>());

        OllamaChatRequest request = OllamaChatRequest.builder()
                .withModel(MODEL)
                .withMessages(history)
                .withMessage(OllamaChatMessageRole.USER, userMessage)
                .build();

        OllamaChatResult result = api.chat(request, token -> {
            String chunk = token.getMessage().getResponse();
            if (chunk != null && !chunk.isEmpty()) {
                onChunk.accept(chunk);
            }
        });

        history.addAll(result.getChatHistory());
    }

    public List<OllamaChatMessage> getHistory(String sessionId) {
        return histories.getOrDefault(sessionId, List.of());
    }

    public void clearSession(String sessionId) {
        histories.remove(sessionId);
        System.out.println("[Ollama] Session törölve: " + sessionId);
    }
}