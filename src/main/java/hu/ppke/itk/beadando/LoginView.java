package hu.ppke.itk.beadando;

import io.github.ollama4j.exceptions.OllamaException;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginView {
    public TextField password;
    public TextField user_name;

    public void login() throws IOException {
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        HelloController controller = fxmlLoader.getController();
        stage.setTitle("JavaGPT");
        stage.setScene(scene);
        stage.show();
        if (controller != null) {
            controller.onStart();
        }
    }
}
