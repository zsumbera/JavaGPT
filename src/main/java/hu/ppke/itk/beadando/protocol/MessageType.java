package hu.ppke.itk.beadando.protocol;

public enum MessageType {
    LOGIN, LOGIN_OK,
    REGISTER, REGISTER_OK,
    CHAT, CHAT_CHUNK, CHAT_END,
    HISTORY_REQUEST, HISTORY_RESPONSE,
    ERROR
}