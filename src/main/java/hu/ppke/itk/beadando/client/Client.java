package hu.ppke.itk.beadando.client;

import javafx.application.Platform;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class Client extends Thread {

    public static final String HOST = "localhost";
    public static final int    PORT = 2222;

    private final Socket         socket;
    private final BufferedReader in;
    private final PrintWriter    out;

    private volatile Consumer<String> messageHandler;
    private volatile boolean          running = true;

    public Client() throws IOException {
        this(HOST, PORT);
    }

    public Client(String host, int port) throws IOException {
        socket = new Socket(host, port);
        in  = new BufferedReader(new InputStreamReader(socket.getInputStream(),  "UTF-8"));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
        setDaemon(true);
        setName("client-reader");
    }

    @Override
    public void run() {
        try {
            String line;
            while (running && (line = in.readLine()) != null) {
                final String json = line;
                Consumer<String> handler = messageHandler;
                if (handler != null) {
                    Platform.runLater(() -> handler.accept(json));
                }
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("[Client] Kapcsolat megszakadt: " + e.getMessage());
                Consumer<String> handler = messageHandler;
                if (handler != null) {
                    Platform.runLater(() -> handler.accept("{\"type\":\"ERROR\",\"payload\":\"Kapcsolat megszakadt\"}"));
                }
            }
        }
    }

    public void send(String message) {
        out.println(message);
    }

    public void close() {
        running = false;
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("[Client] Hiba záráskor: " + e.getMessage());
        }
    }

    public void setMessageHandler(Consumer<String> handler) {
        this.messageHandler = handler;
    }

    public boolean isConnected() {
        return socket != null && !socket.isClosed() && socket.isConnected();
    }
}