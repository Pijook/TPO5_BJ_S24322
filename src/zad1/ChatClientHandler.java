package zad1;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

public class ChatClientHandler {

    private final List<String> clientLog;
    private String clientId;
    private final SocketChannel clientSocket;

    public ChatClientHandler(SocketChannel clientSocket) {
        this.clientSocket = clientSocket;
        this.clientLog = new ArrayList<>();
    }

    public String getClientId() {
        return clientId;
    }

    public List<String> getClientLog() {
        return clientLog;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
