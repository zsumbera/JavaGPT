package hu.ppke.itk.beadando.client.controller;

import com.google.gson.Gson;
import hu.ppke.itk.beadando.client.Client;
import hu.ppke.itk.beadando.protocol.Message;
import hu.ppke.itk.beadando.protocol.MessageType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class ChatController {

    @FXML private ListView<ChatMessage> chatList;
    @FXML private TextField             inputField;
    @FXML private Button                send;

    private final ObservableList<ChatMessage> messages =
            FXCollections.observableArrayList();
    private final Gson gson = new Gson();

    private Client  client;
    private String  sessionId;
    private ChatMessage streamingMessage = null;

    @FXML
    private void initialize() {
        chatList.setItems(messages);
        chatList.setCellFactory(lv -> new BubbleCell());

        messages.add(new ChatMessage(Role.ASSISTANT,
                "Szia! Miben segíthetek ma?"));
    }

    public void init(Client client, String sessionId) {
        this.client    = client;
        this.sessionId = sessionId;
        client.setMessageHandler(this::handleIncoming);
    }

    private void handleIncoming(String json) {
        Message msg = gson.fromJson(json, Message.class);
        if (msg == null || msg.type == null) return;

        switch (msg.type) {
            case CHAT_CHUNK -> {
                if (streamingMessage == null) {
                    streamingMessage = new ChatMessage(Role.ASSISTANT, "");
                    messages.add(streamingMessage);
                }
                streamingMessage.appendText(msg.payload);
                chatList.refresh();
                scrollToBottom();
            }
            case CHAT_END -> {
                streamingMessage = null;
                send.setDisable(false);
                scrollToBottom();
            }
            case ERROR -> {
                messages.add(new ChatMessage(Role.ASSISTANT,
                        "[Hiba]: " + msg.payload));
                streamingMessage = null;
                send.setDisable(false);
            }
            default -> {}
        }
    }

    @FXML
    private void onSend() {
        if (client == null) return;
        String text = inputField.getText().trim();
        if (text.isBlank()) return;

        messages.add(new ChatMessage(Role.USER, text));
        inputField.clear();
        send.setDisable(true);
        scrollToBottom();

        Message msg = new Message(MessageType.CHAT, text);
        msg.sessionId = sessionId;
        client.send(gson.toJson(msg));
    }

    private void scrollToBottom() {
        if (!messages.isEmpty())
            chatList.scrollTo(messages.size() - 1);
    }

    private static class BubbleCell extends ListCell<ChatMessage> {
        private final HBox   row    = new HBox();
        private final Label  bubble = new Label();
        private final Region spacer = new Region();

        BubbleCell() {
            bubble.setWrapText(true);
            bubble.setPadding(new Insets(8, 12, 8, 12));
            bubble.setMaxWidth(420);
            HBox.setHgrow(spacer, Priority.ALWAYS);
            row.setSpacing(8);
            row.setPadding(new Insets(4, 12, 4, 12));
            setStyle("-fx-background-color: transparent;");
        }

        @Override
        protected void updateItem(ChatMessage item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                return;
            }
            bubble.setText(item.getText());

            if (item.getRole() == Role.USER) {
                bubble.setStyle(
                        "-fx-background-color: #dbeafe;" +
                                "-fx-background-radius: 16 4 16 16;" +
                                "-fx-text-fill: #1e3a5f;" +
                                "-fx-font-size: 14px;");
                row.getChildren().setAll(spacer, bubble);
                row.setAlignment(Pos.CENTER_RIGHT);
            } else {
                bubble.setStyle(
                        "-fx-background-color: #f3f4f6;" +
                                "-fx-background-radius: 4 16 16 16;" +
                                "-fx-text-fill: #1f2937;" +
                                "-fx-font-size: 14px;");
                row.getChildren().setAll(bubble, spacer);
                row.setAlignment(Pos.CENTER_LEFT);
            }
            setGraphic(row);
        }
    }

    public enum Role { USER, ASSISTANT }

    public static class ChatMessage {
        private final Role          role;
        private final StringBuilder text;

        public ChatMessage(Role role, String text) {
            this.role = role;
            this.text = new StringBuilder(text);
        }

        public void appendText(String chunk) { text.append(chunk); }
        public Role   getRole() { return role; }
        public String getText() { return text.toString(); }
    }
}