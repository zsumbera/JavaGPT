package hu.ppke.itk.beadando;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class HelloController {
    public Button send;
    public ListView<ChatMessage> chatList;
    @FXML
    private TextField prompt;
    Server server;
    private final ObservableList<ChatMessage> messages = FXCollections.observableArrayList();



    public HelloController() {
        this.server = new Server();
    }

    @FXML
    private void initialize() {
        chatList.setItems(messages);
        chatList.setCellFactory(list -> new ChatMessageCell());
    }

    @FXML
    protected void onSend(){
        String text = prompt.getText();
        if (text == null || text.isBlank()) {
            return;
        }
        messages.add(new ChatMessage(ChatMessage.Role.USER, text));
        String response = server.chat(text);
        messages.add(new ChatMessage(ChatMessage.Role.ASSISTANT, response));
        scrollToBottom();
        prompt.setText("");
    }

    public void onStart() {
        prompt.setDisable(false);
        send.setText("Send");
        send.setOnAction(event -> onSend());
        server.run();
        messages.add(new ChatMessage(ChatMessage.Role.ASSISTANT, server.getInit()));
        scrollToBottom();
    }

    private void scrollToBottom() {
        Platform.runLater(() -> {
            if (!messages.isEmpty()) {
                chatList.scrollTo(messages.size() - 1);
            }
        });
    }

    private static class ChatMessageCell extends ListCell<ChatMessage> {
        private final HBox row = new HBox();
        private final Label bubble = new Label();
        private final Region spacer = new Region();

        private ChatMessageCell() {
            bubble.setWrapText(true);
            bubble.setPadding(new Insets(8, 10, 8, 10));
            row.setSpacing(6);
            row.setPadding(new Insets(4, 8, 4, 8));
            HBox.setHgrow(spacer, Priority.ALWAYS);
        }

        @Override
        protected void updateItem(ChatMessage item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            bubble.setText(item.getText());
            double maxWidth = getListView() == null ? 480 : getListView().getWidth() * 0.75;
            bubble.setMaxWidth(Math.max(200, maxWidth));

            if (item.getRole() == ChatMessage.Role.USER) {
                bubble.setStyle("-fx-background-color: #dbeafe; -fx-background-radius: 8;");
                row.getChildren().setAll(spacer, bubble);
                row.setAlignment(Pos.CENTER_RIGHT);
            } else {
                bubble.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 8;");
                row.getChildren().setAll(bubble, spacer);
                row.setAlignment(Pos.CENTER_LEFT);
            }

            setText(null);
            setGraphic(row);
        }
    }
}
