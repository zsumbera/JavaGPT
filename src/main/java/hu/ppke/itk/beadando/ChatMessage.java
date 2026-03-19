package hu.ppke.itk.beadando;

public class ChatMessage {
    public enum Role {
        USER,
        ASSISTANT
    }

    private final Role role;
    private final String text;

    public ChatMessage(Role role, String text) {
        this.role = role;
        this.text = text;
    }

    public Role getRole() {
        return role;
    }

    public String getText() {
        return text;
    }
}
