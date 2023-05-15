/**
 *
 *  @author Bielecki Jakub S24322
 *
 */

package zad1;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

public class ChatServer {

    private LinkedList<String> serverLog;

    private final String host;
    private final int port;

    private Thread serverThread;

    private static final HashMap<SocketChannel, ChatClientHandler> connectedClients = new HashMap<>();


    private ServerSocketChannel serverSocketChannel;
    private Selector selector;

    public ChatServer(String host, int port) {
        this.host = host;
        this.port = port;
        this.serverLog = new LinkedList<>();
    }

    public void startServer() {
        System.out.println("Server started");
        serverThread = new Thread(() -> {
            try {
                selector = Selector.open();
                serverSocketChannel = ServerSocketChannel.open();
                serverSocketChannel.bind(new InetSocketAddress(host, port));
                serverSocketChannel.configureBlocking(false);
                serverSocketChannel.register(selector, serverSocketChannel.validOps(), null);

                while (!serverThread.isInterrupted()) {
                    selector.select();
                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iter = selectedKeys.iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();

                        if (key.isAcceptable()) {
                            SocketChannel clientSocket = serverSocketChannel.accept();
                            clientSocket.configureBlocking(false);
                            clientSocket.register(selector, SelectionKey.OP_READ);
                            connectedClients.put(clientSocket, new ChatClientHandler(clientSocket));
                        }

                        if (key.isReadable()) {
                            SocketChannel clientSocket = (SocketChannel) key.channel();
                            ByteBuffer buffer = ByteBuffer.allocate(1024);
                            clientSocket.read(buffer);

                            String clientRequest = new String(buffer.array()).trim();

                            String[] parts = clientRequest.split(";");

                            if(parts[0].equals("login")) {
                                handleLogIn(clientSocket, parts);
                            }
                            else if(parts[0].equals("logout")) {
                                handleLogOut(clientSocket, parts);
                            }
                            else if(parts[0].equals("text")) {
                                handleText(clientSocket, parts);
                            }
                        }

                        iter.remove();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        serverThread.start();
    }

    private void handleLogIn(SocketChannel socketChannel, String[] parts) {
        connectedClients.get(socketChannel).setClientId(parts[1]);
        String message = parts[1] + " logged in";
        logToServer(message);
        broadCastMessage(message);
    }

    private void handleLogOut(SocketChannel socketChannel, String[] parts) {
        connectedClients.remove(socketChannel);
        String message = parts[1] + " logged out";
        logToServer(message);
        broadCastMessage(message);
    }

    private void handleText(SocketChannel socketChannel, String[] parts) {
        String id = connectedClients.get(socketChannel).getClientId();
        String message = id + ": ";
        for(int i = 0; i < parts.length; i++) {
            message = message + parts[i];
            if(i != parts.length - 1) {
                message = message + " ";
            }
        }

        message = message + ", mówię ja, " + id;

        logToServer(message);

        broadCastMessage(message);
    }

    private void broadCastMessage(String message) {
        CharBuffer charBuffer = CharBuffer.wrap(message);
        ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(charBuffer);
        for(SocketChannel socketChannel : connectedClients.keySet()) {
            try {
                socketChannel.write(byteBuffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void logToServer(String message) {
        serverLog.add(LocalTime.now() + " " + message);
    }

    private void register(Selector selector, ServerSocketChannel serverSocket) throws IOException {
        SocketChannel client = serverSocket.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
    }

    public void stopServer() {
        try {
            serverThread.interrupt();
            Thread.sleep(300);
            serverSocketChannel.close();
            selector.close();
            System.out.println("Server stopped");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public LinkedList<String> getServerLog() {
        return serverLog;
    }
}
