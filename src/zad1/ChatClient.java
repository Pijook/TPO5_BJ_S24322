/**
 *
 *  @author Bielecki Jakub S24322
 *
 */

package zad1;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ChatClient {

    private final String host;
    private final int port;
    private final String id;
    private final Charset charset;
    private final static int toSleep = 10;

    private List<String> chatView;

    private SocketChannel socket;
    private PrintWriter out;
    private BufferedReader in;

    public ChatClient(String host, int port, String id) {
        this.host = host;
        this.port = port;
        this.id = id;
        this.charset = StandardCharsets.UTF_8;
        this.chatView = new ArrayList<>();
    }

    public void login() {
        send("login;" + id);
    }

    public void logout() {
        send("logout;" + id);
    }

    public void text(String message) {
        send("text;" + message);
    }

    public String getChatView() {
        String result = "";
        for(int i = 0; i < chatView.size(); i++) {
            result = result + chatView.get(i);
            if(i != chatView.size() - 1) {
                result = result + "\n";
            }
        }

        return result;
    }

    public void connect() {
        try {
            socket = SocketChannel.open(new InetSocketAddress(host, port));
            socket.configureBlocking(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String send(String request) {
        StringBuilder response = null;
        try {
            response = new StringBuilder();
            ByteBuffer byteBuffer = ByteBuffer.wrap(request.getBytes());
            socket.write(byteBuffer);

            byteBuffer.clear();

            int toRead = 0;
            while(toRead == 0) {
                Thread.sleep(toSleep);
                toRead = socket.read(byteBuffer);
                System.out.println("0" + toRead);
            }

            while(toRead != 0) {
                byteBuffer.flip();

                response.append(charset.decode(byteBuffer));

                byteBuffer.clear();

                toRead = socket.read(byteBuffer);
                System.out.println("!0" + toRead);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return response.toString().replace(";", "\n");
    }

    public String getId() {
        return id;
    }

}
