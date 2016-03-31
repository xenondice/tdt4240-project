package tdt4240.lyvlyvtjuesyv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Created by RayTM on 30.03.2016.
 */
public class ClientListener {
    private Socket client;
    private BufferedReader input;
    private BufferedWriter output;
    private Thread pingThread;

    public ClientListener(Socket client) {
        this.client = client;

        try {
            input = new BufferedReader(new InputStreamReader(client.getInputStream()));
            output = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
        } catch (IOException e) {
            disconnect();
            e.printStackTrace();
        }

        startPingLoop();
    }

    private void startPingLoop() {
        pingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if (!ping()) {
                            disconnect();
                            return;
                        }
                        Thread.sleep(Constants.PING_INTERVAL);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        });
        pingThread.start();
    }

    public boolean ping() throws InterruptedException {
        send(Constants.REQUEST_STATUS);
        if (expectHeader() == Constants.STATUS_NO_ANSWER) {
            return false;
        } else {
            return true;
        }
    }

    public char expectHeader() throws InterruptedException {
        return expectResponse().charAt(0);
    }

    private String expectResponse() throws InterruptedException {
        long started = System.currentTimeMillis();
        while (System.currentTimeMillis() - started <= Constants.RESPONSE_TIMEOUT) {
            try {
                if (input.ready()) {
                    return input.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            Thread.sleep(Constants.RESPONSE_CHECK_INTERVAL);
        }
        return Constants.STATUS_NO_ANSWER + "";
    }

    public void send(char header) {
        send(header, "");
    }

    public void send(char header, String message) {
        try {
            output.write(header + message + '\n');
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
            disconnect();
        }
    }

    public void disconnect() {
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (pingThread != null)
            pingThread.interrupt();
        if (LocalServer.getInstance() != null)
            LocalServer.getInstance().kick(this);
    }
}
