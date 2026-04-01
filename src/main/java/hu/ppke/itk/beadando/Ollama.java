package hu.ppke.itk.beadando;
import io.github.ollama4j.exceptions.OllamaException;
import io.github.ollama4j.models.chat.OllamaChatMessageRole;
import io.github.ollama4j.models.chat.OllamaChatRequest;
import io.github.ollama4j.models.chat.OllamaChatResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Ollama extends Thread{
    protected Socket clientSocket;
    protected BufferedReader clientReader;
    protected PrintWriter clientWriter;

    private io.github.ollama4j.Ollama ollama;
    private String model = "gemma3:1b";
    private OllamaChatRequest builder;
    private OllamaChatRequest requestModel;
    private OllamaChatResult chatResult;

    public Ollama(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        this.clientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.clientWriter = new PrintWriter(clientSocket.getOutputStream());

        try {
            ollama = new io.github.ollama4j.Ollama("http://127.0.0.1:11434");
            ollama.pullModel(model);
            System.out.println("Ollama init");
            builder = OllamaChatRequest.builder().withModel(model);
            requestModel =
                    builder.withMessage(OllamaChatMessageRole.USER, ("Hi!"))
                            .build();
            chatResult = ollama.chat(requestModel, null);


            clientWriter.println(chatResult.getResponseModel().getMessage().getResponse() + "END");
            clientWriter.flush();
            System.out.println(chatResult.getResponseModel().getMessage().getResponse());
        } catch (OllamaException e) {
            System.out.println(e.getMessage());
        }
    }

    public void run(){
        System.out.println("Ollama fut");
    }
    protected void chat(){
        try {
            requestModel =
                    builder.withMessages(chatResult.getChatHistory())
                            .withMessage(
                                    OllamaChatMessageRole.USER, clientReader.readLine())
                            .build();
            chatResult = ollama.chat(requestModel, null);
            System.out.println(chatResult.getResponseModel().getMessage().getResponse());
            clientWriter.println(chatResult.getResponseModel().getMessage().getResponse());
            clientWriter.flush();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
