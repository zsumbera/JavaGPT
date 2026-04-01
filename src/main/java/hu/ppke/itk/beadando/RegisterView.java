package hu.ppke.itk.beadando;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import javafx.event.ActionEvent;
import javafx.scene.control.TextField;

import java.io.FileWriter;
import java.io.IOException;

public class RegisterView {

    public TextField password;
    public TextField user_name;

    public void regist() {
        User new_user = new User(this.user_name.getText(),this.password.getText());

    }
}
