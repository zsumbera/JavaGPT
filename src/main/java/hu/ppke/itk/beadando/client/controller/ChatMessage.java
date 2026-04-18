package hu.ppke.itk.beadando.client.controller;

public class ChatMessage {

    public enum Role { USER, ASSISTANT }

    private final Role    role;
    private final StringBuilder text;

    public ChatMessage(Role role, String text) {
        this.role = role;
        this.text = new StringBuilder(text);
    }

    public void appendText(String chunk) {
        this.text.append(chunk);
    }

    public Role   getRole() { return role; }
    public String getText() { return text.toString(); }
}