package hu.ppke.itk.beadando;

import com.google.common.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.ollama4j.exceptions.OllamaException;

public class Server implements Runnable {
    Gson gson ;
    private List<User> users = Collections.synchronizedList(new ArrayList<>());
    protected static ServerSocket serverSocket;
    protected Ollama ollama;
    protected Socket socket;
    static int PORT_NUMBER = 2222;


    public Server() throws IOException {
        gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            serverSocket = new ServerSocket(PORT_NUMBER);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        loadFromFile();
    }

    public static void main(String[] args) throws IOException {
        new Thread(new Server()).start();
        System.out.println("A Szerver elindult");
    }

    @Override
    public void run() {
        System.out.println("Server fut");

        while (!Thread.currentThread().isInterrupted()) {
            try {
                Socket socket = serverSocket.accept();

                System.out.println("Új kliens csatlakozott");

                Ollama ollama = new Ollama(socket);

                new Thread(() -> {
                    try {
                        ollama.chat();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();

            } catch (IOException e) {
                System.err.println("Failed to communicate with client!");
            }
        }

        System.out.println("A Szerver leáll");
    }


    private void loadFromFile() {
        File file = new File("users.json");
        if (!file.exists()) return;

        try (Reader reader = new FileReader(file)) {
            List<User> loaded = gson.fromJson(reader,new TypeToken<ArrayList<User>>(){}.getType());
            if (loaded != null) this.users = loaded;
            System.out.println("Adatok betöltve. User-ek száma: " + users.size());
        } catch (IOException e) {
            System.err.println("Hiba a betöltéskor: " + e.getMessage());
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void saveToFile() {
        try (Writer writer = new FileWriter("users.json")) {
            gson.toJson(users, writer);
        } catch (IOException e) {
            System.err.println("Hiba a mentéskor: " + e.getMessage());
        }
    }

    public void addUser(User user) {
        users.add(user);
    }


}
