package hu.ppke.itk.beadando;


import io.github.ollama4j.Ollama;
import io.github.ollama4j.exceptions.OllamaException;
import io.github.ollama4j.models.chat.OllamaChatMessageRole;
import io.github.ollama4j.models.chat.OllamaChatRequest;
import io.github.ollama4j.models.chat.OllamaChatResult;

public class Server implements Runnable {
    private Ollama ollama;
    private String model = "gemma3:1b";
    private OllamaChatRequest builder;
    private OllamaChatRequest requestModel;
    private OllamaChatResult chatResult;
    private String init;
    public static void main(String[] args){


    }

    @Override
    public void run() {
        try {
        ollama = new Ollama("http://127.0.0.1:11434");
        ollama.pullModel(model);

            builder = OllamaChatRequest.builder().withModel(model);
            requestModel =
                    builder.withMessage(OllamaChatMessageRole.USER, ("Hi!"))
                            .build();
            chatResult = ollama.chat(requestModel, null);


           init =(chatResult.getResponseModel().getMessage().getResponse());
        } catch (OllamaException e) {
            System.out.println(e.getMessage());
        }
    }

    public String getInit(){
        return init;
    }

    public String chat(String prompt){
        try {
            requestModel =
                    builder.withMessages(chatResult.getChatHistory())
                            .withMessage(
                                    OllamaChatMessageRole.USER, prompt)
                            .build();
            chatResult = ollama.chat(requestModel, null);
            return (chatResult.getResponseModel().getMessage().getResponse());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return "Error";
    }
}
