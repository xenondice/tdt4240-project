package tdt4240.lyvlyvtjuesyv;

import android.app.IntentService;
import android.content.Intent;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by RayTM on 28.03.2016.
 */
public class LocalServer extends IntentService {
    private static LocalServer instance = null;

    private ServerSocket server;
    private Thread serverThread;
    private State state;
    private List<ClientListener> clients;
    private ClientListener host;

    public void kick(ClientListener client) {
        print("Kicked " + client + "!");
        clients.remove(client);
        if (client.equals(host)) {
            print("Bummer, it was the host...");
            close();
        }
    }

    private enum State {
        waiting_for_clients,
        closed,
        started
    }

    public static LocalServer getInstance() {
        return instance;
    }

    public LocalServer() {
        super("Server");
    }

    private void initServer() {
        serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                print("Server up, waiting for clients...");
                state = State.waiting_for_clients;
                while (state != State.closed) {
                    try {
                        switch (state) {
                            case waiting_for_clients:
                                Socket clientSocket = server.accept();
                                ClientListener client = new ClientListener(clientSocket);
                                print("New client connected!");
                                clients.add(client);
                                if (clients.size() == 1) {
                                    print("Welcome boss!");
                                    host = client;
                                }
                                break;
                            default:
                                break;
                        }
                        Thread.sleep(1000, 0);
                    } catch (IOException | InterruptedException e) {
                        continue;
                    }
                }
            }
        });
    }

    public void close() {
        print("Server closing...");
        changeState(State.closed);
        try {
            server.close();
        } catch (IOException e) {
            print("Couldn't close server!");
            e.printStackTrace();
        }
        instance = null;
        for (ClientListener client : clients)
            client.disconnect();
        stopSelf();
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

    @Override
    protected void onHandleIntent(Intent workIntent) {

        // Setup singleton class
        if (instance != null) {
            print("Another server is already running!");
            return;
        }
        instance = this;
        clients = new ArrayList<>();

        // Setup server
        initServer();

        // Start server
        try {
            server = new ServerSocket(Constants.DEFAULT_PORT);
        } catch (IOException e) {
            print("Couldn't start server!");
            e.printStackTrace();
            return;
        }
        serverThread.start();

        // Inform host that the server is up
        GameHub.getInstance().connect();
    }
}
