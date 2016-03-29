package tdt4240.lyvlyvtjuesyv;

import android.app.IntentService;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by RayTM on 28.03.2016.
 */
public class LocalServer extends IntentService {
    public static final int DEFAULT_PORT = 56694;
    public static final String HOME_ADDRESS = "localhost";
    private static final long PING_INTERVAL = 500;

    private ServerSocket server;
    private Thread serverThread;
    private Thread pingThread;
    private State state;
    private List<Socket> clients;
    private Socket host;

    private enum State {
        waiting_for_clients,
        closed,
        started
    }

    public LocalServer() {
        super("Server");
        serverThread = Thread.currentThread();
        clients = new ArrayList<>();
        try {
            server = new ServerSocket(DEFAULT_PORT);
        } catch (IOException e) {
            print("Couldn't start server!");
            e.printStackTrace();
            return;
        }
        print ("Server up on port " + server.getLocalPort());
        run();
    }

    public void close() {
        print("Server closing...");
        changeState(State.closed);
        pingThread.interrupt();
    }

    public void start() {
        print("Game starting...");
        changeState(State.started);
    }

    private void changeState(State state) {
        this.state = state;
        serverThread.interrupt();
    }

    private void print(String message) {
        System.out.println(message);
    }

    private void startClientPinging() {
        pingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (state != State.closed) {
                    for (Socket client : clients) {
                        if (!client.isConnected()) {
                            clients.remove(client);
                            print("Client disconnected");
                            if (client.equals(host)) {
                                close();
                                return;
                            }
                        }
                    }
                    try {
                        Thread.sleep(PING_INTERVAL);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        });
        pingThread.run();
    }

    private void run() {
        startClientPinging();
        print("Server up, waiting for clients...");
        state = State.waiting_for_clients;
        while (state != State.closed) {
            try {
                switch (state) {
                    case waiting_for_clients:
                        Socket client = server.accept();
                        print("New client connected!");
                        clients.add(client);
                        if (clients.size() == 1) {
                            print("This client is the host");
                            host = client;
                        }
                        break;
                    default:
                        break;
                }
                Thread.sleep(1000, 0);
            } catch (InterruptedException e) {
            } catch (IOException e) {
                close();
            }
        }
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        String dataString = workIntent.getDataString();
    }
}
