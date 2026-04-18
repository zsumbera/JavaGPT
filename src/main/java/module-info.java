module hu.ppke.itk.beadando {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires ollama4j;
    requires com.google.gson;
    requires com.google.common;


    opens hu.ppke.itk.beadando to javafx.fxml;
    exports hu.ppke.itk.beadando;
    exports hu.ppke.itk.beadando.protocol;
    opens hu.ppke.itk.beadando.protocol to javafx.fxml;
    exports hu.ppke.itk.beadando.server;
    opens hu.ppke.itk.beadando.server to javafx.fxml;
    exports hu.ppke.itk.beadando.server.ollama;
    opens hu.ppke.itk.beadando.server.ollama to javafx.fxml;
    exports hu.ppke.itk.beadando.client;
    opens hu.ppke.itk.beadando.client to javafx.fxml;
    exports hu.ppke.itk.beadando.client.controller;
    opens hu.ppke.itk.beadando.client.controller to javafx.fxml;
}