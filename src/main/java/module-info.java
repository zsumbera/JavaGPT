module hu.ppke.itk.beadando {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires ollama4j;


    opens hu.ppke.itk.beadando to javafx.fxml;
    exports hu.ppke.itk.beadando;
}