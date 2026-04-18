package hu.ppke.itk.beadando.client;

public record User(String username ,String password) {

    public String getUsername() {
        return username;
    }

    public String getPasswordHash(){
        return password;
    }
}
