package hu.ppke.itk.beadando;

import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;

public class Client extends Thread {
    protected Socket clientSocket;
    protected BufferedReader serverInput;
    protected PrintWriter serverOutput;

    private Consumer<String> messageHandler;

    public Client() throws IOException {
        clientSocket = new Socket("localhost", Server.PORT_NUMBER);

        serverInput = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream())
        );

        serverOutput = new PrintWriter(
                clientSocket.getOutputStream(), true
        );
    }

    public void close() throws IOException {
        clientSocket.close();
    }

    @Override
    public void run() {
        try {
            String line;
            while ((line = serverInput.readLine()) != null) {

                String finalLine = line;

                if (messageHandler != null) {
                    messageHandler.accept(finalLine);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setMessageHandler(Consumer<String> handler) {
        this.messageHandler = handler;
    }

    public void send(String msg) {
        serverOutput.println(msg);
    }

    protected String init(){
        try {
            serverInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while (!(line = serverInput.readLine()).equals("END")) {
                response.append(line).append("\n");
            }
            return response.toString();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return "Error";
    }

    protected String chat(String promt){
        try {
            serverInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            serverOutput = new PrintWriter(clientSocket.getOutputStream());
            System.out.println(promt);
            serverOutput.println(promt);
            serverOutput.flush();
            StringBuilder response = new StringBuilder();
            String line;

            while (!(line = serverInput.readLine()).equals("END")) {
                response.append(line).append("\n");
            }
            return response.toString();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
            return "Error";
    }
}
