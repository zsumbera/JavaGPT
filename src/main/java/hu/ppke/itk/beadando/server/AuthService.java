package hu.ppke.itk.beadando.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import hu.ppke.itk.beadando.client.User;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AuthService {

    private static final String FILE = "users.json";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final List<User> users = new CopyOnWriteArrayList<>();

    public AuthService() {
        loadFromFile();
    }

    public boolean register(String username, String password) {
        synchronized (this) {
            boolean exists = users.stream()
                    .anyMatch(u -> u.getUsername().equals(username));
            if (exists) return false;
            users.add(new User(username, hashPassword(password)));
            saveToFile();
            return true;
        }
    }

    public boolean login(String username, String password) {
        return users.stream().anyMatch(u ->
                u.getUsername().equals(username) &&
                        u.getPasswordHash().equals(hashPassword(password))
        );
    }

    private String hashPassword(String password) {
        try {
            var digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("Hash hiba", e);
        }
    }

    public synchronized void saveToFile() {
        try (Writer w = new FileWriter(FILE)) {
            gson.toJson(new ArrayList<>(users), w);
        } catch (IOException e) {
            System.err.println("[Auth] Mentési hiba: " + e.getMessage());
        }
    }

    private void loadFromFile() {
        File f = new File(FILE);
        if (!f.exists()) return;
        try (Reader r = new FileReader(f)) {
            List<User> loaded = gson.fromJson(r, new TypeToken<List<User>>(){}.getType());
            if (loaded != null) users.addAll(loaded);
            System.out.println("[Auth] Betöltve: " + users.size() + " user");
        } catch (IOException e) {
            System.err.println("[Auth] Betöltési hiba: " + e.getMessage());
        }
    }
}