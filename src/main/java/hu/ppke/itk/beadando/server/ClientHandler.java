package hu.ppke.itk.beadando.server;

import com.google.gson.Gson;
import hu.ppke.itk.beadando.server.ollama.Ollama;
import hu.ppke.itk.beadando.protocol.Message;
import hu.ppke.itk.beadando.protocol.MessageType;
import io.github.ollama4j.models.chat.OllamaChatMessage;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {

    private final Socket      socket;
    private final AuthService authService;
    private final Ollama      ollama;
    private final Gson        gson = new Gson();

    private PrintWriter out;
    private String      loggedInUser = null;

    public ClientHandler(Socket socket, AuthService auth, Ollama ollama) {
        this.socket      = socket;
        this.authService = auth;
        this.ollama      = ollama;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

            String line;
            while ((line = in.readLine()) != null) {
                try {
                    Message msg = gson.fromJson(line, Message.class);
                    if (msg == null || msg.type == null) {
                        send(new Message(MessageType.ERROR, "Érvénytelen üzenet"));
                        continue;
                    }
                    handleMessage(msg);
                } catch (Exception e) {
                    send(new Message(MessageType.ERROR, "Feldolgozási hiba: " + e.getMessage()));
                }
            }
        } catch (IOException e) {
            System.err.println("[Handler] " +
                    (loggedInUser != null ? loggedInUser : "ismeretlen") +
                    " lecsatlakozott: " + e.getMessage());
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    private void handleMessage(Message msg) throws Exception {
        switch (msg.type) {
            case LOGIN           -> handleLogin(msg);
            case REGISTER        -> handleRegister(msg);
            case CHAT            -> handleChat(msg);
            case HISTORY_REQUEST -> handleHistory(msg);
            default              -> send(new Message(MessageType.ERROR,
                    "Ismeretlen üzenet: " + msg.type));
        }
    }

    private void handleLogin(Message msg) {
        boolean ok = authService.login(msg.username, msg.payload);
        if (ok) {
            loggedInUser = msg.username;
            send(new Message(MessageType.LOGIN_OK, "Sikeres bejelentkezés"));
        } else {
            send(new Message(MessageType.ERROR, "Hibás felhasználónév vagy jelszó"));
        }
    }

    private void handleRegister(Message msg) {
        boolean ok = authService.register(msg.username, msg.payload);
        if (ok) {
            send(new Message(MessageType.REGISTER_OK, "Sikeres regisztráció"));
        } else {
            send(new Message(MessageType.ERROR, "Felhasználónév már foglalt"));
        }
    }

    private void handleChat(Message msg) throws Exception {
        if (loggedInUser == null) {
            send(new Message(MessageType.ERROR, "Előbb jelentkezz be"));
            return;
        }

        String sid = (msg.sessionId != null && !msg.sessionId.isBlank())
                ? msg.sessionId : "default";
        String sessionId = loggedInUser + ":" + sid;

        try {
            ollama.chat(sessionId, msg.payload, chunk ->
                    send(new Message(MessageType.CHAT_CHUNK, chunk))
            );
        } finally {
            send(new Message(MessageType.CHAT_END, ""));
        }
    }

    private void handleHistory(Message msg) {
        if (loggedInUser == null) {
            send(new Message(MessageType.ERROR, "Előbb jelentkezz be"));
            return;
        }

        String sid = (msg.sessionId != null && !msg.sessionId.isBlank())
                ? msg.sessionId : "default";
        String sessionId = loggedInUser + ":" + sid;

        List<OllamaChatMessage> history = ollama.getHistory(sessionId);
        for (OllamaChatMessage entry : history) {
            send(new Message(MessageType.HISTORY_RESPONSE,
                    entry.getRole().getRoleName() + ": " + entry.getResponse()));
        }
        send(new Message(MessageType.CHAT_END, ""));
    }

    private synchronized void send(Message msg) {
        if (out != null) {
            out.println(gson.toJson(msg));
        }
    }
}