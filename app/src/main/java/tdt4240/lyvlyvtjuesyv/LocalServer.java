package tdt4240.lyvlyvtjuesyv;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

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
    private Thread pingThread;
    private State state;
    private List<Socket> clients;
    private Socket host;
    private BroadcastReceiver hostReceiver = null;

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
                startClientPinging();

                state = State.waiting_for_clients;
                while (state != State.closed) {
                    try {
                        switch (state) {
                            case waiting_for_clients:
                                Socket client = server.accept();
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
                    } catch (InterruptedException e) {
                    } catch (IOException e) {
                        close();
                    }
                }
            }
        });
    }

    public void close() {
        print("Server closing...");
        changeState(State.closed);
        pingThread.interrupt();
        try {
            server.close();
        } catch (IOException e) {
            print("Couldn't close server!");
            e.printStackTrace();
        }
        //if (hostReceiver != null) unregisterReceiver(hostReceiver);
        instance = null;
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
                        Thread.sleep(Constants.PING_INTERVAL);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        });
        pingThread.start();
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
/*
        // Setup communication channel
        hostReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int status = intent.getIntExtra(Constants.SERVER_STATUS_ADD, 0);
                if (status == Constants.HOST_STATUS_STOP) close();
                Toast.makeText(context, "Server received " + status, Toast.LENGTH_LONG).show();
            }
        };
        IntentFilter hostReceiverFilter = new IntentFilter(Constants.SERVER_BROADCAST_ADD);
        registerReceiver(
                hostReceiver,
                hostReceiverFilter);
*/
        initServer();
        try {
            server = new ServerSocket(Constants.DEFAULT_PORT);
        } catch (IOException e) {
            print("Couldn't start server!");
            e.printStackTrace();
            return;
        }
        serverThread.start();
        GameHub.getInstance().connect();
        /*print("Broadcasting that server is up...");
        Intent intent = new Intent(Constants.HOST_BROADCAST_ADD)
                .putExtra(Constants.SERVER_STATUS_ADD, Constants.SERVER_STATUS_ACTIVE);
        sendBroadcast(intent);*/
    }
}
