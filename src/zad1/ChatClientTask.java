/**
 *
 *  @author Bielecki Jakub S24322
 *
 */

package zad1;


import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class ChatClientTask extends FutureTask<Void> {

    public static ChatClientTask create(ChatClient client, List<String> messages, int delay) {
        ChatClientTask task = new ChatClientTask(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                client.connect();
                client.login();
                messages.forEach(message -> {
                    client.text(message);
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });

                client.logout();
                return null;
            }
        }, client);

        return task;
    }

    private ChatClient client;

    public ChatClientTask(Callable<Void> callable, ChatClient client) {
        super(callable);
        this.client = client;
    }

    public ChatClient getClient() {
        return client;
    }
}
