package hu.ppke.itk.beadando.protocol;

public class Message {
    public MessageType type;
    public String      username;
    public String      payload;
    public String      sessionId;

    public Message() {}

    public Message(MessageType type, String payload) {
        this.type    = type;
        this.payload = payload;
    }
}