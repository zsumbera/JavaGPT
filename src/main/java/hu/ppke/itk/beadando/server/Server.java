package hu.ppke.itk.beadando.server;

import hu.ppke.itk.beadando.server.ollama.Ollama;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server implements Runnable {

    public static final int PORT = 2222;
    private static final int MAX_THREADS = 20;

    private final ServerSocket     serverSocket;
    private final ExecutorService  threadPool;
    private final AuthService      authService;
    private final Ollama ollamaService;
    private volatile boolean       running = true;

    public Server() throws Exception {
        serverSocket  = new ServerSocket(PORT);
        threadPool    = Executors.newFixedThreadPool(MAX_THREADS);
        authService   = new AuthService();
        ollamaService = new Ollama();
        System.out.println("[Server] Elindult, port: " + PORT);
    }

    @Override
    public void run() {
        while (running) {
            try {
                Socket client = serverSocket.accept();
                System.out.println("[Server] Új kliens: " + client.getInetAddress());
                threadPool.submit(new ClientHandler(client, authService, ollamaService));
            } catch (IOException e) {
                if (running) System.err.println("[Server] Accept hiba: " + e.getMessage());
            }
        }
    }

    public void stop() {
        running = false;
        authService.saveToFile();
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(5, TimeUnit.SECONDS))
                threadPool.shutdownNow();
            serverSocket.close();
        } catch (IOException | InterruptedException e) {
            threadPool.shutdownNow();
        }
        System.out.println("[Server] Leállt.");
    }

    public static void main(String[] args) throws Exception {
        Server server = new Server();
        Thread t = new Thread(server, "server-main");
        t.start();

        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
    }
}