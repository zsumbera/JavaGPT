package hu.ppke.itk.beadando;

import com.google.gson.Gson;
import hu.ppke.itk.beadando.client.Client;
import hu.ppke.itk.beadando.protocol.Message;
import hu.ppke.itk.beadando.protocol.MessageType;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegisterView {

    @FXML private TextField     user_name;
    @FXML private PasswordField password;
    @FXML private Label         errorLabel;

    private final Gson gson = new Gson();
    private Client client;

    public void setClient(Client client) {
        this.client = client;
        this.client.setMessageHandler(this::handleResponse);
    }

    @FXML
    public void regist() {
        String username = user_name.getText().trim();
        String pass     = password.getText();

        if (username.isBlank() || pass.isBlank()) {
            errorLabel.setText("Töltsd ki az összes mezőt");
            return;
        }

        Message msg = new Message(MessageType.REGISTER, pass);
        msg.username = username;
        client.send(gson.toJson(msg));
    }

    private void handleResponse(String json) {
        Message msg = gson.fromJson(json, Message.class);
        if (msg == null) return;

        switch (msg.type) {
            case REGISTER_OK -> {
                errorLabel.setStyle("-fx-text-fill: green;");
                errorLabel.setText("Sikeres regisztráció! Bezárás...");

                new Thread(() -> {
                    try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                    javafx.application.Platform.runLater(() ->
                            ((Stage) user_name.getScene().getWindow()).close()
                    );
                }).start();
            }
            case ERROR -> errorLabel.setText(msg.payload);
            default    -> {}
        }
    }
}