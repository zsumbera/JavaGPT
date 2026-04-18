package hu.ppke.itk.beadando;

import com.google.gson.Gson;
import hu.ppke.itk.beadando.client.Client;
import hu.ppke.itk.beadando.client.controller.ChatController;
import hu.ppke.itk.beadando.protocol.Message;
import hu.ppke.itk.beadando.protocol.MessageType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginView {

    @FXML private TextField     user_name;
    @FXML private PasswordField password;
    @FXML private Label         errorLabel;

    private final Gson gson = new Gson();
    private Client client;

    @FXML
    private void initialize() {
        try {
            client = new Client();
            client.setMessageHandler(this::handleResponse);
            client.start();
        } catch (Exception e) {
            showError("Szerver nem elérhető");
        }
    }

    @FXML
    public void login() {
        String username = user_name.getText().trim();
        String pass     = password.getText();

        if (username.isBlank() || pass.isBlank()) {
            showError("Töltsd ki az összes mezőt");
            return;
        }

        Message msg = new Message(MessageType.LOGIN, pass);
        msg.username = username;
        client.send(gson.toJson(msg));
    }

    @FXML
    public void register() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    HelloApplication.class.getResource("register-view.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Regisztráció");
            stage.setScene(new Scene(loader.load(), 400, 300));

            RegisterView controller = loader.getController();
            controller.setClient(client);

            stage.setOnHidden(e -> client.setMessageHandler(this::handleResponse));
            stage.show();
        } catch (IOException e) {
            showError("Nem sikerült megnyitni");
        }
    }

    private void handleResponse(String json) {
        Message msg = gson.fromJson(json, Message.class);
        if (msg == null || msg.type == null) return;

        switch (msg.type) {
            case LOGIN_OK    -> openChatWindow();
            case REGISTER_OK -> showError("Regisztráció sikeres, jelentkezz be");
            case ERROR       -> showError(msg.payload);
            default          -> {}
        }
    }

    private void openChatWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    HelloApplication.class.getResource("hello-view.fxml"));
            Stage stage = new Stage();
            stage.setTitle("JavaGPT");
            stage.setScene(new Scene(loader.load(), 800, 600));

            ChatController controller = loader.getController();
            controller.init(client, user_name.getText().trim());

            Stage loginStage = (Stage) user_name.getScene().getWindow();
            loginStage.close();

            stage.show();
        } catch (IOException e) {
            showError("Nem sikerült megnyitni a chat ablakot");
        }
    }

    private void showError(String msg) {
        if (errorLabel != null) errorLabel.setText(msg);
    }
}